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

    /**
     * Parameter to determine whether file checksums are updated.
     */
    @Parameter(property = "computeImpactedMethods", defaultValue = "true")
    private boolean computeImpactedMethodOption;

    public void execute() throws MojoExecutionException {
        getLog().info("The computeImpactedMethods value is : " + computeImpactedMethodOption);

        setUpdateMethodsChecksums(true);
        setComputeImpactedMethods(true);
        long start = System.currentTimeMillis();
        getLog().info("[eMOP] Invoking the ImpactedMethods Mojo...");
        super.execute();
        long end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Execute ImpactedMethods Mojo takes " + (end - start) + " ms");
    }

    public boolean getComputeImpactedMethods() {
        return computeImpactedMethodOption;
    }

}
