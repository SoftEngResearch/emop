package edu.cornell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import edu.cornell.emop.maven.SurefireMojoInterceptor;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "rpp", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "rpp")
public class RppMojo extends RppHandlerMojo {

    /**
     * Runs maven surefire.
     * @throws MojoExecutionException
     */
    private void invokeSurefire() throws MojoExecutionException {
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
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Relocates the generated violation-counts file.
     * @param mode the identifier for the relocated violation-counts file (either "critical" or "background")
     * @throws MojoExecutionException
     */
    private void moveViolationCounts(String mode) throws MojoExecutionException {
        // If we get a handle on violation-counts from VMS, then we don't have to do this in the first place...
        File violationCounts = new File(getBasedir() + File.separator + "violation-counts");
        if (!violationCounts.exists()) {
            getLog().info("violation-counts file was not produced, skipping moving...");
        }
        File newViolationCounts = new File(getArtifactsDir() + File.separator + mode + "-violation-counts.txt");
        try {
            Files.move(violationCounts.toPath(), newViolationCounts.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * This mojo runs RPP.
     * @throws MojoExecutionException
     */
    public void execute() throws MojoExecutionException {
        // by the time this method is invoked, we have finished invoking the critical specs surefire run
        moveViolationCounts("critical");
        String backgroundAgent = System.getProperty("background-agent");
        if (!backgroundAgent.isEmpty()) {
            String previousJavamopAgent = System.getProperty("rpp-agent");
            System.setProperty("previous-javamop-agent", previousJavamopAgent);
            System.setProperty("rpp-agent", backgroundAgent);
            invokeSurefire();
            moveViolationCounts("background");
        } else { // edge case where critical phase runs all specs
            getLog().info("No specs to monitor for background phase, terminating...");
        }
    }

}
