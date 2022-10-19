package edu.cornell;

import edu.cornell.emop.maven.AgentLoader;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.AbstractSurefireMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;

@Mojo(name = "rpp-handler", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class RppHandlerMojo extends AbstractMojo {

    @Parameter(property = "criticalSpecsFile", defaultValue = "")
    private String criticalSpecsFile;

    @Parameter(property = "javamopAgent")
    private String javamopAgent;

    @Parameter( defaultValue = "${localRepository}", required = true, readonly = true )
    private ArtifactRepository localRepository;


    public void execute() throws MojoExecutionException {
        // prepare the two jars
        if (javamopAgent == null) {
            javamopAgent = localRepository.getBasedir() + File.separator + "javamop-agent"
                    + File.separator + "javamop-agent"
                    + File.separator + "1.0"
                    + File.separator + "javamop-agent-1.0.jar";
        }
        System.out.println("JavaMOP agent: " + javamopAgent);
        // record path to jars in system properties
        if (!AgentLoader.loadDynamicAgent("JavaAgent.class")) {
            throw new MojoExecutionException("Could not attach agent");
        }
    }
}
