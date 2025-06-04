package edu.cornell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cornell.emop.util.Util;
import edu.illinois.starts.enums.Granularity;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.aspectj.bridge.IMessage;

@Mojo(name = "rps", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "rps")
public class RpsMojo extends MonitorMojo {

    private static final int CLASS_INDEX_IN_MSG = 3;
    private static final int TRIMMED_SPEC_NAME_INDEX = 4;
    private static final int SPEC_INDEX_IN_MSG = 5;
    private static final String ASPECTJ_WEAVING_FILE = "aspectj-weaving-message.log";

    protected Map<String, Set<String>> classToSpecs = new HashMap<>();
    private Map<String, Set<String>> changedMap = new HashMap<>();

    public void execute() throws MojoExecutionException {
        long start = System.currentTimeMillis();
        getLog().info("[eMOP] Invoking the RPS Mojo...");
        System.setProperty("exiting-rps", "false");

        Path ajcLog = Paths.get(getArtifactsDir() + File.separator + ASPECTJ_WEAVING_FILE);
        if (Files.exists(ajcLog)) {
            getLog().info("AspectJ weaving log found: " + ajcLog.toString());
            classToSpecs = Util.readMapFromFile(getArtifactsDir(), "classToSpecs.bin");
            computeMapFromMessage(ajcLog);
            changedMap.forEach((key, value) -> classToSpecs.merge(key, value, (oldValue, newValue) -> newValue));

            try (FileOutputStream fos
                         = new FileOutputStream(getArtifactsDir() + File.separator + "classToSpecs.bin");
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(classToSpecs);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            try {
                Files.delete(ajcLog);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        long end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Compute affected specs from log takes " + (end - start) + " ms");
    }

    private void computeMapFromMessage(Path ajcLog) throws MojoExecutionException {
        String[] ms;
        try {
            ms = Files.lines(ajcLog).toArray(String[]::new);
        } catch (IOException e) {
            throw new MojoExecutionException("Error reading ajcLog file", e);
        }

        int i = 0;
        if (getGranularity() == Granularity.CLASS || getGranularity() == Granularity.FINE || !finerSpecMapping) {
            for (String message : ms) {
                if (!message.contains("weaveinfo Join point")) {
                    continue;
                }

                String[] lexedMessage = message.split("'");
                String key = lexedMessage[CLASS_INDEX_IN_MSG];
                String value = lexedMessage[SPEC_INDEX_IN_MSG].substring(TRIMMED_SPEC_NAME_INDEX);

                if (!changedMap.containsKey(key)) {
                    changedMap.put(key, new HashSet<>());
                }
                changedMap.get(key).add(value);
                i += 1;
            }
        }
        getLog().info("Added " + i + " class/spec to the changedMap from AspectJ's log.");
    }
}
