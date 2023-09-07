package edu.cornell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.starts.enums.TransitiveClosureOptions;
import edu.illinois.starts.helpers.Writer;
import edu.illinois.starts.jdeps.ImpactedMojo;
import edu.illinois.starts.util.Pair;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "impacted", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class ImpactedClassMojo extends ImpactedMojo {

    private static final String TARGET = "target";

    /** Denotes whether a project dependency (jar or Maven dependency) has changed. */
    protected boolean dependencyChangeDetected = false;

    /** A list that stores the checksums of jar files. */
    protected List<Pair> jarCheckSums = null;

    /**
     * Parameter to determine whether file checksums are updated.
     */
    @Parameter(property = "updateChecksums", defaultValue = "true")
    private boolean updateChecksums;

    /**
     * Parameter to determine which closure to use for impacted classes.
     * Options are PS1, PS2, PS3.
     */
    @Parameter(
            property = "closureOption",
            defaultValue = "PS3"
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

        String cpString = Writer.pathToString(getSureFireClassPath().getClassPath());
        // TODO: Change STARTS so that it exposes the three methods needed here
        List<String> sfPathElements = getCleanClassPath(cpString);
        if (!isSameClassPath(sfPathElements) || !hasSameJarChecksum(sfPathElements)) {
            Writer.writeClassPath(cpString, artifactsDir);
            Writer.writeJarChecksums(sfPathElements, artifactsDir, jarCheckSums);
            dependencyChangeDetected = true;
            getLog().info("Dependencies changed! Reverting to Base RV.");
        }
    }

    // Copied from STARTS
    private boolean isSameClassPath(List<String> sfPathString) throws MojoExecutionException {
        if (sfPathString.isEmpty()) {
            return true;
        }
        String oldSfPathFileName = Paths.get(getArtifactsDir(), SF_CLASSPATH).toString();
        if (!new File(oldSfPathFileName).exists()) {
            return false;
        }
        try {
            List<String> oldClassPathLines = Files.readAllLines(Paths.get(oldSfPathFileName));
            if (oldClassPathLines.size() != 1) {
                throw new MojoExecutionException(SF_CLASSPATH + " is corrupt! Expected only 1 line.");
            }
            List<String> oldClassPathelements = getCleanClassPath(oldClassPathLines.get(0));
            // comparing lists and not sets in case order changes
            if (sfPathString.equals(oldClassPathelements)) {
                return true;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return false;
    }

    // Copied from STARTS
    private boolean hasSameJarChecksum(List<String> cleanSfClassPath) throws MojoExecutionException {
        if (cleanSfClassPath.isEmpty()) {
            return true;
        }
        String oldChecksumPathFileName = Paths.get(getArtifactsDir(), JAR_CHECKSUMS).toString();
        if (!new File(oldChecksumPathFileName).exists()) {
            return false;
        }
        boolean noException = true;
        try {
            List<String> lines = Files.readAllLines(Paths.get(oldChecksumPathFileName));
            Map<String, String> checksumMap = new HashMap<>();
            for (String line : lines) {
                String[] elems = line.split(COMMA);
                checksumMap.put(elems[0], elems[1]);
            }
            jarCheckSums = new ArrayList<>();
            for (String path : cleanSfClassPath) {
                Pair<String, String> pair = Writer.getJarToChecksumMapping(path);
                jarCheckSums.add(pair);
                String oldCS = checksumMap.get(pair.getKey());
                noException &= pair.getValue().equals(oldCS);
            }
        } catch (IOException ioe) {
            noException = false;
            // reset to null because we don't know what/when exception happened
            jarCheckSums = null;
            ioe.printStackTrace();
        }
        return noException;
    }

    // Copied from STARTS
    private List<String> getCleanClassPath(String cp) {
        List<String> cpPaths = new ArrayList<>();
        String[] paths = cp.split(File.pathSeparator);
        String classes = File.separator + TARGET +  File.separator + CLASSES;
        String testClasses = File.separator + TARGET + File.separator + TEST_CLASSES;
        for (int i = 0; i < paths.length; i++) {
            // TODO: should we also exclude SNAPSHOTS from same project?
            if (paths[i].contains(classes) || paths[i].contains(testClasses)) {
                continue;
            }
            cpPaths.add(paths[i]);
        }
        return cpPaths;
    }
}
