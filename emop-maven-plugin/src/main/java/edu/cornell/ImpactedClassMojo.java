package edu.cornell;

import java.util.List;

import edu.cornell.emop.util.Util;
import edu.illinois.starts.enums.TransitiveClosureOptions;
import edu.illinois.starts.helpers.Writer;
import edu.illinois.starts.jdeps.ImpactedMojo;
import edu.illinois.starts.util.Pair;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "impacted", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class ImpactedClassMojo extends ImpactedMojo {

    /** Denotes whether a project dependency (jar or Maven dependency) has changed. */
    protected boolean dependencyChangeDetected = false;

    /** A list that stores the checksums of jar files. */
    protected List<Pair> jarCheckSums = null;

    @Parameter(property = "debug", defaultValue = "false")
    protected boolean debug;

    /** Parameter to determine whether file checksums are updated. */
    @Parameter(property = "updateChecksums", defaultValue = "true")
    private boolean updateChecksums;

    /** Parameter to determine whether fine RTS should be used. */
    @Parameter(property = "enableFineRTS", defaultValue = "false")
    private boolean enableFineRTS;

    /**
     * Parameter to determine which closure to use for impacted classes.
     * Options are PS1, PS2, PS3.
     */
    @Parameter(property = "closureOption", defaultValue = "PS3")
    private TransitiveClosureOptions closureOption;

    public void execute() throws MojoExecutionException {
        this.fineRTSOn = enableFineRTS;
        this.saveMRTSOn = enableFineRTS;
        setUpdateImpactedChecksums(updateChecksums);
        setTrackNewClasses(true);
        setTransitiveClosureOption(closureOption);

        long start = System.currentTimeMillis();
        getLog().info("[eMOP] Invoking the ImpactedClasses Mojo...");
        super.execute();
        long end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Execute ImpactedClasses Mojo takes " + (end - start) + " ms");
        getLog().info("[eMOP] Total number of classes: " + (getOldClasses().size() + getNewClasses().size()));

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
