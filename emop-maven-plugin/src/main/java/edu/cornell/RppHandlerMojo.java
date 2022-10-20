package edu.cornell;

import edu.cornell.emop.maven.AgentLoader;
import edu.cornell.emop.util.Util;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.surefire.AbstractSurefireMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mojo(name = "rpp-handler", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class RppHandlerMojo extends AbstractMojo {

    @Parameter(property = "criticalSpecsFile", defaultValue = "")
    private String criticalSpecsFile;

    @Parameter(property = "backgroundSpecsFile", defaultValue = "")
    private String backgroundSpecsFile;

    @Parameter(property = "javamopAgent")
    private String javamopAgent;

    @Parameter( defaultValue = "${localRepository}", required = true, readonly = true )
    private ArtifactRepository localRepository;

    protected File criticalRunJavaMop;
    protected File backgroundRunJavaMop;

    private Set<String> parseSpecsFile(String specsFilePath) {
        try {
            // FIXME: clean this up
            return new HashSet<>(Files.readAllLines(new File(specsFilePath).toPath()))
                    .stream()
                    .map(spec -> spec.endsWith("MonitorAspect") ? spec : spec + "MonitorAspect").collect(Collectors.toSet());
        } catch (IOException e) {
            return new HashSet<>();
        }
    }

    public void execute() throws MojoExecutionException {
        // prepare the two jars
        if (javamopAgent == null) {
            javamopAgent = localRepository.getBasedir() + File.separator + "javamop-agent"
                    + File.separator + "javamop-agent"
                    + File.separator + "1.0"
                    + File.separator + "javamop-agent-1.0.jar";
        }
        File javamopAgentFile = new File(javamopAgent);
        this.criticalRunJavaMop = new File(javamopAgentFile.getParentFile(), "critical-javamop.jar");
        try {
            Files.copy(javamopAgentFile.toPath(), criticalRunJavaMop.toPath());
            Set<String> criticalSpecs = parseSpecsFile(criticalSpecsFile);
            Util.generateNewMonitorFile(
                    System.getProperty("user.dir") + File.separator + "critical-ajc.xml", criticalSpecs);
            Util.replaceFileInJar(this.criticalRunJavaMop.getAbsolutePath(), "/META-INF/aop-ajc.xml",
                    System.getProperty("user.dir") + File.separator + "critical-ajc.xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.backgroundRunJavaMop = new File(javamopAgentFile.getParentFile(), "background-javamop.jar");
        try {
            Files.copy(javamopAgentFile.toPath(), backgroundRunJavaMop.toPath());
            Set<String> backgroundSpecs = parseSpecsFile(backgroundSpecsFile);
            Util.generateNewMonitorFile(
                    System.getProperty("user.dir") + File.separator + "background-ajc.xml", backgroundSpecs);
            Util.replaceFileInJar(this.backgroundRunJavaMop.getAbsolutePath(), "/META-INF/aop-ajc.xml",
                    System.getProperty("user.dir") + File.separator + "background-ajc.xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.setProperty("rpp-agent", this.criticalRunJavaMop.getAbsolutePath());
        // record path to jars in system properties
        if (!AgentLoader.loadDynamicAgent("JavaAgent.class")) {
            throw new MojoExecutionException("Could not attach agent");
        }
    }
}
