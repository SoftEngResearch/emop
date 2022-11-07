package edu.cornell;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.emop.util.Util;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "monitor", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class MonitorMojo extends AffectedSpecsMojo {

    private String monitorFile = "new-aop-ajc.xml";

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

    public void execute() throws MojoExecutionException {
        super.execute();
        getLog().info("[eMOP] Invoking the Monitor Mojo...");
        long start = System.currentTimeMillis();
        Util.generateNewMonitorFile(getArtifactsDir() + File.separator + monitorFile, affectedSpecs,
                includeLibraries ? new HashSet<>() : retrieveIncludePackages(),
                includeNonAffected ? new HashSet<>() : getNonAffected());
        if (javamopAgent == null) {
            javamopAgent = getLocalRepository().getBasedir() + File.separator + "javamop-agent"
                    + File.separator + "javamop-agent"
                    + File.separator + "1.0"
                    + File.separator + "javamop-agent-1.0.jar";
        }
        Util.replaceFileInJar(javamopAgent, "/META-INF/aop-ajc.xml", getArtifactsDir() + File.separator + monitorFile);
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
