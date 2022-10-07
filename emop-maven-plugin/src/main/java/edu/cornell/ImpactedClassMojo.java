package edu.cornell;

import edu.illinois.starts.enums.TransitiveClosureOptions;
import edu.illinois.starts.jdeps.ImpactedMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "impacted", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class ImpactedClassMojo extends ImpactedMojo {

    /**
     * Parameter to determine whether file checksums are updated.
     */
    @Parameter(property = "updateChecksums", defaultValue = "true")
    private boolean updateChecksums;

    /**
     * Parameter to determine which closure to use for impacted classes.
     * TRANS_OF_INVERSE_TRANS = p1
     * TRANS_AND_INVERSE_TRANS = p2
     * TRANS = p3
     */
    @Parameter(
            property = "closureOption",
            defaultValue = "TRANS"
    )
    private TransitiveClosureOptions closureOption;

    public void execute() throws MojoExecutionException {
        setUpdateImpactedChecksums(updateChecksums);
        setTrackNewClasses(true);
        setTransitiveClosureOption(closureOption);
        long start = System.currentTimeMillis();
        getLog().info("[eMOP] Invoking the ImpactedClasses Mojo...");
        super.execute();
        long end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Execute ImpactedClasses Mojo takes " + (end - start) + " ms");
        getLog().info("[eMOP] Total number of classes: " + (getOldClasses().size() + getNewClasses().size()));
        if (getImpacted().isEmpty()) {
            getLog().info("[eMOP] No impacted classes, terminating...");
            System.exit(0);
        }
    }
}
