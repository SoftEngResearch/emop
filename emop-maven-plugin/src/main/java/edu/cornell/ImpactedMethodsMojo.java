package edu.cornell;

import java.io.File;
import java.util.List;

import edu.cornell.emop.util.Util;
import edu.illinois.starts.helpers.Writer;
import edu.illinois.starts.jdeps.MethodsMojo;

import edu.illinois.starts.util.Pair;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.tools.ajc.Main;

@Mojo(name = "impacted-methods", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class ImpactedMethodsMojo extends MethodsMojo {

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

    /**
     * The path to the Javamop Agent JAR file.
     */
    @Parameter(property = "javamopAgent")
    protected String javamopAgent;

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
