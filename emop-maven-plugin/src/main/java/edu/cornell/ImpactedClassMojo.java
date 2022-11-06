package edu.cornell;

import java.util.Date;

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
     * TRANSITIVE_OF_INVERSE_TRANSITIVE = p1
     * TRANSITIVE_AND_INVERSE_TRANSITIVE = p2
     * TRANSITIVE = p3
     */
    @Parameter(
            property = "closureOption",
            defaultValue = "TRANSITIVE"
    )
    private TransitiveClosureOptions closureOption;

    public void execute() throws MojoExecutionException {
        getLog().info("ImpactedClassMojo start time: " + RppHandlerMojo.timeFormatter.format(new Date()));
        getLog().info("closureOption: " + closureOption);
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
        getLog().info("ImpactedClassMojo end time: " + RppHandlerMojo.timeFormatter.format(new Date()));
    }
}
