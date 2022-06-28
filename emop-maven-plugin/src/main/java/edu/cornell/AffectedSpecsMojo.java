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
        String destinationDir = getArtifactsDir() + File.separator + "all-specs";
        //  getLog().info("[EMOP] Impacted classes: " + getImpacted());
        // find all the available specs
        List<String> allSpecs = getAllSpecs(destinationDir);
        getLog().info("[EMOP] All Specs: " + allSpecs);
        getLog().info("[EMOP] .STARTS DIR: " + getArtifactsDir());
    }

    private List<String> getAllSpecs(String destinationDir) {
        List<String> specs = new ArrayList<>();
        URL allSpecsDir = AffectedSpecsMojo.class.getClassLoader().getResource("all-specs");
        if ((allSpecsDir != null) && allSpecsDir.getProtocol().equals("jar")) {
            try {
                new File(destinationDir).mkdirs();
                JarFile jarfile = ((JarURLConnection) allSpecsDir.openConnection()).getJarFile();
                Enumeration<JarEntry> entries = jarfile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry =  entries.nextElement();
                    if (entry.getName().contains(".mop")) {
                        specs.add(entry.getName());
                        InputStream inputStream = jarfile.getInputStream(entry);
                        File spec = new File(destinationDir + File.separator + entry.getName().replace("all-specs/", ""));
                        if (!spec.exists()) {
                            FileOutputStream outputStream = new FileOutputStream(spec);
                            while (inputStream.available() > 0) {
                                outputStream.write(inputStream.read());
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return specs;
    }
}
