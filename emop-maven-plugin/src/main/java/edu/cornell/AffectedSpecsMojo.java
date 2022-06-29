package edu.cornell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import edu.cornell.emop.util.Util;
import edu.illinois.starts.helpers.Writer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.tools.ajc.Main;

@Mojo(name = "affected-specs", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class AffectedSpecsMojo extends ImpactedClassMojo {
    public void execute() throws MojoExecutionException {
        super.execute();
        getLog().info("[eMOP] Invoking the AffectedSpecs Mojo...");
        String[] arguments = createAJCArguments();
        Main compiler = new Main();
        MessageHandler mh = new MessageHandler();
        compiler.run(arguments, mh);
        IMessage[] ms = mh.getMessages(IMessage.WEAVEINFO, false);
        Writer.writeToFile(Arrays.asList(ms), getArtifactsDir() + File.separator + "join-points");
        getLog().info("[eMOP] Number of impacted classes: " + getImpacted().size());
        getLog().info("[eMOP] Number of messages to process: " + Arrays.asList(ms).size());
    }

    private String[] createAJCArguments() throws MojoExecutionException {
        // extract the aspects for all available specs from the jar and make a list of them in a file
        String destinationDir = getArtifactsDir() + File.separator + "weaved-specs";
        String aspectList = getArtifactsDir() + File.separator + "aspects.lst";
        List<String> aspects = find(destinationDir, ".aj", "weaved-specs");
        Writer.writeToFile(aspects, aspectList);
        // the source files that we want to weave are the impacted classes, write them to a file
        String sourceList = getArtifactsDir() + File.separator + "sources.lst";
        makeSourcesFile(sourceList, getImpacted());
        // extract the argument file that we want to use from the jar to the .starts directory
        String argsList = getArtifactsDir() + File.separator + "argz";
        List<String> args = find(argsList, ".lst", "argz");
        // prepare the classpath that we want to call AJC with
        String classpath = getClassPath() + File.pathSeparator + getRuntimeJars();
        // prepare an array of arguments that the aspectj compiler will be called with
        return new String[]{ "-classpath", classpath, "-argfile", aspectList, "-argfile", sourceList, "-argfile",
                args.get(0), "-d", "none"};
    }

    /**
     * We need to put aspectjrt and rv-monitor-rt on the classpath for AJC.
     * @return classpath with only the runtime jars
     * @throws MojoExecutionException throws MojoExecutionException
     */
    private String getRuntimeJars() throws MojoExecutionException {
        String destinationDir = getArtifactsDir() + File.separator + "lib";
        List<String> runtimeJars = find(destinationDir, ".jar", "lib");
        return String.join(File.pathSeparator, runtimeJars);
    }

    private String getClassPath() throws MojoExecutionException {
        return Writer.pathToString(getSureFireClassPath().getClassPath());
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
            } else if (test.exists()) {
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
                            File spec = new File(destinationDir + File.separator
                                    + entry.getName().replace(name + File.separator, ""));
                            if (!spec.exists()) {
                                FileOutputStream outputStream = new FileOutputStream(spec);
                                while (inputStream.available() > 0) {
                                    outputStream.write(inputStream.read());
                                }
                            }
                        }
                    }
                } catch (IOException | MojoExecutionException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return files;
    }
}

// ajc command that Owolabi ran locally:

/*
time ajc -classpath /home/owolabi/.m2/repository/junit/junit/4.12/junit-4.12.jar:/home/owolabi/.m2/repository/org/hamcre
st/hamcrest-core/1.3/hamcrest-core-1.3.jar:/home/owolabi/.m2/repository/javax/servlet/servlet-api/2.4/servlet-api-2.4.ja
r:/home/owolabi/.m2/repository/portlet-api/portlet-api/1.0/portlet-api-1.0.jar:/home/owolabi/.m2/repository/commons-io/c
ommons-io/2.2/commons-io-2.2.jar:target/classes:target/test-classes:/home/owolabi/projects/emop/scripts/lib/rv-monitor-r
t.jar:/home/owolabi/projects/emop/scripts/lib/aspectjrt.jar -argfile args.lst -d test-ajc -argfile aspects.lst -argfile
sources.lst &> a.txt
 */

// String processing command to get the map of specs to tests

/*
paste -d, <(grep "Join point" ${weave_out} | cut -d\' -f4) <(grep "Join point" ${weave_out} | rev | cut -d\( -f1 | rev |
 cut -d. -f1) | sort -u
 */
