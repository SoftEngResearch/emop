package edu.cornell;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Filename to read the "old" violations from. The filename is resolved
     * against the artifacts directory.
     */
    @Parameter(property = "lastViolationsFile", defaultValue = "violation-counts-old")
    private String lastViolationsFile;

    private Path oldViolationCountsPath;
    private Path newViolationCountsPath;


    public void execute() throws MojoExecutionException {
        super.execute();
        doVMSPart();
    }

    protected String getLastShaHelper() throws MojoExecutionException {
        if (lastSha != null && !lastSha.isEmpty()){
            return lastSha;
        }
        Path lastShaPath = Paths.get(getArtifactsDir(), "last-SHA");
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(lastShaPath.toFile()))) {
            String sha = bufferedReader.readLine();
            return sha;
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void doVMSPart() throws MojoExecutionException {
        gitDir = Paths.get(executionRootDirectory, ".git");

        // for now, we will emulate regular VMS by aggregating between the critical and background runs
        // to get a singular violation-counts file, and comparing that against the previously created version

        oldViolationCountsPath = Paths.get(getArtifactsDir(), lastViolationsFile);
        Set<Violation> oldViolations = Violation.parseViolations(oldViolationCountsPath);
        // need to get path from both critical and background phases
        Set<Violation> newViolations = Violation.parseViolations(Paths.get(criticalViolationsPath));
        newViolations.addAll(Violation.parseViolations(Paths.get(bgViolationsPath)));

        lastSha = getLastShaHelper();
        if (lastSha == null && !lastSha.isEmpty()) firstRun = true;

        if (!firstRun) {
            List<DiffEntry> diffEntryList = VmsMojo.getCommitDiffsHelper(gitDir, lastSha, newSha);
            Map<String, String> renames = new HashMap<>();
            Map<String, Map<Integer, Integer>> offsets = new HashMap<>();
            Map<String, Set<Integer>> modifiedLines = new HashMap<>();
            VmsMojo.findLineChangesAndRenamesHelper(diffEntryList, renames, offsets, modifiedLines);
        }

        


    }

}