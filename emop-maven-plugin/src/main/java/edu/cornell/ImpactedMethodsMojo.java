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

    public void execute() throws MojoExecutionException {
        setUpdateMethodsChecksums(updateChecksums);
        setComputeImpactedMethods(computeImpactedMethods);
        setIncludeVariables(includeVariables);  
        setDebug(debug);
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
