package edu.cornell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import edu.cornell.emop.maven.SurefireMojoInterceptor;
import edu.cornell.emop.util.Util;
import edu.cornell.emop.util.Violation;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "rpp", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "rpp")
public class RppMojo extends RppHandlerMojo {

    protected Path backgroundViolationsPath;
    protected Path criticalViolationsPath;

    @Parameter(property = "demoteCritical", defaultValue = "false", required = false)
    private boolean demoteCritical;

    public void setDemoteCritical(boolean demoteCritical) {
        this.demoteCritical = demoteCritical;
    }

    /**
     * Runs maven surefire.
     * @return false if the surefire run resulted in an exception, true if otherwise
     * @throws MojoExecutionException when the surefire invocation fails.
     */
    private boolean invokeSurefire() throws MojoExecutionException {
        getLog().info("RPP background phase surefire execution start: " + timeFormatter.format(new Date()));
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        try {
            PrintStream ps = new PrintStream(new FileOutputStream(
                    getArtifactsDir() + File.separator + "background-surefire-run.txt"));
            // FIXME: this somehow doesn't redirect the entire surefire output. Debug while PR is ongoing
            System.setOut(ps);
            System.setErr(ps);
            SurefireMojoInterceptor.sfMojo.getClass().getMethod("execute").invoke(SurefireMojoInterceptor.sfMojo);
            System.setOut(stdout);
            System.setErr(stderr);
        } catch (Exception ex) {
            return false;
        } finally {
            getLog().info("RPP background phase surefire execution end: " + timeFormatter.format(new Date()));
        }
        return true;
    }

    public void updateCriticalAndBackgroundSpecs(Path criticalViolationsPath,
                                                 Path backgroundViolationsPath,
                                                 String javamopAgent)
            throws MojoExecutionException, FileNotFoundException {
        Set<String> criticalSpecsSet = new HashSet<>();
        Set<String> allSpecs = Util.retrieveSpecListFromJar(javamopAgent, getLog());
        if (!demoteCritical && !RppHandlerMojo.criticalSpecsSet.equals(allSpecs)) {
            // demote specs if the critical specs set contained all specs (first run)
            criticalSpecsSet.addAll(RppHandlerMojo.criticalSpecsSet);
        }
        // read the violation-counts files and output the list of critical and background specs for next time
        // (in the case that the user doesn't provide files for critical and background specs)
        Set<String> violatedSpecs = Violation.parseViolationSpecs(criticalViolationsPath);
        Set<String> backgroundViolatedSpecs = new HashSet<>();
        if (backgroundViolationsPath != null) {
            backgroundViolatedSpecs = Violation.parseViolationSpecs(backgroundViolationsPath);
        }
        violatedSpecs.addAll(backgroundViolatedSpecs);
        violatedSpecs = violatedSpecs.stream().map(spec -> spec.endsWith("MonitorAspect") ? spec :
                spec + "MonitorAspect").collect(Collectors.toSet());
        // implicitly demote all specs that were not violated and not already in the critical specs set
        criticalSpecsSet.addAll(violatedSpecs);
        File artifactsDir = new File(getArtifactsDir());
        File metaCriticalSpecsFile = new File(artifactsDir, "rpp-critical-specs.txt");
        File metaBackgroundSpecsFile = new File(artifactsDir, "rpp-background-specs.txt");
        Set<String> backgroundSpecsSet = new HashSet<>(allSpecs);
        backgroundSpecsSet.removeAll(criticalSpecsSet);
        Util.writeSpecsToFile(criticalSpecsSet, metaCriticalSpecsFile);
        Util.writeSpecsToFile(backgroundSpecsSet, metaBackgroundSpecsFile);
    }

    /**
     * This mojo runs RPP.
     * @throws MojoExecutionException if RPP fails.
     */
    public void execute() throws MojoExecutionException {
        getLog().info("RPP background phase start: " + timeFormatter.format(new Date()));
        // by the time this method is invoked, we have finished invoking the critical specs surefire run
        criticalViolationsPath = Paths.get(Util.moveViolationCounts(getBasedir(), getArtifactsDir(), "critical"));
        String previousJavamopAgent = System.getProperty("rpp-agent");
        String backgroundAgent = System.getProperty("background-agent");
        if (!backgroundAgent.isEmpty()) {
            System.setProperty("previous-javamop-agent", previousJavamopAgent);
            System.setProperty("rpp-agent", backgroundAgent);
            if (!invokeSurefire()) {
                getLog().info("Surefire run threw an exception.");
            }
            backgroundViolationsPath = Paths.get(Util.moveViolationCounts(getBasedir(), getArtifactsDir(), "background"));
        } else { // edge case where critical phase runs all specs
            getLog().info("No specs to monitor for background phase, terminating...");
        }
        try {
            updateCriticalAndBackgroundSpecs(criticalViolationsPath, backgroundViolationsPath, previousJavamopAgent);
        } catch (FileNotFoundException ex) {
            getLog().error("Failed to automatically update critical and background specs.");
            System.exit(1);
        }
        getLog().info("RPP background phase end: " + timeFormatter.format(new Date()));
    }

}
