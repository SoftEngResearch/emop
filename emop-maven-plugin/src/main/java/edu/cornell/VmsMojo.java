package edu.cornell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

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
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

@Mojo(name = "vms", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "vms")
public class VmsMojo extends MonitorMojo {

    private final DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);

    // Represents the map between a file and a map representing the number of lines added or deleted at each line
    // of the file. If the value is 0, the line has been modified in place.
    // Note: If renames are involved, the old name of the file is used.
    //          file        line     lines modified
    private Map<String, Map<Integer, Integer>> lineChanges = new HashMap<>();

    // Represents the renaming of one file to another
    private Map<String, String> renames = new HashMap<>();

    // Describes new classes which have been made between commits
    private Set<String> newClasses = new HashSet<>();

    private List<List<String>> oldViolations;
    private List<List<String>> newViolations;

    public void execute() throws MojoExecutionException {
        getLog().info("[eMOP] Invoking the VMS Mojo...");
        saveViolationCounts();
        List<DiffEntry> diffs = null;
        try {
            diffs = getDiffs();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        findLineChangesAndRenames(diffs);
        getLog().info("Found renames: " + renames.toString());
        getLog().info("Found line changes: " + lineChanges.toString());
        getLog().info("New classes: " + newClasses.toString());
//        oldViolations = parseViolations(...);
//        newViolations = parseViolations(...);
        removeDuplicateViolations();
        rewriteViolationCounts();
    }

    /**
     * Fetches the two most recent commits of the repository and finds the differences between them
     *
     * @return List of differences between two most recent commits of the repository
     * @throws MojoExecutionException
     */
    private List<DiffEntry> getDiffs() throws MojoExecutionException, IOException {
        Git git;
        Iterable<RevCommit> commits;
        ObjectReader objectReader;
        List<DiffEntry> diffs;

        // Sets up repository and fetches commits
        try {
            git = Git.open(new File(".git"));
            commits = git.log().setMaxCount(2).call();
            objectReader = git.getRepository().newObjectReader();
        } catch (GitAPIException e) {
            throw new MojoExecutionException("Failed to fetch two previous commits from repository");
        }

        // Creates trees to parse through to analyze for differences
        List<CanonicalTreeParser> trees = new ArrayList<>();
        try {
            for (RevCommit commit : commits) {
                trees.add(new CanonicalTreeParser(null, objectReader, commit.getTree().getId()));
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Encountered an error when creating trees from commits");
        }

        // Sets up diffFormatter and analyzes for differences between the two trees
        diffFormatter.setRepository(git.getRepository());
        diffFormatter.setContext(0);
        diffFormatter.setDetectRenames(true);
        try {
            diffs = diffFormatter.scan(trees.get(1), trees.get(0));
        } catch (IOException e) {
            throw new MojoExecutionException("Encountered an error when analyzing for differences between commits");
        }

        git.close();
        return diffs;
    }

    /**
     * Updates the lineChanges and renames based on found differences
     *
     * @param diffs List of differences between two versions of the same program
     * @throws MojoExecutionException
     */
    private void findLineChangesAndRenames(List<DiffEntry> diffs) throws MojoExecutionException {
        try {
            for (DiffEntry diff : diffs) {
                // If the old path is /dev/null, then the file has been created between commits
                if (diff.getOldPath().equals("/dev/null")) {
                    newClasses.add(diff.getNewPath());
                }

                // Ignore if a deleted class
                else if(!diff.getNewPath().equals("/dev/null")) {
                    // Gets renamed classes
                    if (!diff.getOldPath().equals(diff.getNewPath())) {
                        renames.put(diff.getOldPath(), diff.getNewPath());
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
        } catch (IOException e) {
            throw new MojoExecutionException("Encountered an error when comparing different files");
        }
    }

    /**
     * Analyzes a violations file and returns a list of violations
     *
     * @param violations The file where violations are located
     * @return A list of violations, each violation is made up of a specification, a class, and a line number
     */
    private List<List<String>> parseViolations(File violations) {
        return null;
    }

    /**
     * Scrubs newViolations of violations believed to be duplicates from violation-counts-old
     */
    private void removeDuplicateViolations() {
        // for each new violation, get all old violations of the same specification
        // then, filter the old violations for those of the same class (take renames into account)
        // fetch the linechanges associated with the (old) class
        // sort linechanges by line number, add or subtract as necessary to determine if the line number of the old
        //     violation is the same as the new
        // if so, remove the new violation
    }

    /**
     * Rewrites violation-counts to only include violations in newViolations
     */
    private void rewriteViolationCounts() {

    }

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
            Files.move(newVC, savedVC);
        }
        catch (IOException ex) {
            ex.printStackTrace();
            throw new MojoExecutionException("Failed to save violation-counts", ex);
        }
    }

    /**
     * If a class is written in p1.p2.p3.java format, convert to p1/p2/p3.java and vice versa
     *
     * @param oldClass Name of class to be reformatted
     * @return Reformatted name of the class
     */
    private String changeClassFormat(String oldClass) {
        return null;
    }
}
