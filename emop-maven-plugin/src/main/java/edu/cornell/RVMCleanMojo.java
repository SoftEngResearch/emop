package edu.cornell;

import java.io.File;
import java.nio.file.Paths;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "clean-rvm", requiresDirectInvocation = true)
public class RVMCleanMojo extends AbstractMojo {
    public void execute() throws MojoExecutionException {
        File vc = Paths.get(System.getProperty("user.dir"), "violation-counts").toFile();
        if (vc.isFile()) {
            vc.delete();
        }
    }
}
