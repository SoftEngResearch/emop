package edu.cornell;

import java.nio.file.Paths;
import java.util.Set;

import edu.illinois.starts.helpers.FileUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "clean", requiresDirectInvocation = true)
public class CleanMojo extends edu.illinois.starts.jdeps.CleanMojo {

    /**
     * Options for which artifacts to clean.
     */
    public static enum Artifact {
        STARTS,
        WEAVED_BYTECODE,
        VIOLATION_COUNTS
    }

    /**
     * Parameter to determine which artifacts to clean. Specified as a comma-separated list of
     * artifacts. By default, all artifacts are cleaned. The artifacts recognized by this parameter
     * are:
     * <ul>
     *   <li><b>STARTS</b>: the {@code .starts} directory</li>
     *   <li><b>WEAVED_BYTECODE</b>: the {@code weaved-bytecode} directory</li>
     *   <li><b>VIOLATION_COUNTS</b>: the {@code violation-counts} file</li>
     * </ul>
     */
    @Parameter(property = "clean.mode", defaultValue = "STARTS,WEAVED_BYTECODE,VIOLATION_COUNTS")
    private Set<Artifact> mode;

    public void execute() throws MojoExecutionException {
        for (Artifact artifact : mode) {
            switch (artifact) {
                case STARTS:
                    super.execute();
                    break;
                case WEAVED_BYTECODE:
                    FileUtil.delete(Paths.get(System.getProperty("user.dir"), "weaved-bytecode").toFile());
                    break;
                case VIOLATION_COUNTS:
                    FileUtil.delete(basedir.toPath().resolve("violation-counts").toFile());
                    break;
                default:
                    break;
            }
        }
    }
}
