package edu.cornell;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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
                getLog().info("Diff Change Type: " + diff.getChangeType().toString());
                getLog().info("Diff Change Type: " + diff.;
                outputStream.reset();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
