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
    protected boolean updateChecksums;

    /**
     * Parameter to determine whether file checksums are updated.
     */
    protected boolean computeImpactedMethods;

    /*
     * Parameter to determine whether to include variables in the impacted methods.
     */
    protected boolean includeVariables;

    /**
     * Parameter to determine whether to include variables in the impacted methods.
     */
    protected boolean debug;

    /**
     * Parameter to determine whether to include non affectedClasses.
     */
    protected boolean includeNonAffectedClasses;

    public void setIncludeNonAffected(boolean includeNonAffected) {
        this.includeNonAffectedClasses = includeNonAffected;
    }

    public void execute() throws MojoExecutionException {
        setUpdateMethodsChecksums(true);
        setComputeImpactedMethods(true);
        super.setIncludeNonAffectedClasses(includeNonAffectedClasses);
        long start = System.currentTimeMillis();
        getLog().info("[eMOP] Invoking the ImpactedMethods Mojo...");
        super.execute();
        long end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Execute ImpactedMethods Mojo takes " + (end - start) + " ms");
    }

}
