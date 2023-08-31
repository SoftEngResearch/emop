package edu.cornell;

import edu.illinois.starts.jdeps.HybridMojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "hybrid", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class ImpactedHybridMojo extends HybridMojo {

    /**
     * Parameter to determine whether file checksums are updated.
     */
    @Parameter(property = "updateChecksums", defaultValue = "true")
    private boolean updateChecksums;

    /**
     * Parameter to determine whether file checksums are updated.
     */
    @Parameter(property = "computeImpactedMethods", defaultValue = "true")
    private boolean computeImpactedMethods;

    public void execute() throws MojoExecutionException {
        setUpdateMethodsChecksums(true);
        setComputeImpactedMethods(true);
        long start = System.currentTimeMillis();
        getLog().info("[eMOP] Invoking the ImpactedMethods Mojo...");
        super.execute();
        long end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Execute ImpactedMethods Mojo takes " + (end - start) + " ms");
    }

    public boolean getComputeImpactedMethods() {
        return computeImpactedMethods;
    }

}
