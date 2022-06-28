package edu.cornell;

import edu.cornell.emop.util.Util;
import edu.illinois.starts.helpers.Writer;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.surefire.booter.Classpath;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Mojo(name = "affected-specs", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class AffectedSpecsMojo extends ImpactedClassMojo {
    public void execute() throws MojoExecutionException {
        super.execute();
        getLog().info( "[eMOP] Invoking the AffectedSpecs Mojo...");
//        getLog().info("[EMOP] Impacted classes: " + getImpacted());
        // find all the available specs
        String destinationDir = getArtifactsDir() + File.separator + "weaved-specs";
        String aspectList = getArtifactsDir() + File.separator + "aspects.lst";
        List<String> aspects = find(destinationDir, ".aj", "weaved-specs");
        Writer.writeToFile(aspects, aspectList);
        String sourceList = getArtifactsDir() + File.separator + "sources.lst";
        makeSourcesFile(sourceList, getImpacted());
        String argsList = getArtifactsDir() + File.separator + "argz";
        List<String> args = find(argsList, ".lst", "argz");
        String classpath = getClassPath();
        getLog().info("[eMOP] classpath: " + classpath);
    }

    private String getClassPath() throws MojoExecutionException {
        Classpath sfClassPath = getSureFireClassPath();
        String sfPathString = Writer.pathToString(sfClassPath.getClassPath());
        return sfPathString;
    }

    private void makeSourcesFile(String sourceList, Set<String> impacted) {
        Set<String> classes = new HashSet<>();
        for (String klas : impacted) {
            if (klas.contains("$")) {
                klas = klas.substring(0, klas.indexOf("$"));
            }
            klas = klas.replace(".", File.separator) + ".java";
            File test = new File(getTestSourceDirectory().getAbsolutePath() + File.separator + klas);
            File source = new File(test.getAbsolutePath().replace("src" + File.separator + "test",
                    "src" + File.separator + "main"));
            if (source.exists()) {
                classes.add(source.getAbsolutePath());
            } else if (test.exists()){
                classes.add(test.getAbsolutePath());
            } else {
                getLog().error("Source file not found: " + source.getAbsolutePath());
                getLog().error("Test file not found: " + test.getAbsolutePath());
            }
        }
        Writer.writeToFile(classes, sourceList);
    }

    private List<String> find(String destinationDir, String extension, String name) {
        List<String> files = new ArrayList<>();
        File destination = new File(destinationDir);
        if (destination.exists()) {
            files.addAll(Util.findFilesOfType(destination, extension));
        } else {
            URL allSpecsDir = AffectedSpecsMojo.class.getClassLoader().getResource(name);
            if ((allSpecsDir != null) && allSpecsDir.getProtocol().equals("jar")) {
                destination.mkdirs();
                try {
                    JarFile jarfile = ((JarURLConnection) allSpecsDir.openConnection()).getJarFile();
                    Enumeration<JarEntry> entries = jarfile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.getName().contains(extension)) {
                            files.add(getArtifactsDir() + entry.getName());
                            InputStream inputStream = jarfile.getInputStream(entry);
                            File spec = new File(destinationDir + File.separator + entry.getName().replace(name + File.separator, ""));
                            if (!spec.exists()) {
                                FileOutputStream outputStream = new FileOutputStream(spec);
                                while (inputStream.available() > 0) {
                                    outputStream.write(inputStream.read());
                                }
                            }
                        }
                    }
                } catch (IOException | MojoExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return files;
    }
}
