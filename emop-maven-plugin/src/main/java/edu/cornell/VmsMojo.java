package edu.cornell;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

@Mojo(name = "vms", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "vms")
public class VmsMojo extends MonitorMojo {
    public void execute() throws MojoExecutionException {
        getLog().info("[eMOP] Invoking the VMS Mojo...");
        getLog().info("I think the artifacts directory is here:" + getArtifactsDir());
        Git git = null;
    }
}
