package edu.cornell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.eclipse.jgit.util.io.NullOutputStream;

@Mojo(name = "vms", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "vms")
public class VmsMojo extends MonitorMojo {
    public void execute() throws MojoExecutionException {
        getLog().info("[eMOP] Invoking the VMS Mojo...");
        getLog().info("I think the artifacts directory is here:" + getArtifactsDir());
        try {
            Git git = Git.cloneRepository()
                    .setURI("https://github.com/apache/commons-fileupload.git")
                    .call();

            ObjectReader objectReader = git.getRepository().newObjectReader();
            Iterable<RevCommit> commits = git.log().setMaxCount(2).call();

            List<CanonicalTreeParser> trees = new ArrayList<>();
            for (RevCommit commit : commits) {
                trees.add(new CanonicalTreeParser(null, objectReader, commit.getTree().getId()));
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DiffFormatter diffFormatter = new DiffFormatter(outputStream);
            diffFormatter.setRepository(git.getRepository());
            diffFormatter.setDetectRenames(true);
            List<DiffEntry> diffs = diffFormatter.scan(trees.get(0), trees.get(1));

            for(DiffEntry diff : diffs) {
                diffFormatter.format(diff);
                String diffText = outputStream.toString("UTF-8");
                getLog().info("Diff: " + diffText);
                outputStream.reset();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
