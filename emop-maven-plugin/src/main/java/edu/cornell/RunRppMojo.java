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
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;

@Mojo(name = "run-rpp", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "rpp")
public class RunRppMojo extends RppHandlerMojo {

    public void invokeSurefire() {
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
        if (this.javamopAgent == null) {
            javamopAgent = localRepository.getBasedir() + File.separator + "javamop-agent"
                    + File.separator + "javamop-agent"
                    + File.separator + "1.0"
                    + File.separator + "javamop-agent-1.0.jar";
        }
        File javamopAgentFile = new File(javamopAgent);
        this.backgroundRunJavaMop = new File(javamopAgentFile.getParentFile(), "background-javamop.jar");
        try {
            Files.copy(javamopAgentFile.toPath(), backgroundRunJavaMop.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Set<String> backgroundSpecs = parseSpecsFile(backgroundSpecsFile);
            Util.generateNewMonitorFile(
                    System.getProperty("user.dir") + File.separator + "background-ajc.xml", backgroundSpecs);
            Util.replaceFileInJar(this.backgroundRunJavaMop.getAbsolutePath(), "/META-INF/aop-ajc.xml",
                    System.getProperty("user.dir") + File.separator + "background-ajc.xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.setProperty("rpp-agent", this.backgroundRunJavaMop.getAbsolutePath());
        System.out.println();
        invokeSurefire();
        // removing the jars
    }

}
