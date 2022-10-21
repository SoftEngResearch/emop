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
public class RppHandlerMojo extends MonitorMojo {

    @Parameter(property = "criticalSpecsFile", defaultValue = "")
    private String criticalSpecsFile;

    @Parameter(property = "backgroundSpecsFile", defaultValue = "")
    private String backgroundSpecsFile;

    @Parameter(property = "javamopAgent")
    protected String javamopAgent;

    Set<String> criticalSpecsSet;
    Set<String> backgroundSpecsSet;

    File metaInfoDirectory;

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
        if (criticalSpecsFile == null && backgroundSpecsFile == null) {
            // if the user didn't provide any
            File autogenCriticalSpecs = new File(metaInfoDirectory, "rpp-critical-specs.txt");
            File autogenBackgroundSpecs = new File(metaInfoDirectory, "rpp-background-specs.txt");
            if (autogenCriticalSpecs.exists()) {
                criticalSpecsFile = autogenCriticalSpecs.getAbsolutePath();
            }
            if (autogenBackgroundSpecs.exists()) {
                backgroundSpecsFile = autogenBackgroundSpecs.getAbsolutePath();
            }
        }
    }

    private void computeSpecSets() {
        Set<String> allSpecs = Util.retrieveSpecListFromJar(javamopAgent);
        // if we still don't have any spec files, then we'd just need to obtain all specs and run it in critical
        if (criticalSpecsFile == null && backgroundSpecsFile == null) {
            criticalSpecsSet = allSpecs;
            backgroundSpecsSet = new HashSet<>();
        } else if (backgroundSpecsFile == null) {
            // if we only have the critical specs, then our background specs is the set difference
            criticalSpecsSet = parseSpecsFile(criticalSpecsFile);
            backgroundSpecsSet = Sets.difference(allSpecs, criticalSpecsSet);
        } else if (criticalSpecsFile == null) {
            // if we only have the background specs, then our critical specs is the set difference
            backgroundSpecsSet = parseSpecsFile(backgroundSpecsFile);
            criticalSpecsSet = Sets.difference(allSpecs, backgroundSpecsSet);
        } else {
            // we have both files, so all we need to do is read from them.
            criticalSpecsSet = parseSpecsFile(criticalSpecsFile);
            backgroundSpecsSet = parseSpecsFile(backgroundSpecsFile);
            if (criticalSpecsSet.isEmpty() && backgroundSpecsSet.isEmpty()) {
                getLog().error("Both critical sets and background sets were empty!");
                System.exit(1);
            }
        }
    }

    private String setUpSingleJar(String mode, Set<String> specsToMonitor) {
        if (specsToMonitor.isEmpty()) {
            return "";
        } else {
            File javamopAgentFile = new File(javamopAgent);
            File createdJar = new File(metaInfoDirectory, mode + "-javamop.jar");
            try {
                Files.copy(javamopAgentFile.toPath(), createdJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Util.generateNewMonitorFile(
                        metaInfoDirectory + File.separator + mode + "-ajc.xml", specsToMonitor);
                Util.replaceFileInJar(createdJar.getAbsolutePath(), "/META-INF/aop-ajc.xml",
                        metaInfoDirectory + File.separator + mode + "-ajc.xml");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (MojoExecutionException e) {
                throw new RuntimeException(e);
            }
            return createdJar.getAbsolutePath();
        }
    }

    private void setupJars() {
        if (javamopAgent == null) {
            javamopAgent = getLocalRepository().getBasedir() + File.separator + "javamop-agent"
                    + File.separator + "javamop-agent"
                    + File.separator + "1.0"
                    + File.separator + "javamop-agent-1.0.jar";
        }
        setupSpecFiles();
        computeSpecSets();
        String criticalRunAgentPath = setUpSingleJar("critical", criticalSpecsSet);
        String backgroundRunAgentPath = setUpSingleJar("background", backgroundSpecsSet);
        if (!criticalRunAgentPath.isEmpty()) {
            System.setProperty("rpp-agent", criticalRunAgentPath);
            System.setProperty("background-agent", backgroundRunAgentPath);
        } else {
            getLog().info("Critical phase had no specs, skipping and running background phase...");
            System.setProperty("rpp-agent", backgroundRunAgentPath);
        }
    }

    public void execute() throws MojoExecutionException {
        System.out.println("criticalSpecsFile: " + criticalSpecsFile);
        System.out.println("backgroundSpecsFile: " + backgroundSpecsFile);
        metaInfoDirectory = new File(getArtifactsDir());
        // prepare the two jars
        setupJars();
        // record path to jars in system properties
        if (!AgentLoader.loadDynamicAgent("JavaAgent.class")) {
            throw new MojoExecutionException("Could not attach agent");
        }
    }
}