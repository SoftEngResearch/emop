package edu.cornell;

import java.util.List;

import edu.cornell.emop.util.Util;
import edu.illinois.starts.enums.Granularity;
import edu.illinois.starts.helpers.Writer;
import edu.illinois.starts.jdeps.ImpactedMojo;
import edu.illinois.starts.util.Pair;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "impacted", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class ImpactedComponentsMojo extends ImpactedMojo {

    protected boolean computeImpactedMethods = true;

    /** Parameter to determine whether to include variables in the impacted methods. */
    protected boolean includeVariables;

    /** Path to a JavaMOP Agent JAR file. */
    @Parameter(property = "javamopAgent")
    protected String javamopAgent;

    /** Denotes whether a project dependency (jar or Maven dependency) has changed. */
    protected boolean dependencyChangeDetected = false;

    /** A list that stores the checksums of jar files. */
    protected List<Pair> jarCheckSums = null;

    @Parameter(property = "debug", defaultValue = "false")
    protected boolean debug;

    /** Parameter to determine whether file checksums are updated. */
    @Parameter(property = "updateChecksums", defaultValue = "true")
    private boolean updateChecksums;

    /** Determines whether fine RTS should be used. */
    @Parameter(property = "enableFineRTS", defaultValue = "false")
    private boolean enableFineRTS;

    /** Choose which level of granularity to perform impact-change analysis. */
    @Parameter(property = "granularity", defaultValue = "CLASS")
    private Granularity granularity;

    public void execute() throws MojoExecutionException {
        // TODO: Refactor this section:
        if (getGranularity() == Granularity.CLASS || getGranularity() == Granularity.FINE) {
            System.out.println("Granularity:" + getGranularity());
            this.fineRTSOn = enableFineRTS;
            this.saveMRTSOn = enableFineRTS;
            setUpdateImpactedChecksums(updateChecksums);
            setTrackNewClasses(true);
        } else if (getGranularity() == Granularity.METHOD) {
            setUpdateMethodsChecksums(updateChecksums);
            setComputeImpactedMethods(computeImpactedMethods);
            setIncludeVariables(includeVariables);
            setDebug(debug);
        } else if (getGranularity() == Granularity.HYBRID) {
            setUpdateMethodsChecksums(true);
            setComputeImpactedMethods(true);
        }
        long start = System.currentTimeMillis();
        getLog().info("[eMOP] Invoking ImpactedComponentsMojo.");
        super.execute();
        long end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Execute ImpactedComponentsMojo takes " + (end - start) + " ms.");
        if (getGranularity() == Granularity.CLASS) {
            getLog().info("[eMOP] Total number of classes: " + (getOldClasses().size() + getNewClasses().size()));
        }
        String cpString = Writer.pathToString(getSureFireClassPath().getClassPath());
        List<String> sfPathElements = Util.getCleanClassPath(cpString);
        if (Util.hasDifferentClassPath(sfPathElements, getArtifactsDir())
                || Util.hasDifferentJarChecksum(sfPathElements, jarCheckSums, getArtifactsDir())) {
            Writer.writeClassPath(cpString, artifactsDir);
            Writer.writeJarChecksums(sfPathElements, artifactsDir, jarCheckSums);
            dependencyChangeDetected = true;
            getLog().info("Dependencies changed! Reverting to Base RV.");
        }
    }
}
