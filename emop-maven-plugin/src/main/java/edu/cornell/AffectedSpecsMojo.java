package edu.cornell;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Mojo(name = "affected-specs")
public class AffectedSpecsMojo extends ImpactedClassMojo {
    public void execute() throws MojoExecutionException {
        super.execute();
        getLog().info( "[eMOP] Invoking the AffectedSpecs Mojo...");
        //  getLog().info("[EMOP] Impacted classes: " + getImpacted());
        // find all the available specs
        List<String> allSpecs = getAllSpecs();
        getLog().info("[EMOP] All Specs: " + allSpecs);
        getLog().info("[EMOP] .STARTS DIR: " + getArtifactsDir());
    }

    private List<String> getAllSpecs() {
        List<String> specs = new ArrayList<>();
        URL allSpecsDir = AffectedSpecsMojo.class.getClassLoader().getResource("all-specs");
        if ((allSpecsDir != null) && allSpecsDir.getProtocol().equals("jar")) {
            try {
                JarURLConnection connection = (JarURLConnection) allSpecsDir.openConnection();
                JarFile jarfile = connection.getJarFile();
                getLog().info("JAR: " + jarfile.getName());
                Enumeration<JarEntry> entries = jarfile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry =  entries.nextElement();
                    if (entry.getName().contains(".mop")) {
                        getLog().info("ENTRY: " + entry.getName());
                        specs.add(entry.getName());
                        InputStream inputStream = jarfile.getInputStream(entry);
                        FileOutputStream outputStream = new FileOutputStream(new File(getArtifactsDir() + File.separator + entry.getName().replace("all-specs/", "")));
                        while (inputStream.available() > 0) {
                            outputStream.write(inputStream.read());
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (MojoExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return specs;
    }
}
