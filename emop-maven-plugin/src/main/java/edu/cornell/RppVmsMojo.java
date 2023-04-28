package edu.cornell;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import edu.cornell.emop.util.Violation;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.eclipse.jgit.diff.DiffEntry;

@Mojo(name = "rpp-vms", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "rpp-vms")
public class RppVmsMojo extends RppMojo {

    @Parameter(defaultValue = "${session.executionRootDirectory}", required = true, readonly = true)
    private String executionRootDirectory;

    protected Path gitDir;


    // copied from VMS - is there a better way to do this?
    /**
     * Specific SHA to use as the "old" version of code when making comparisons.
     * Should correspond to the previous run of VMS.
     */
    @Parameter(property = "lastSha", required = false)
    private String lastSha;

    /**
     * Specific SHA to use as the "new" version of code when making comparisons.
     */
    @Parameter(property = "newSha", required = false)
    private String newSha;

    /**
     * Whether to treat all found violations as new, regardless of previous runs.
     */
    @Parameter(property = "firstRun", required = false, defaultValue = "false")
    private boolean firstRun;

    private Path oldViolationCountsPath;
    private Path newViolationCountsPath;

    private Set<Violation> oldViolations;
    private Set<Violation> newViolations;


    public void execute() throws MojoExecutionException {
        super.execute();
        doVMSPart();
    }

    private void doVMSPart() throws MojoExecutionException {
        gitDir = Paths.get(executionRootDirectory, ".git");
        List<DiffEntry> diffEntryList = VmsMojo.getCommitDiffsHelper(gitDir, lastSha, newSha);

    }

}
