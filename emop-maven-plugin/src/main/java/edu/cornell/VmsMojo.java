package edu.cornell;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * The URI of the GitHub repository VMS is to be performed on
     */
    @Parameter(property = "repo")
    private String repo;

    public void execute() throws MojoExecutionException {
        getLog().info("[eMOP] Invoking the VMS Mojo...");
        if (repo == null) {
            throw new MojoExecutionException("Valid GitHub repository necessary for VMS. Please use the `repo` option");
        }
        List<DiffEntry> diffs = getDiffs();
        findLineChangesAndRenames(diffs);
        getLog().info("Found renames:\n" + renames.toString());
        getLog().info("Found line changes:\n" + lineChanges.toString());
    }

    /**
     * Fetches the two most recent commits of the repository and finds the differences between them
     *
     * @return List of differences between two most recent commits of the repository
     * @throws MojoExecutionException
     */
    private List<DiffEntry> getDiffs() throws MojoExecutionException {
        Git git;
        Iterable<RevCommit> commits;
        ObjectReader objectReader;
        List<DiffEntry> diffs;

        // Sets up repository and fetches commits
        try {
            git = Git.cloneRepository()
                    .setURI(repo)
                    .call();
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
        diffFormatter.setDetectRenames(true);
        try {
            diffs = diffFormatter.scan(trees.get(0), trees.get(1));
        } catch (IOException e) {
            throw new MojoExecutionException("Encountered an error when analyzing for differences between commits");
        }

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
                // If the paths of the found change is different, the file has been renamed
                if (!diff.getOldPath().equals(diff.getNewPath())) {
                    renames.put(diff.getOldPath(), diff.getNewPath());
                }
                // For each file, find the replacements, additions, and deletions at each line
                for (Edit edit : diffFormatter.toFileHeader(diff).toEditList()) {
                    Map<Integer, Integer> lineChange = new HashMap<>();
                    if (lineChanges.containsKey(diff.getOldPath())) {
                        lineChange = lineChanges.get(diff.getOldPath());
                    }
                    lineChange.put(edit.getBeginA(), edit.getEndB() - edit.getEndA());
                    lineChanges.put(diff.getOldPath(), lineChange);
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Encountered an error when comparing different files");
        }
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
}
