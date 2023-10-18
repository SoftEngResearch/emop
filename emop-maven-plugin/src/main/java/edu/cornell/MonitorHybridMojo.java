package edu.cornell;

import java.io.File;
import java.util.HashSet;

import java.util.Set;

import edu.cornell.emop.maven.AgentLoader;
import edu.cornell.emop.util.Util;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "monitor-hybrid", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class MonitorHybridMojo extends AffectedSpecsHybridMojo {

    public static final String AGENT_CONFIGURATION_FILE = "new-aop-ajc.xml";
    protected static Set<String> monitorIncludes;
    protected static Set<String> monitorExcludes;

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

    /**
     * Parameter to determine whether file checksums are updated.
     */
    @Parameter(property = "updateChecksums", defaultValue = "true")
    private boolean updateChecksumsTemp;

    /**
     * Parameter to determine whether file checksums are updated.
     */
    @Parameter(property = "computeImpactedMethods", defaultValue = "true")
    private boolean computeImpactedMethodsTemp;

    /*
     * Parameter to determine whether to include variables in the impacted methods.
     */
    @Parameter(property = "includeVariables", defaultValue = "false")
    private boolean includeVariablesTemp;

    @Parameter(property = "debug", defaultValue = "flase")
    private boolean debugTemp;

    public void execute() throws MojoExecutionException {
        includeVariables = includeVariablesTemp;
        updateChecksums = updateChecksumsTemp;
        computeImpactedMethods = computeImpactedMethodsTemp;
        debug = debugTemp;
        super.execute();

        if (getAffectedMethods().isEmpty() && getAffectedClasses().isEmpty()) {
            System.setProperty("exiting-rps", "true");
            System.setProperty("rps-test-excludes", "**/Test*,**/*Test,**/*Tests,**/*TestCase");
            if (!AgentLoader.loadDynamicAgent("JavaAgent.class")) {
                throw new MojoExecutionException("Could not attach agent");
            }
            getLog().info("No impacted classes mode detected Hybrid MonitorMojo");
            return;
        }

        getLog().info("[eMOP] Invoking the Hybrid Monitor Mojo...");
        long start = System.currentTimeMillis();
        monitorIncludes = includeLibraries ? new HashSet<>() : retrieveIncludePackages();
        monitorExcludes = new HashSet<>();
        getLog().info("AffectedSpecs: " + affectedSpecs.size());
        if (debug) {
            getLog().info("AffectedSpecs: " + affectedSpecs);
        }
        Util.generateNewAgentConfigurationFile(getArtifactsDir() + File.separator + AGENT_CONFIGURATION_FILE, affectedSpecs,
                monitorIncludes, monitorExcludes);
        if (javamopAgent == null) {
            javamopAgent = getLocalRepository().getBasedir() + File.separator + "javamop-agent"
                    + File.separator + "javamop-agent"
                    + File.separator + "1.0"
                    + File.separator + "javamop-agent-1.0.jar";
        }
        Util.replaceFileInJar(javamopAgent, "/META-INF/aop-ajc.xml",
                getArtifactsDir() + File.separator + AGENT_CONFIGURATION_FILE);
        long end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Generating aop-ajc.xml and replace it takes " + (end - start) + " ms");
    }

    /**
     * Generates the set of package names of classes that should be monitored. If
     * the set is non-empty, then any
     * package not included in this set will not be monitored.
     *
     * @return created set of package names for weaving. An empty set is returned if
     *         the includeLibraries is true.
     */
    private Set<String> retrieveIncludePackages() {
        if (!includeLibraries) {
            return Util.retrieveProjectPackageNames(getClassesDirectory());
        }
        return new HashSet<>();
    }
}
