package edu.cornell;

import com.google.common.collect.Sets;
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
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mojo(name = "rpp-handler", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class RppHandlerMojo extends AbstractMojo {

    @Parameter(property = "criticalSpecsFile", defaultValue = "")
    protected String criticalSpecsFile;

    @Parameter(property = "backgroundSpecsFile", defaultValue = "")
    protected String backgroundSpecsFile;

    @Parameter(property = "javamopAgent")
    protected String javamopAgent;

    @Parameter( defaultValue = "${localRepository}", required = true, readonly = true )
    protected ArtifactRepository localRepository;

    protected File criticalRunJavaMopJar;
    protected File backgroundRunJavaMopJar;

    protected Set<String> parseSpecsFile(String specsFilePath) {
        try {
            // FIXME: clean this up
            return new HashSet<>(Files.readAllLines(new File(specsFilePath).toPath()))
                    .stream()
                    .map(spec -> spec.endsWith("MonitorAspect") ? spec : spec + "MonitorAspect").collect(Collectors.toSet());
        } catch (IOException e) {
            return new HashSet<>();
        }
    }

    private void setupSpecFiles() {
        if (criticalSpecsFile.isEmpty() && backgroundSpecsFile.isEmpty()) {
            // if the user didn't provide any
            String baseDir = System.getProperty("basedir");
            File startsDir = new File(baseDir + File.separator + ".starts");
            File autogenCriticalSpecs = new File(startsDir, "rpp-critical-specs.txt");
            File autogenBackgroundSpecs = new File(startsDir, "rpp-background-specs.txt");
            if (autogenCriticalSpecs.exists()) {
                criticalSpecsFile = autogenCriticalSpecs.getAbsolutePath();
            }
            if (autogenBackgroundSpecs.exists()) {
                backgroundSpecsFile = autogenBackgroundSpecs.getAbsolutePath();
            }
        }
    }

    Set<String> criticalSpecsSet;
    Set<String> backgroundSpecsSet;

    private void computeSpecSets() {
        Set<String> allSpecs = Util.retrieveSpecListFromJar(javamopAgent);
        // if we still don't have any spec files, then we'd just need to obtain all specs and run it in critical
        if (criticalSpecsFile.isEmpty() && backgroundSpecsFile.isEmpty()) {
            criticalSpecsSet = allSpecs;
            backgroundSpecsSet = new HashSet<>();
        } else if (backgroundSpecsFile.isEmpty()) {
            // if we only have the critical specs, then our background specs is the set difference
            criticalSpecsSet = parseSpecsFile(criticalSpecsFile);
            backgroundSpecsSet = Sets.difference(allSpecs, criticalSpecsSet);
        } else if (criticalSpecsFile.isEmpty()) {
            // if we only have the background specs, then our critical specs is the set difference
            backgroundSpecsSet = parseSpecsFile(backgroundSpecsFile);
            criticalSpecsSet = Sets.difference(allSpecs, backgroundSpecsSet);
        } else {
            // we have both files, so all we need to do is read from them.
            criticalSpecsSet = parseSpecsFile(criticalSpecsFile);
            backgroundSpecsSet = parseSpecsFile(backgroundSpecsFile);
        }
    }

    private String setUpSingleJar(String mode, Set<String> specsToMonitor) {
        File javamopAgentFile = new File(javamopAgent);
        File createdJar = new File(javamopAgentFile.getParentFile(), mode + "-javamop.jar");
        try {
            Files.copy(javamopAgentFile.toPath(), createdJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Util.generateNewMonitorFile(
                    System.getProperty("user.dir") + File.separator + mode + "-ajc.xml", specsToMonitor);
            Util.replaceFileInJar(createdJar.getAbsolutePath(), "/META-INF/aop-ajc.xml",
                    System.getProperty("user.dir") + File.separator + mode + "-ajc.xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (MojoExecutionException e) {
            throw new RuntimeException(e);
        }
        return createdJar.getAbsolutePath();
    }

    private void setupJars() {
        setupSpecFiles();
        computeSpecSets();
        if (javamopAgent == null) {
            javamopAgent = localRepository.getBasedir() + File.separator + "javamop-agent"
                    + File.separator + "javamop-agent"
                    + File.separator + "1.0"
                    + File.separator + "javamop-agent-1.0.jar";
        }
        setUpSingleJar("critical", criticalSpecsSet);
        setUpSingleJar("background", backgroundSpecsSet);
    }
    public void execute() throws MojoExecutionException {
        // prepare the two jars

        System.setProperty("rpp-agent", this.criticalRunJavaMopJar.getAbsolutePath());
        // record path to jars in system properties
        if (!AgentLoader.loadDynamicAgent("JavaAgent.class")) {
            throw new MojoExecutionException("Could not attach agent");
        }
    }
}
