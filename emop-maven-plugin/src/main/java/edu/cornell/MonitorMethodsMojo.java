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

@Mojo(name = "monitor-methods", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class MonitorMethodsMojo extends AffectedSpecsMethodsMojo {

    public static final String AGENT_CONFIGURATION_FILE = "new-aop-ajc.xml";
    protected static Set<String> monitorIncludes;
    protected static Set<String> monitorExcludes;

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

    @Parameter(property = "enableStats", defaultValue = "false")
    private boolean enableStats;

    public void execute() throws MojoExecutionException {
        includeVariables = includeVariablesTemp;
        updateChecksums = updateChecksumsTemp;
        computeImpactedMethods = computeImpactedMethodsTemp;
        debug = debugTemp;
        super.execute();

        // If there is no affected methods, then we should not instrument anything.
        if (getAffectedMethods().isEmpty()) {
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
        monitorExcludes = includeNonAffected ? new HashSet<>() : getNonAffectedClasses();
        getLog().info("AffectedSpecs: " + affectedSpecs.size());
        if (debug) {
            getLog().info("AffectedSpecs: " + affectedSpecs);
        }
        Util.generateNewAgentConfigurationFile(getArtifactsDir() + File.separator + AGENT_CONFIGURATION_FILE, affectedSpecs,
                monitorIncludes, monitorExcludes, enableStats);
        Util.replaceFileInJar(javamopAgent, "/META-INF/aop-ajc.xml",
                getArtifactsDir() + File.separator + AGENT_CONFIGURATION_FILE);
        long end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Generating aop-ajc.xml and replace it takes " + (end - start) + " ms");
    }

    /**
     * Generates the set of package names of classes that should be monitored. If
     * the set is non-empty, then any package not included in this set will not be
     * monitored.
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
