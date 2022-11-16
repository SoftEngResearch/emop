package edu.cornell;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "rps", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "rps")
public class RpsMojo extends MonitorMojo {
    public void execute() throws MojoExecutionException {
        getLog().info("[eMOP] Invoking the RPS Mojo...");
        System.setProperty("exiting-rps", "false");
    }
}
