package edu.cornell;

import edu.cornell.emop.maven.SurefireMojoInterceptor;
import edu.cornell.emop.util.Util;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;

@Mojo(name = "run-rpp", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "rpp")
public class RunRppMojo extends RppHandlerMojo {

    public void invokeSurefire() {
        PrintStream stdout = System.out;
//        PrintStream ps = new PrintStream(new FileOutputStream(new File(System.getProperty("user.dir") + )));
        try {
            SurefireMojoInterceptor.sfMojo.getClass().getMethod("execute").invoke(SurefireMojoInterceptor.sfMojo);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public void execute() throws MojoExecutionException {
        // need to move violation-counts file
        String backgroundAgent = System.getProperty("background-agent");
        if (!backgroundAgent.isEmpty()) {
            System.setProperty("rpp-agent", backgroundAgent);
            invokeSurefire();
        } else {
            getLog().info("No specs to monitor for background phase, terminating...");
        }
    }

}
