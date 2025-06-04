package edu.cornell;

import java.io.File;
import java.util.List;

import edu.cornell.emop.util.Util;
import edu.illinois.starts.enums.Granularity;
import edu.illinois.starts.helpers.Writer;
import edu.illinois.starts.jdeps.ImpactedMojo;
import edu.illinois.starts.util.Pair;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "impacted", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class ImpactedComponentsMojo extends ImpactedMojo {
    /** Path to a JavaMOP Agent JAR file. */
    @Parameter(property = "javamopAgent")
    protected String javamopAgent;

    /** Denotes whether a project dependency (jar or Maven dependency) has changed. */
    protected boolean dependencyChanged = false;

    /** A list that stores the checksums of jar files. */
    protected List<Pair> jarCheckSums = null;

    /** Parameter to determine whether file checksums are updated. */
    @Parameter(property = "updateChecksums", defaultValue = "true")
    private boolean updateChecksums;

    public void execute() throws MojoExecutionException {
        configure();

        long start = System.currentTimeMillis();
        getLog().info("[eMOP] Invoking ImpactedComponentsMojo.");
        super.execute();
        long end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Execute ImpactedComponentsMojo takes " + (end - start) + " ms.");
        if (getGranularity() == Granularity.CLASS) {
            getLog().info("[eMOP] Total number of classes: " + (getOldClasses().size() + getNewClasses().size()));
        }

        checkDependencies();
    }

    private void configure() {
        if (getGranularity() == Granularity.FINE) {
            this.fineRTSOn = true;
            this.saveMRTSOn = true;
        }
        setTrackNewClasses(true);
        setUpdateImpactedChecksums(updateChecksums);
        setUpdateMethodsChecksums(updateChecksums);
    }

    private void checkDependencies() throws MojoExecutionException {
        String cpString = Writer.pathToString(getSureFireClassPath().getClassPath());
        List<String> sfPathElements = Util.getCleanClassPath(cpString);
        if (Util.hasDifferentClassPath(sfPathElements, getArtifactsDir())
                || Util.hasDifferentJarChecksum(sfPathElements, jarCheckSums, getArtifactsDir())) {
            Writer.writeClassPath(cpString, artifactsDir);
            Writer.writeJarChecksums(sfPathElements, artifactsDir, jarCheckSums);
            dependencyChanged = true;
            getLog().info("Dependencies changed! Reverting to Base RV.");
        }
    }
}
