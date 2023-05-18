package edu.cornell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cornell.emop.maven.AgentLoader;
import edu.cornell.emop.util.Util;
import edu.illinois.starts.helpers.Writer;
import edu.illinois.starts.util.Pair;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "monitor", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class MonitorMojo extends AffectedSpecsMojo {

    private static final String TARGET = "target";
    public static final String MONITOR_FILE = "new-aop-ajc.xml";

    protected static Set<String> monitorIncludes;
    protected static Set<String> monitorExcludes;

    protected List<Pair> jarCheckSums = null;

    /**
     * The path that specify the Javamop Agent JAR file.
     */
    @Parameter(property = "javamopAgent")
    private String javamopAgent;

    /**
     * The path that specify the Javamop Agent JAR file.
     */
    @Parameter(property = "includeNonAffected", required = false, defaultValue = "true")
    private boolean includeNonAffected;

    /**
     * Whether to instrument third-party libraries.
     * Setting this option to false triggers the ^l weak RPS variants.
     */
    @Parameter(property = "includeLibraries", required = false, defaultValue = "true")
    private boolean includeLibraries;

    @Parameter(property = "rpsRpp", defaultValue = "false")
    private boolean rpsRpp;

    public void execute() throws MojoExecutionException {
        super.execute();
        if (hasLibraryChanges()) {
            includeLibraries = true;
        }
        if (getImpacted().isEmpty()) {
            System.setProperty("exiting-rps", "true");
            System.setProperty("rps-test-excludes", "**/Test*,**/*Test,**/*Tests,**/*TestCase");
            if (!AgentLoader.loadDynamicAgent("JavaAgent.class")) {
                throw new MojoExecutionException("Could not attach agent");
            }
            getLog().info("No impacted classes mode detected MonitorMojo");
            return;
        }
        getLog().info("[eMOP] Invoking the Monitor Mojo...");
        long start = System.currentTimeMillis();
        monitorIncludes = includeLibraries ? new HashSet<>() : retrieveIncludePackages();
        monitorExcludes = includeNonAffected ? new HashSet<>() : getNonAffected();
        Util.generateNewMonitorFile(getArtifactsDir() + File.separator + MONITOR_FILE, affectedSpecs,
                monitorIncludes, monitorExcludes);
        if (rpsRpp) {
            getLog().info("In mode RPS-RPP, writing the list of affected specs to affected-specs.txt...");
            try {
                Util.writeSpecsToFile(affectedSpecs, new File(
                        getArtifactsDir(), "affected-specs.txt"));
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            System.setProperty("rpsRpp", "true");
        }
        if (javamopAgent == null) {
            javamopAgent = getLocalRepository().getBasedir() + File.separator + "javamop-agent"
                    + File.separator + "javamop-agent"
                    + File.separator + "1.0"
                    + File.separator + "javamop-agent-1.0.jar";
        }
        Util.replaceFileInJar(javamopAgent, "/META-INF/aop-ajc.xml", getArtifactsDir() + File.separator + MONITOR_FILE);
        long end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Generating aop-ajc.xml and replace it takes " + (end - start) + " ms");
    }

    /**
     * Generates the set of package names of classes that should be monitored. If the set is non-empty, then any
     * package not included in this set will not be monitored.
     * @return created set of package names for weaving. An empty set is returned if the includeLibraries is true.
     */
    private Set<String> retrieveIncludePackages() {
        if (!includeLibraries) {
            return Util.retrieveProjectPackageNames(getClassesDirectory());
        }
        return new HashSet<>();
    }

    /**
     * Generates a String containing !within() pointcuts so that instrumentation is only performed within the
     * affected classes, effectively disabling instrumentation in non-affected classes.
     * If the includeNonAffected parameter is set to true, then this method would return an empty string.
     */
    private String generateNonAffectedExclusion() {
        StringBuilder stringBuilder = new StringBuilder();
        if (!includeNonAffected) {
            for (String className : getNonAffected()) {
                if (!className.contains("package-info")) { // not excluding package-info results in compile error
                    stringBuilder.append("    !within(" + className + ") &&\n");
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Checks whether third-party library changes are involved by looking at whether:
     * 1. Classpath has been changed
     * 2. Jar checksums have been changed
     * @return Whether a third-party library change is involved
     */
    private boolean hasLibraryChanges() throws MojoExecutionException {
        String cpString = Writer.pathToString(getSureFireClassPath().getClassPath());
        List<String> sfPathElements = getCleanClassPath(cpString);
        return !isSameClassPath(sfPathElements) || !hasSameJarChecksum(sfPathElements);
    }

    // Copied from STARTS
    private boolean isSameClassPath(List<String> sfPathString) throws MojoExecutionException {
        if (sfPathString.isEmpty()) {
            return true;
        }
        String oldSfPathFileName = Paths.get(getArtifactsDir(), SF_CLASSPATH).toString();
        if (!new File(oldSfPathFileName).exists()) {
            return false;
        }
        try {
            List<String> oldClassPathLines = Files.readAllLines(Paths.get(oldSfPathFileName));
            if (oldClassPathLines.size() != 1) {
                throw new MojoExecutionException(SF_CLASSPATH + " is corrupt! Expected only 1 line.");
            }
            List<String> oldClassPathelements = getCleanClassPath(oldClassPathLines.get(0));
            // comparing lists and not sets in case order changes
            if (sfPathString.equals(oldClassPathelements)) {
                return true;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return false;
    }

    // Copied from STARTS
    private boolean hasSameJarChecksum(List<String> cleanSfClassPath) throws MojoExecutionException {
        if (cleanSfClassPath.isEmpty()) {
            return true;
        }
        String oldChecksumPathFileName = Paths.get(getArtifactsDir(), JAR_CHECKSUMS).toString();
        if (!new File(oldChecksumPathFileName).exists()) {
            return false;
        }
        boolean noException = true;
        try {
            List<String> lines = Files.readAllLines(Paths.get(oldChecksumPathFileName));
            Map<String, String> checksumMap = new HashMap<>();
            for (String line : lines) {
                String[] elems = line.split(COMMA);
                checksumMap.put(elems[0], elems[1]);
            }
            jarCheckSums = new ArrayList<>();
            for (String path : cleanSfClassPath) {
                Pair<String, String> pair = Writer.getJarToChecksumMapping(path);
                jarCheckSums.add(pair);
                String oldCS = checksumMap.get(pair.getKey());
                noException &= pair.getValue().equals(oldCS);
            }
        } catch (IOException ioe) {
            noException = false;
            // reset to null because we don't know what/when exception happened
            jarCheckSums = null;
            ioe.printStackTrace();
        }
        return noException;
    }

    // Copied from STARTS
    private List<String> getCleanClassPath(String cp) {
        List<String> cpPaths = new ArrayList<>();
        String[] paths = cp.split(File.pathSeparator);
        String classes = File.separator + TARGET +  File.separator + CLASSES;
        String testClasses = File.separator + TARGET + File.separator + TEST_CLASSES;
        for (int i = 0; i < paths.length; i++) {
            // TODO: should we also exclude SNAPSHOTS from same project?
            if (paths[i].contains(classes) || paths[i].contains(testClasses)) {
                continue;
            }
            cpPaths.add(paths[i]);
        }
        return cpPaths;
    }
}
