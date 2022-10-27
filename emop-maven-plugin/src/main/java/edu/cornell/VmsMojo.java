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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.cornell.emop.util.Violation;
import edu.illinois.starts.jdeps.DiffMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
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
     * Specific SHA to use when analyzing differences between versions. Should correspond to the previous run of VMS.
     */
    @Parameter(property = "lastSha", required = false)
    private String lastSha;

    /**
     * Whether to use the current working tree as the updated version of code or to use the most recent commit as the
     * updated version of code when making code comparisons.
     */
    @Parameter(property = "useWorkingTree", required = false, defaultValue = "true")
    private boolean useWorkingTree;

    /**
     * Whether to treat all found violations as new, regardless of previous runs.
     */
    @Parameter(property = "firstRun", required = false, defaultValue = "false")
    private boolean firstRun;

    private Path gitDir;
    private Path oldVC;
    private Path newVC;

    private final DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);

    // Represents the map between a file and a map representing the number of lines added or deleted at each line
    // of the file. If the value is 0, the line has been modified in place.
    // Note: If renames are involved, the old name of the file is used.
    //          file        line     lines modified
    private Map<String, Map<Integer, Integer>> lineChanges = new HashMap<>();

    // Represents the renamed file's original name
    private Map<String, String> renames = new HashMap<>();

    // Describes new classes which have been made between commits
    private Set<String> newClasses = new HashSet<>();

    private Set<Violation> oldViolations;
    private Set<Violation> newViolations;

    public void execute() throws MojoExecutionException {
        getLog().info("[eMOP] Invoking the VMS Mojo...");
        lastSha = getLastSha();
        gitDir = basedir.toPath().resolve(".git");
        oldVC = Paths.get(getArtifactsDir(), "violation-counts-old");
        newVC = Paths.get(System.getProperty("user.dir"), "violation-counts");

        touchVmsFiles();

        findLineChangesAndRenames(getDiffs());
        getLog().info("Number of files renamed: " + renames.size());
        getLog().info("Number of changed files found: " + lineChanges.size());
        oldViolations = Violation.parseViolations(oldVC);
        newViolations = Violation.parseViolations(newVC);
        getLog().info("Number of total violations found: " + newViolations.size());
        if (!firstRun) {
            removeDuplicateViolations();
        }
        getLog().info("Number of \"new\" violations found: " + newViolations.size());
        saveViolationCounts();
        rewriteViolationCounts();
    }

    /**
     * Fetches the two most recent commits of the repository and finds the differences between them.
     * TODO: Update description with how the previous version is determined based on user input, lastSha file, and previous commit
     *
     * @return List of differences between two most recent commits of the repository
     * @throws MojoExecutionException if error is encountered during runtime
     */
    private List<DiffEntry> getDiffs() throws MojoExecutionException {
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
            if (useWorkingTree) {
                trees.add(new FileTreeIterator(git.getRepository()));
            } else {
                RevCommit mostRecentCommit = git.log().setMaxCount(1).call().iterator().next();
                trees.add(new CanonicalTreeParser(null, objectReader, mostRecentCommit.getTree().getId()));
            }

            // Get older version of code (either from user or file)
            RevCommit olderCommit;
            if (lastSha != null || !lastSha.isEmpty()) {
                ObjectId shaId = git.getRepository().resolve(lastSha);
                olderCommit = git.getRepository().parseCommit(shaId);
                trees.add(new CanonicalTreeParser(null, objectReader, olderCommit.getTree().getId()));
            } else {
                return null;
            }
            diffs = diffFormatter.scan(trees.get(1), trees.get(0));
        } catch (IOException | GitAPIException exception) {
            throw new MojoExecutionException("Failed to fetch code version");
        }

        return diffs;
    }

    /**
     * Updates the lineChanges and renames based on found differences.
     *
     * @param diffs List of differences between two versions of the same program
     * @throws MojoExecutionException if error is encountered during runtime
     */
    private void findLineChangesAndRenames(List<DiffEntry> diffs) throws MojoExecutionException {
        try {
            for (DiffEntry diff : diffs) {
                if (!diff.getNewPath().equals("/dev/null") && !diff.getOldPath().equals(DiffEntry.DEV_NULL)) {
                    // Gets renamed classes
                    if (!diff.getNewPath().equals(diff.getOldPath())) {
                        renames.put(diff.getNewPath(), diff.getOldPath());
                    }

                    // For each file, find the replacements, additions, and deletions at each line
                    for (Edit edit : diffFormatter.toFileHeader(diff).toEditList()) {
                        getLog().info("Type of diff: " + edit.getType());
                        getLog().info("A starts at: " + (edit.getBeginA() + 1));
                        getLog().info("A ends at: " + (edit.getEndA() + 1));
                        getLog().info("B starts at: " + (edit.getBeginB() + 1));
                        getLog().info("B ends at: " + (edit.getEndB() + 1));
                        getLog().info("Offset: " + (edit.getLengthB() - edit.getLengthA()));
                        Map<Integer, Integer> lineChange = new HashMap<>();
                        if (lineChanges.containsKey(diff.getOldPath())) {
                            lineChange = lineChanges.get(diff.getOldPath());
                        }
                        lineChange.put(edit.getBeginA() + 1, edit.getLengthB() - edit.getLengthA());
                        lineChanges.put(diff.getOldPath(), lineChange);
                    }
                }
            }
        } catch (IOException exception) {
            throw new MojoExecutionException("Encountered an error when comparing different files");
        }
    }

    /**
     * Scrubs newViolations of violations believed to be duplicates from violation-counts-old.
     */
    private void removeDuplicateViolations() {
        Set<Violation> violationsToRemove = new HashSet<>();
        for (Violation newViolation : newViolations) {
            Set<Violation> relevantOldViolations = oldViolations.stream()
                    .filter(oldViolation -> oldViolation.getSpecification().equals(newViolation.getSpecification()))
                    .filter(oldViolation -> oldViolation.getClassInfo().equals(newViolation.getClassInfo())
                                         || isRenamed(oldViolation.getClassInfo(), newViolation.getClassInfo()))
                    .filter(oldViolation -> hasSameLineNumber(oldViolation.getClassInfo(), oldViolation.getLineNum(),
                            newViolation.getLineNum()))
                    .collect(Collectors.toSet());

            if (!relevantOldViolations.isEmpty()) {
                violationsToRemove.add(newViolation);
            }
        }
        for (Violation violationToRemove : violationsToRemove) {
            newViolations.remove(violationToRemove);
        }
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
     * Determines whether an old line in a file can be mapped to the new line. There's room for optimization.
     *
     * @param classInfo Particular class being considered (if the class was renamed, this is the old name)
     * @param oldLine Original line number
     * @param newLine New line number
     * @return Whether the original line number can be mapped to the new line number in the updated version
     */
    private boolean hasSameLineNumber(String classInfo, int oldLine, int newLine) {
        for (String className : lineChanges.keySet()) {
            if (className.contains(classInfo)) {
                int offset = 0;
                for (Integer originalLine : lineChanges.get(className).keySet()) {
                    if (originalLine <= oldLine) {
                        offset += lineChanges.get(className).get(originalLine);
                    }
                }
                if (newLine >= oldLine) { // if lines have been inserted
                    return offset >= 0 && offset >= newLine - oldLine;
                } else { // if lines have been removed
                    return offset <= 0 && offset <= newLine - oldLine;
                }
            }
        }
        return oldLine == newLine;
    }

    /**
     * Rewrites <code>violation-counts</code> to only include violations in newViolations.
     */
    private void rewriteViolationCounts() throws MojoExecutionException {
        // for each line of violation-counts, if it can be mapped to a new violation it gets to stay (else it goes)
        try {
            List<String> lines = Files.readAllLines(newVC);
            PrintWriter writer = new PrintWriter(newVC.toFile());
            for (String line : lines) {
                if (isNewViolation(line)) {
                    writer.println(line);
                }
            }
            writer.close();
        } catch (IOException exception) {
            throw new MojoExecutionException("Failure encountered when rewriting violation-counts");
        }
    }

    /**
     * Whether a violation line is a new violation.
     *
     * @param violation Violation line being considered
     * @return Whether the violation is a new violation
     */
    private boolean isNewViolation(String violation) {
        Violation parsedViolation = Violation.parseViolation(violation);
        for (Violation newViolation : newViolations) {
            if (newViolation.equals(parsedViolation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ensures that <code>violation-counts</code> and <code>violation-counts-old</code>
     * both exist, creating empty files if not.
     */
    private void touchVmsFiles() throws MojoExecutionException {
        try {
            oldVC.toFile().createNewFile();
            newVC.toFile().createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new MojoExecutionException("Failed to create violation-counts", ex);
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

                try (PrintWriter out = new PrintWriter(Paths.get(getArtifactsDir(), "last-SHA").toFile())) {
                    out.println(git.getRepository().resolve(Constants.HEAD).name());
                }
            }
        } catch (IOException | GitAPIException ex) {
            ex.printStackTrace();
            throw new MojoExecutionException("Failed to save violation-counts", ex);
        }
    }

    /**
     * Reads and returns the last SHA VMS has been run on from file.
     *
     * @return String of the previous SHA, null if the file is empty
     * @throws MojoExecutionException if error encountered at runtime
     */
    private String getLastSha() throws MojoExecutionException {
        if (lastSha != null && !lastSha.isEmpty()) {
            return lastSha;
        }
        Path lastShaPath = Paths.get(getArtifactsDir(), "last-SHA");
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(lastShaPath.toFile()))) {
            return bufferedReader.readLine();
        } catch (IOException exception) {
            throw new MojoExecutionException("Error encountered when reading lastSha");
        }
    }
}
