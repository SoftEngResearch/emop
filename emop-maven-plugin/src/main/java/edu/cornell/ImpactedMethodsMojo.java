package edu.cornell;

import edu.illinois.starts.jdeps.MethodsMojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "impacted-methods", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class ImpactedMethodsMojo extends MethodsMojo {

    /**
     * Parameter to determine whether file checksums are updated.
     */
    @Parameter(property = "updateChecksums", defaultValue = "true")
    private boolean updateChecksums;

    public void execute() throws MojoExecutionException {
        setUpdateMethodsChecksums(updateChecksums);
        // setTrackNewClasses(true);
        // setTransitiveClosureOption(closureOption);
        long start = System.currentTimeMillis();
        getLog().info("[eMOP] Invoking the ImpactedMethods Mojo...");
        super.execute();
        long end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Execute ImpactedMethods Mojo takes " + (end - start) + " ms");
//         getLog().info("[eMOP] Total number of methods: " + (getOldClasses().size() + getNewClasses().size()));
//         if (getImpacted().isEmpty()) {
//             getLog().info("[eMOP] No impacted methods, returning...");
// //            System.exit(0);
//         }
    }
}
