package edu.cornell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.emop.util.Util;
import edu.illinois.starts.helpers.Writer;
import edu.illinois.starts.jdeps.HybridMojo;

import edu.illinois.starts.util.Pair;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "hybrid", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class ImpactedHybridMojo extends HybridMojo {

    /** Denotes whether a project dependency (jar or Maven dependency) has changed. */
    protected boolean dependencyChangeDetected = false;

    /** A list that stores the checksums of jar files. */
    protected List<Pair> jarCheckSums = null;

    /**
     * Parameter to determine whether file checksums are updated.
     */
    protected boolean updateChecksums;

    /**
     * Parameter to determine whether file checksums are updated.
     */
    protected boolean computeImpactedMethods;

    /** Parameter to determine whether to include variables in the impacted methods. */
    protected boolean includeVariables;

    @Parameter(property = "debug", defaultValue = "false")
    protected boolean debug;

    public void execute() throws MojoExecutionException {
        setUpdateMethodsChecksums(true);
        setComputeImpactedMethods(true);
        long start = System.currentTimeMillis();
        getLog().info("[eMOP] Invoking the ImpactedMethods Mojo...");
        super.execute();
        long end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Execute ImpactedMethods Mojo takes " + (end - start) + " ms");

        String cpString = Writer.pathToString(getSureFireClassPath().getClassPath());
        List<String> sfPathElements = Util.getCleanClassPath(cpString);
        if (!Util.isSameClassPath(sfPathElements, getArtifactsDir())
                || !Util.hasSameJarChecksum(sfPathElements, jarCheckSums, getArtifactsDir())) {
            Writer.writeClassPath(cpString, artifactsDir);
            Writer.writeJarChecksums(sfPathElements, artifactsDir, jarCheckSums);
            dependencyChangeDetected = true;
            getLog().info("Dependencies changed! Reverting to Base RV.");
        }
    }
}
