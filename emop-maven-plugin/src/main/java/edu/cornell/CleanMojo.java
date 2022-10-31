package edu.cornell;

import java.nio.file.Paths;

import edu.illinois.starts.helpers.FileUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "clean", requiresDirectInvocation = true)
public class CleanMojo extends edu.illinois.starts.jdeps.CleanMojo {

    /**
     * Options for which artifacts to clean.
     */
    public static enum CleanMode {
        EVERYTHING,         // All artifacts
        VIOLATION_COUNTS    // violation-counts only
    }

    /**
     * Parameter to determine which artifacts to clean.
     */
    @Parameter(property = "clean.mode", defaultValue = "EVERYTHING")
    private CleanMode mode;

    public void execute() throws MojoExecutionException {
        switch (mode) {
            case EVERYTHING:
                super.execute();
                FileUtil.delete(Paths.get(System.getProperty("user.dir"), "weaved-bytecode").toFile());
                // fall through
            case VIOLATION_COUNTS:
                FileUtil.delete(Paths.get(System.getProperty("user.dir"), "violation-counts").toFile());
                break;
            default:
                break;
        }
    }
}
