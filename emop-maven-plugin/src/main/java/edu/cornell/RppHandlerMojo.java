package edu.cornell;

import edu.cornell.emop.maven.AgentLoader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "rpp-handler", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class RppHandlerMojo extends AbstractMojo {
    public void execute() throws MojoExecutionException {
        if (!AgentLoader.loadDynamicAgent("JavaAgent.class")) {
            throw new MojoExecutionException("Could not attach agent");
        }
    }
}
