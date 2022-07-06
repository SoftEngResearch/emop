package edu.cornell;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "emop", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "emop")
public class EmopMojo extends MonitorMojo {
    public void execute() throws MojoExecutionException {
        getLog().info("[eMOP] Invoking the EMOP Mojo...");
    }
}
