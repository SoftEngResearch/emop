package edu.cornell;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.emop.maven.AgentLoader;
import edu.cornell.emop.util.Util;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "monitor", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class MonitorMojo extends AffectedSpecsMojo {

    public static final String AGENT_CONFIGURATION_FILE = "new-aop-ajc.xml";
    protected static Set<String> monitorIncludes;
    protected static Set<String> monitorExcludes;

    @Parameter(property = "rpsRpp", defaultValue = "false")
    private boolean rpsRpp;

    @Parameter(property = "enableStats", defaultValue = "false")
    private boolean enableStats;

    public void execute() throws MojoExecutionException {
        super.execute();
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
        Util.generateNewAgentConfigurationFile(getArtifactsDir() + File.separator + AGENT_CONFIGURATION_FILE, affectedSpecs,
                monitorIncludes, monitorExcludes, enableStats);
        if (rpsRpp) {
            getLog().info("In mode RPS-RPP, writing the list of affected specs to affected-specs.txt...");
            try {
                Util.writeSpecsToFile(affectedSpecs, new File(getArtifactsDir(), "affected-specs.txt"));
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            System.setProperty("rpsRpp", "true");
        }
        getLog().info("AffectedSpecs: " + affectedSpecs.size());
        Util.replaceFileInJar(javamopAgent, "/META-INF/aop-ajc.xml",
                getArtifactsDir() + File.separator + AGENT_CONFIGURATION_FILE);
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
}
