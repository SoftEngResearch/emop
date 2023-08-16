package edu.cornell;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "mm", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "mmk")
public class RpsMethodsMojo extends MonitorMethodsMojo {
    public void execute() throws MojoExecutionException {
        getLog().info("[eMOP] Invoking the RPS Mojo...");
        System.setProperty("exiting-rps", "false");
    }
}
