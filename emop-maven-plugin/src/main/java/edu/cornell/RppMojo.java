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
import edu.cornell.emop.util.Util;
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
     * @throws MojoExecutionException when the surefire invocation fails.
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
     * This mojo runs RPP.
     * @throws MojoExecutionException if RPP fails.
     */
    public void execute() throws MojoExecutionException {
        // by the time this method is invoked, we have finished invoking the critical specs surefire run
        boolean criticalCountsExist = Util.moveViolationCounts(getBasedir(), getArtifactsDir(), "critical");
        if (!criticalCountsExist) {
            getLog().info("violation-counts file for critical run was not produced, skip moving...");
        }
        String backgroundAgent = System.getProperty("background-agent");
        if (!backgroundAgent.isEmpty()) {
            String previousJavamopAgent = System.getProperty("rpp-agent");
            System.setProperty("previous-javamop-agent", previousJavamopAgent);
            System.setProperty("rpp-agent", backgroundAgent);
            invokeSurefire();
            boolean backgroundCountsExist = Util.moveViolationCounts(getBasedir(), getArtifactsDir(), "background");
            if (!backgroundCountsExist) {
                getLog().info("violation-counts file for background run was not produced, skipping moving...");
            }
        } else { // edge case where critical phase runs all specs
            getLog().info("No specs to monitor for background phase, terminating...");
        }
    }

}
