package edu.cornell;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "rps-rpp", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "rps-rpp")
public class RpsRppMojo extends MonitorMojo {
    public void execute() throws MojoExecutionException {
        getLog().info("[eMOP] Invoking the RPS-RPP Mojo...");
    }
}
