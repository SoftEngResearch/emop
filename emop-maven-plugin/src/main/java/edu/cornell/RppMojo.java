package edu.cornell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    @Parameter(property = "demoteCritical", defaultValue = "false", required = false)
    private boolean demoteCritical;

    /**
     * Runs maven surefire.
     * @throws MojoExecutionException when the surefire invocation fails.
     */
    private void invokeSurefire(String phaseName) throws MojoExecutionException {
        getLog().info("previous-javamop-agent: " + System.getProperty("previous-javamop-agent"));
        getLog().info("rpp-agent: " + System.getProperty("rpp-agent"));
        getLog().info("RPP " + phaseName + " phase surefire execution start: " + timeFormatter.format(new Date()));
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        try {
            PrintStream ps = new PrintStream(new FileOutputStream(
                    getArtifactsDir() + File.separator + phaseName + "-surefire-run.txt"));
//            // FIXME: this somehow doesn't redirect the entire surefire output. Debug while PR is ongoing
            System.setOut(ps);
            System.setErr(ps);
            SurefireMojoInterceptor.sfMojo.getClass().getMethod("execute").invoke(SurefireMojoInterceptor.sfMojo);
            System.setOut(stdout);
            System.setErr(stderr);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        getLog().info("RPP " + phaseName + " phase surefire execution end: " + timeFormatter.format(new Date()));
    }

    public void updateCriticalAndBackgroundSpecs(String criticalViolationsPath, String bgViolationsPath, String javamopAgent)
            throws MojoExecutionException, FileNotFoundException {
        Set<String> criticalSpecsSet = new HashSet<>();
        Set<String> allSpecs = Util.retrieveSpecListFromJar(javamopAgent, getLog());
        if (!demoteCritical && !RppHandlerMojo.criticalSpecsSet.equals(allSpecs)) {
            // demote specs if the critical specs set contained all specs (first run)
            criticalSpecsSet.addAll(RppHandlerMojo.criticalSpecsSet);
        }
        // read the violation-counts files and output the list of critical and background specs for next time
        // (in the case that the user doesn't provide files for critical and background specs)
        Set<String> violatedSpecs = Violation.parseViolationSpecs(Paths.get(criticalViolationsPath));
        Set<String> bgViolatedSpecs = Violation.parseViolationSpecs(Paths.get(bgViolationsPath));
        violatedSpecs.addAll(bgViolatedSpecs);
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

    public String runRppRun(String phase, String previousAgent, String agentPath) throws MojoExecutionException {
        if (agentPath.isEmpty()) {
            getLog().info("No specs to monitor for " + phase + " phase, terminating...");
            return "";
        }
        System.setProperty("previous-javamop-agent", previousAgent);
        System.setProperty("rpp-agent", agentPath);
        invokeSurefire(phase);
        String violationsPath = Util.moveViolationCounts(getBasedir(), getArtifactsDir(), phase);
        if (violationsPath.isEmpty()) {
            getLog().info("violation-counts file for " + phase + " run was not produced, skipping moving...");
        }
        return violationsPath;
    }

    /**
     * This mojo runs RPP.
     * @throws MojoExecutionException if RPP fails.
     */
    public void execute() throws MojoExecutionException {
        System.setProperty("skipping-execution", "false");
        System.setProperty("running-rpp", "true");
        getLog().info("RPP start: " + timeFormatter.format(new Date()));
        // by the time this method is invoked, we have finished invoking the critical specs surefire run
        String previousJavamopAgent = System.getProperty("previous-javamop-agent");
        System.out.println("previousJavamopAgent: " + previousJavamopAgent);
        String criticalAgent = System.getProperty("critical-agent");
        String backgroundAgent = System.getProperty("background-agent");
        String criticalViolationsPath = runRppRun("critical", previousJavamopAgent, criticalAgent);
        String bgViolationsPath;
        if (criticalViolationsPath.isEmpty()) {
            bgViolationsPath = runRppRun("background", previousJavamopAgent, backgroundAgent);
        } else {
            bgViolationsPath = runRppRun("background", criticalAgent, backgroundAgent);
        }
        try {
            updateCriticalAndBackgroundSpecs(criticalViolationsPath, bgViolationsPath, previousJavamopAgent);
        } catch (FileNotFoundException ex) {
            getLog().error("Failed to automatically update critical and background specs.");
            System.exit(1);
        }
        getLog().info("RPP end: " + timeFormatter.format(new Date()));
    }

}
