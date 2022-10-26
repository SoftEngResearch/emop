package edu.cornell;

import java.io.File;
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
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;

@Mojo(name = "vms", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "vms")
public class VmsMojo extends DiffMojo {

    // DiffFormatter is used to analyze differences between versions of code including both renames and line insertions
    // and deletions
    private final DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);

    // Map from a file to a map representing the number of additional lines added or deleted at each line
    // of the original file. If the value is 0, the line has been modified in place.
    // Note: If renames are involved, the old name of the file is used.
    // More information about how differences are represented in JGit can be found here:
    // https://archive.eclipse.org/jgit/docs/jgit-2.0.0.201206130900-r/apidocs/org/eclipse/jgit/diff/Edit.html
    //          file        line     lines modified
    private Map<String, Map<Integer, Integer>> lineChanges = new HashMap<>();

    // Maps renamed files to the original names
    private Map<String, String> renames = new HashMap<>();

    // Contains new classes which have been made between commits
    private Set<String> newClasses = new HashSet<>();

    private Set<Violation> oldViolations;
    private Set<Violation> newViolations;

    public void execute() throws MojoExecutionException {
        getLog().info("[eMOP] Invoking the VMS Mojo...");
        saveViolationCounts();
        findLineChangesAndRenames(getCommitDiffs());
        getLog().info("Number of files renamed: " + renames.size());
        getLog().info("Number of changed files found: " + lineChanges.size());
        oldViolations = Violation.parseViolations(getArtifactsDir() + File.separator + "violation-counts-old");
        newViolations = Violation.parseViolations(getArtifactsDir() + File.separator + "violation-counts");
        getLog().info("Number of total violations found: " + newViolations.size());
        removeDuplicateViolations();
        getLog().info("Number of \"new\" violations found: " + newViolations.size());
        rewriteViolationCounts();
    }

    /**
     * Fetches the two most recent commits of the repository and finds the differences between them.
     *
     * @return List of differences between two most recent commits of the repository
     * @throws MojoExecutionException if error is encountered at runtime
     */
    private List<DiffEntry> getCommitDiffs() throws MojoExecutionException {
        Git git;
        Iterable<RevCommit> commits;
        ObjectReader objectReader;
        List<DiffEntry> diffs;

        // Sets up repository and fetches commits
        try {
            git = Git.open(basedir.toPath().resolve(".git").toFile());
            commits = git.log().setMaxCount(1).call();
            objectReader = git.getRepository().newObjectReader();
        } catch (GitAPIException | IOException exception) {
            throw new MojoExecutionException("Failed to fetch two previous commits from repository");
        }

        // Creates trees to parse through to analyze for differences
        List<AbstractTreeIterator> trees = new ArrayList<>();
        trees.add(new FileTreeIterator(git.getRepository()));
        try {
            for (RevCommit commit : commits) {
                trees.add(new CanonicalTreeParser(null, objectReader, commit.getTree().getId()));
            }
        } catch (IOException exception) {
            throw new MojoExecutionException("Encountered an error when creating trees from commits");
        }

        // Sets up diffFormatter and analyzes for differences between the two trees
        diffFormatter.setRepository(git.getRepository());
        diffFormatter.setContext(0);
        diffFormatter.setDetectRenames(true);
        try {
            diffs = diffFormatter.scan(trees.get(1), trees.get(0));
        } catch (IOException exception) {
            throw new MojoExecutionException("Encountered an error when analyzing for differences between commits");
        }

        git.close();
        return diffs;
    }

    /**
     * Updates the lineChanges and renames based on found differences.
     *
     * @param diffs List of differences between two versions of the same program
     * @throws MojoExecutionException if error is encountered at runtime
     */
    private void findLineChangesAndRenames(List<DiffEntry> diffs) throws MojoExecutionException {
        try {
            for (DiffEntry diff : diffs) {
                // Determines if the file is new, read more here: https://archive.eclipse.org/jgit/docs/jgit-2.0.0.201206130900-r/apidocs/org/eclipse/jgit/diff/DiffEntry.html
                if (diff.getOldPath().equals(DiffEntry.DEV_NULL)) {
                    newClasses.add(diff.getNewPath());
                } else if (!diff.getNewPath().equals(DiffEntry.DEV_NULL)) { // Ignore if a deleted class
                    // Gets renamed classes
                    if (!diff.getOldPath().equals(diff.getNewPath())) {
                        renames.put(diff.getNewPath(), diff.getOldPath());
                    }

                    // For each file, find the replacements, additions, and deletions at each line
                    for (Edit edit : diffFormatter.toFileHeader(diff).toEditList()) {
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
     * Determines whether an old line in a file can be mapped to the new line. An offset is calculated which determines
     * the maximum boundary of the distance a line has moved based off of differences between code versions. If two
     * lines are within this offset of each other (in the correct direction), they are considered mappable.
     * Take the following example:
     * Old code          New code
     * line 1            line 1
     * line 2            line 2
     * line 3            new line
     * line 4            line 3
     *                   line 4
     * Lines 1 and 2 do not see any differences, so their offset is 0 and they will only map to lines 1 and 2 in the new
     * code, respectively. Line 3 sees an addition of one line, so the offset here is 1 and both the new line and line 3
     * in the new code will map to the previous line 3. Similarly, line 4 also takes the previous addition of a line
     * into account and have an offset of 1, and both lines 3 and 4 in the new code will map to the previous line 4.
     * Deletions work similarly, and are represented by a negative offset.
     * TODO: The current implementation is generous with finding the "same" violation. The offset is very accurate in
     *  finding the precise location unmodified code has been moved to. This information can be leveraged for more
     *  accuracy when differentiating which violations are new or old when they occur close together and the line
     *  itself where the violation occurred is not a part of any direct differences in code. There are also errors that
     *  can arise because of differences between the last commit and the previous run of violation-counts (whose
     *  violations we keep track of) this can be fixed by incorporating sha information.
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
     * Rewrites violation-counts to only include violations in newViolations.
     */
    private void rewriteViolationCounts() throws MojoExecutionException {
        try {
            Path vc = Paths.get(System.getProperty("user.dir"), "violation-counts");
            List<String> lines = Files.readAllLines(vc);
            PrintWriter writer = new PrintWriter(vc.toFile());
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
     * Saves the most recent <code>violation-counts</code> created by RV-Monitor
     * into the artifacts directory, and backs up the previously saved violations
     * to <code>violation-counts-old</code>.
     */
    private void saveViolationCounts() throws MojoExecutionException {
        Path savedVC = Paths.get(getArtifactsDir(), "violation-counts");
        Path savedVCOld = Paths.get(getArtifactsDir(), "violation-counts-old");
        Path newVC = Paths.get(System.getProperty("user.dir"), "violation-counts");

        try {
            getLog().info("Saving previous violation-counts to violation-counts-old...");
            savedVC.toFile().createNewFile();
            Files.move(savedVC, savedVCOld, StandardCopyOption.REPLACE_EXISTING);

            getLog().info("Saving current violation-counts to violation-counts...");
            newVC.toFile().createNewFile();
            Files.copy(newVC, savedVC);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new MojoExecutionException("Failed to save violation-counts", ex);
        }
    }
}
