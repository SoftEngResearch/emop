package edu.cornell;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import edu.cornell.emop.util.Violation;
import edu.illinois.starts.jdeps.DiffMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;

@Mojo(name = "vms", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "vms")
public class VmsMojo extends DiffMojo {

    /**
     * Specific SHA to use as the "old" version of code when making comparisons.
     * Should correspond to the previous run of VMS.
     */
    @Parameter(property = "lastSha", required = false)
    private String lastSha;

    /**
     * Specific SHA to use as the "new" version of code when making comparisons.
     */
    @Parameter(property = "newSha", required = false)
    private String newSha;

    /**
     * Whether to treat all found violations as new, regardless of previous runs.
     */
    @Parameter(property = "firstRun", required = false, defaultValue = "false")
    private boolean firstRun;

    /**
     * Whether to show all violations in the console or only the new violations.
     */
    @Parameter(property = "showAllInConsole", defaultValue = "false")
    private boolean showAllInConsole;

    /**
     * Whether to show all violations in <code>violation-counts</code> or only the new violations.
     */
    @Parameter(property = "showAllInFile", defaultValue = "false")
    private boolean showAllInFile;

    private Path gitDir;
    private Path oldVC;
    private Path newVC;
    private Path lastShaPath;

    // DiffFormatter is used to analyze differences between versions of code including both renames and line insertions
    // and deletions
    private final DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);

    // Map from a file to a map representing the number of additional lines added or deleted at each line
    // of the original file. If the value is 0, the line has been modified in place.
    // Note: If renames are involved, the old name of the file is used.
    // More information about how differences are represented in JGit can be found here:
    // https://archive.eclipse.org/jgit/docs/jgit-2.0.0.201206130900-r/apidocs/org/eclipse/jgit/diff/Edit.html
    //          file        line     lines modified
    private Map<String, Map<Integer, Integer>> offsets = new HashMap<>();

    // Maps files to lines of code in the original version which have not been modified
    // Note: If renames are involved, the old name of the file is used.
    private Map<String, Set<Integer>> modifiedLines = new HashMap<>();

    // Maps renamed files to the original names
    private Map<String, String> renames = new HashMap<>();

    private Set<Violation> oldViolations;
    private Set<Violation> newViolations;

    public void execute() throws MojoExecutionException {
        getLog().info("[eMOP] Invoking the VMS Mojo...");

        gitDir = basedir.toPath().resolve(".git");
        oldVC = Paths.get(getArtifactsDir(), "violation-counts-old");
        newVC = Paths.get(System.getProperty("user.dir"), "violation-counts");
        lastShaPath = Paths.get(getArtifactsDir(), "last-SHA");

        touchVmsFiles();
        oldViolations = Violation.parseViolations(oldVC);
        lastSha = getLastSha(); // will also set firstRun if applicable

        if (!firstRun) {
            findLineChangesAndRenames(getCommitDiffs()); // populates renames, lineChanges, and modifiedLines
            getLog().info("Number of files renamed: " + renames.size());
            getLog().info("Number of changed files found: " + offsets.size());
        }

        invokeSurefire();
        newViolations = Violation.parseViolations(newVC);
        getLog().info("Number of total violations found: " + newViolations.size());

        if (!firstRun) {
            removeDuplicateViolations();
            getLog().info("Number of \"new\" violations found: " + newViolations.size());
        }

        saveViolationCounts();
        if (!showAllInFile) {
            rewriteViolationCounts();
        }
    }

    private static class SurefireOutputHandler implements InvocationOutputHandler {
        private static enum State {
            PROLOGUE,   // Output preceding the surefire:test goal
            BODY,       // Output from surefire:test
            EPILOGUE    // Output following the surefire:test goal
        }

        private static final Pattern epilogueStart = Pattern.compile("\\[INFO\\] BUILD (SUCCESS|FAILURE).*");

        private ViolationFilterer filterer;
        private State state = State.PROLOGUE;
        private boolean filteringCurrentViolation = false;

        public SurefireOutputHandler(ViolationFilterer filterer) {
            this.filterer = filterer;
        }

        @Override
        @SuppressWarnings("checkstyle:Regexp")
        public void consumeLine(String line) {
            switch (state) {
                case PROLOGUE:
                    if (line.contains("maven-surefire-plugin")) {
                        System.out.println(line);
                        state = State.BODY;
                    }
                    break;
                case BODY:
                    if (line.startsWith("Specification")) {
                        if (!(filteringCurrentViolation = filterer.filter(line))) {
                            System.out.println(line);
                        }
                    } else if (epilogueStart.matcher(line).matches()) {
                        state = State.EPILOGUE;
                    } else if (line.startsWith("[INFO]") || !filteringCurrentViolation) {
                        System.out.println(line);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private class ViolationFilterer {
        private final boolean filterOld = !firstRun && !showAllInConsole;

        /**
         * Returns whether to filter the violation.
         *
         * @param violation A string representation of a violation
         * @return <code>true</code> if the violation should be filtered; <code>false</code> otherwise
         */
        private boolean filter(String violation) {
            if (filterOld) {
                Violation newViolation = Violation.parseViolation(violation);
                for (Violation oldViolation : oldViolations) {
                    if (isSameViolationAfterDifferences(oldViolation, newViolation)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    /**
     * Runs Maven Surefire.
     *
     * @throws MojoExecutionException if the Surefire execution fails.
     */
    private void invokeSurefire() throws MojoExecutionException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setGoals(Collections.singletonList("surefire:test"));
        InvocationOutputHandler outputHandler = new SurefireOutputHandler(new ViolationFilterer());
        request.setOutputHandler(outputHandler);
        request.setErrorHandler(outputHandler);

        try {
            Invoker invoker = new DefaultInvoker();
            InvocationResult result = invoker.execute(request);

            if (result.getExitCode() != 0) {
                throw new MojoExecutionException("Surefire reported exit code " + result.getExitCode());
            }
        } catch (MavenInvocationException ex) {
            throw new MojoExecutionException("Failed to execute Surefire", ex);
        }
    }

    /**
     * Fetches the two most recent commits of the repository and finds the differences between them.
     * If the user has specified a lastSha, then that sha will be used as the "old" version of code. It should ideally
     * correspond to
     *
     * @return List of differences between two most recent commits of the repository
     * @throws MojoExecutionException if error is encountered at runtime
     */
    private List<DiffEntry> getCommitDiffs() throws MojoExecutionException {
        ObjectReader objectReader;
        List<DiffEntry> diffs;
        List<AbstractTreeIterator> trees = new ArrayList<>();

        try (Git git = Git.open(gitDir.toFile())) {
            objectReader = git.getRepository().newObjectReader();

            // Set up diffFormatter
            diffFormatter.setRepository(git.getRepository());
            diffFormatter.setContext(0);
            diffFormatter.setDetectRenames(true);

            // Get more recent version of code (either working tree or most recent commit)
            if (newSha == null || newSha.isEmpty()) {
                trees.add(new FileTreeIterator(git.getRepository()));
            } else {
                ObjectId shaId = git.getRepository().resolve(lastSha);
                RevCommit newerCommit = git.getRepository().parseCommit(shaId);
                trees.add(new CanonicalTreeParser(null, objectReader, newerCommit.getTree().getId()));
            }

            // Get older version of code (either from user or file)
            ObjectId shaId = git.getRepository().resolve(lastSha);
            RevCommit olderCommit = git.getRepository().parseCommit(shaId);
            trees.add(new CanonicalTreeParser(null, objectReader, olderCommit.getTree().getId()));
            diffs = diffFormatter.scan(trees.get(1), trees.get(0));
        } catch (IOException exception) {
            throw new MojoExecutionException("Failed to fetch code version");
        }

        return diffs;
    }

    /**
     * Updates the renames, offsets, and modifiedLines based on found differences.
     *
     * @param diffs List of differences between two versions of the same program
     * @throws MojoExecutionException if error is encountered at runtime
     */
    private void findLineChangesAndRenames(List<DiffEntry> diffs) throws MojoExecutionException {
        try {
            for (DiffEntry diff : diffs) {
                // Only consider differences if the files has not just been created or deleted and is in the relevant
                // portion of code (src/main/java) (this file path style matches what is used internally by JGit and
                // is agnostic of the local OS).
                // To read more about how JGit indicates a newly created or deleted file, read here:
                // https://archive.eclipse.org/jgit/docs/jgit-2.0.0.201206130900-r/apidocs/org/eclipse/jgit/diff/DiffEntry.html
                if (!diff.getNewPath().equals(DiffEntry.DEV_NULL) && !diff.getOldPath().equals(DiffEntry.DEV_NULL)
                    && diff.getOldPath().contains("src/main/java/")) {
                    // Gets renamed classes
                    if (!diff.getNewPath().equals(diff.getOldPath())) {
                        renames.put(diff.getNewPath(), diff.getOldPath());
                    }

                    for (Edit edit : diffFormatter.toFileHeader(diff).toEditList()) {
                        // Calculates appropriate beginnings and endings
                        int editBeginning = edit.getBeginA();
                        int editEnding = edit.getEndA();
                        if (edit.getBeginA() != edit.getEndA()) {
                            editBeginning += 1;
                            editEnding += 1;
                        }

                        // Gets offset at each line
                        if (offsets.containsKey(diff.getOldPath())) {
                            Map<Integer, Integer> fileOffsets = offsets.get(diff.getOldPath());
                            if (fileOffsets.containsKey(editBeginning)) {
                                fileOffsets.put(editBeginning, fileOffsets.get(editBeginning)
                                        + edit.getLengthB() - edit.getLengthA());
                            } else {
                                fileOffsets.put(editBeginning, edit.getLengthB() - edit.getLengthA());
                            }
                        } else {
                            Map<Integer, Integer> filesOffsets = new HashMap<>();
                            filesOffsets.put(editBeginning, edit.getLengthB() - edit.getLengthA());
                            offsets.put(diff.getOldPath(), filesOffsets);
                        }

                        // Gets modified lines
                        for (int i = editBeginning; i < editEnding; i++) {
                            if (modifiedLines.containsKey(diff.getOldPath())) {
                                modifiedLines.get(diff.getOldPath()).add(i);
                            } else {
                                Set<Integer> lines = new HashSet<>();
                                lines.add(i);
                                modifiedLines.put(diff.getOldPath(), lines);
                            }
                        }
                    }
                }
            }
        } catch (IOException exception) {
            throw new MojoExecutionException("Encountered an error when comparing different files");
        }
    }

    /**
     * Removes newViolations of violations believed to be duplicates from violation-counts-old.
     */
    private void removeDuplicateViolations() {
        Set<Violation> violationsToRemove = new HashSet<>();
        for (Violation newViolation : newViolations) {
            for (Violation oldViolation : oldViolations) {
                if (isSameViolationAfterDifferences(oldViolation, newViolation)) {
                    violationsToRemove.add(newViolation);
                    break;
                }
            }
        }
        newViolations.removeAll(violationsToRemove);
    }

    /**
     * Determines if an old violation in a class could be mapped to the new violation after accounting for differences
     * in code and renames.
     *
     * @param oldViolation Original violation to compare
     * @param newViolation New violation to compare
     * @return Whether the old violation can be mapped to the new violation, after code changes and renames
     */
    private boolean isSameViolationAfterDifferences(Violation oldViolation, Violation newViolation) {
        return oldViolation.getSpecification().equals(newViolation.getSpecification())
                && (oldViolation.getClassName().equals(newViolation.getClassName())
                    || isRenamed(oldViolation.getClassName(), newViolation.getClassName()))
                && hasSameLineNumber(oldViolation.getClassName(), oldViolation.getLineNum(), newViolation.getLineNum());
    }

    /**
     * Determines whether an old class has been renamed to the new one or not.
     *
     * @param oldClass Previous possible name of a class
     * @param newClass Rename being considered
     * @return Whether the old class name was renamed to the new one
     */
    private boolean isRenamed(String oldClass, String newClass) {
        for (String renamedClass : renames.keySet()) {
            if (renamedClass.contains(newClass) && renames.get(renamedClass).contains(oldClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether an old line in a file can be mapped to the new line. For an old line to be mappable to a new
     * line in a class, it must fulfill two conditions: that the old line has not been modified and that the old line
     * in addition to the net offset of previous edits equals the new line.
     * Take the following example where the second line of code has been modified with a new line inserted directly
     * after it:
     * Old code          New code
     * 1 line A          1 line A
     * 2 line B          2 line B'
     * 3 line C          3 new line
     *                   4 line C
     * The first line of code does not have any edits occurring earlier in the code, and will be matched directly to the
     * first line of the new version of code. The second line has been modified and will not be mapped. The third line
     * of code has an edit occurring earlier in the code with an offset of 1. Therefore, the third line of the old code
     * will be mapped to the fourth line of the new code.
     *
     * @param className Particular class being considered (if the class was renamed, this is the old name)
     * @param oldLine Original line number
     * @param newLine New line number
     * @return Whether the original line number can be mapped to the new line number in the updated version
     */
    private boolean hasSameLineNumber(String className, int oldLine, int newLine) {
        for (String changedClass : offsets.keySet()) {
            if (changedClass.contains(className)) {
                // modified lines are never mapped
                if (modifiedLines.containsKey(changedClass) && modifiedLines.get(changedClass).contains(oldLine)) {
                    return false;
                }
                int netOffset = 0;
                for (Integer offsetLine : offsets.get(changedClass).keySet()) {
                    if (offsetLine < oldLine) {
                        netOffset += offsets.get(changedClass).get(offsetLine);
                    }
                }
                return newLine - oldLine == netOffset;
            }
        }
        return oldLine == newLine;
    }

    /**
     * Rewrites <code>violation-counts</code> to only include violations in newViolations.
     */
    private void rewriteViolationCounts() throws MojoExecutionException {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(newVC);
        } catch (IOException exception) {
            throw new MojoExecutionException("Failure encountered when reading violation-counts");
        }

        try (PrintWriter writer = new PrintWriter(newVC.toFile())) {
            for (String line : lines) {
                if (isNewViolation(line)) {
                    writer.println(line);
                }
            }
        } catch (IOException exception) {
            throw new MojoExecutionException("Failure encountered when writing violation-counts");
        }
    }

    /**
     * Whether a violation line is a new violation.
     *
     * @param violation Violation line being considered
     * @return Whether the violation is a new violation
     */
    private boolean isNewViolation(String violation) {
        if (firstRun) {
            return true;
        }
        Violation parsedViolation = Violation.parseViolation(violation);
        return newViolations.contains(parsedViolation);
    }

    /**
     * Ensures that <code>violation-counts</code> and <code>violation-counts-old</code>
     * both exist, creating empty files if not.
     */
    private void touchVmsFiles() throws MojoExecutionException {
        try {
            oldVC.toFile().createNewFile();
            newVC.toFile().createNewFile();
            lastShaPath.toFile().createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new MojoExecutionException("Failed to create VMS files", ex);
        }
    }

    /**
     * If the working tree is clean, saves the most recent <code>violation-counts</code>
     * created by RV-Monitor in <code>violation-counts-old</code>.
     */
    private void saveViolationCounts() throws MojoExecutionException {
        try (Git git = Git.open(gitDir.toFile())) {
            if (git.status().call().isClean()) {
                Files.copy(newVC, oldVC, StandardCopyOption.REPLACE_EXISTING);

                try (PrintWriter out = new PrintWriter(lastShaPath.toFile())) {
                    out.println(git.getRepository().resolve(Constants.HEAD).name());
                }
            }
        } catch (IOException | GitAPIException ex) {
            ex.printStackTrace();
            throw new MojoExecutionException("Failed to save violation-counts", ex);
        }
    }

    /**
     * Reads and returns the lastSha. That is, the SHA which corresponds to the "old" version of code.
     *
     * @return String of the previous SHA, null if the file is empty
     * @throws MojoExecutionException if error encountered at runtime
     */
    private String getLastSha() throws MojoExecutionException {
        if (lastSha != null && !lastSha.isEmpty()) {
            return lastSha;
        }
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(lastShaPath.toFile()))) {
            String sha = bufferedReader.readLine();
            if (sha == null || sha.isEmpty()) {
                firstRun = true;
            }
            return sha;
        } catch (IOException exception) {
            throw new MojoExecutionException("Error encountered when reading lastSha");
        }
    }
}
