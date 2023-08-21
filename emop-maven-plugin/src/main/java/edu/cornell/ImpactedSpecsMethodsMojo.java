package edu.cornell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.cornell.emop.util.Util;
import edu.illinois.starts.helpers.Writer;
import edu.illinois.starts.util.ChecksumUtil;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.surefire.booter.Classpath;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.tools.ajc.Main;
import org.jboss.forge.roaster.ParserException;

import edu.cornell.emop.util.MethodsHelper;

// @Mojo(name = "affected-specs-methods", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Mojo(name = "asm", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class ImpactedSpecsMethodsMojo extends ImpactedMethodsMojo {

    private static final int CLASS_INDEX_IN_MSG = 3;
    private static final int SPEC_LINE_NUMBER = 4;
    private static final int TRIMMED_SPEC_NAME_INDEX = 4;
    private static final int SPEC_INDEX_IN_MSG = 5;

    /**
     * A map from affected classes to affected specs, for debugging purposes.
     */
    protected Map<String, Set<String>> methodsToSpecs = new HashMap<>();

    /**
     * A set of affected specs to monitor for javamop agent.
     */
    protected Set<String> affectedSpecs = new HashSet<>();

    private enum OutputContent {
        MAP, SET
    }

    private enum OutputFormat {
        BIN, TXT
    }

    /**
     * Defines whether the output content is a set or a map.
     */
    @Parameter(property = "methodsToSpecsContent", defaultValue = "SET")
    private OutputContent methodsToSpecsContent;

    /**
     * Defines the output format of the map.
     */
    @Parameter(property = "methodsToSpecsFormat", defaultValue = "TXT")
    private OutputFormat methodsToSpecsFormat;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject mavenProject;

    public void execute() throws MojoExecutionException {
        super.execute();
        boolean computeImpactedMethods = getComputeImpactedMethods();
        if (computeImpactedMethods && getImpactedMethods().isEmpty()) {

            return;

        } else if (getAffectedMethods().isEmpty()) {
            return;
        }

        getLog().info("[eMOP] Invoking the AffectedSpecsMethods Mojo...");
        long start = System.currentTimeMillis();
        // If only computing changed classes, then these lines can stay the same
        String[] arguments = createAJCArguments();
        Main compiler = new Main();
        MessageHandler mh = new MessageHandler();
        compiler.run(arguments, mh);
        IMessage[] ms = mh.getMessages(IMessage.WEAVEINFO, false);

        long end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Compile-time weaving takes " + (end - start) + " ms");
        start = System.currentTimeMillis();

        try {
            computeMapFromMessage(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (computeImpactedMethods) {
            computeImpactedSpecs();

        } else {
            computeAffectedSpecs();
        }

        end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Compute affected specs takes " + (end - start) +
                " ms");
        start = System.currentTimeMillis();
        end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Write affected specs to disk takes " + (end -
                start) + " ms");

        if (computeImpactedMethods) {
            getLog().info("[eMOP] Number of Impacted methods: " + getAffectedMethods().size());

        } else {
            getLog().info("[eMOP] Number of affected methods: " + getAffectedMethods().size());
        }

        getLog().info("[eMOP] Number of changed classes: " + getChangedClasses().size());
        getLog().info("[eMOP] Number of new classes: " + getNewClasses().size());
        getLog().info("[eMOP] Number of messages to process: " +
                Arrays.asList(ms).size());
    }

    private void computeAffectedSpecs() throws MojoExecutionException {
        for (String affectedMethod : getAffectedMethods()) {
            // Convert method name from asm to java
            String javaMethodName = MethodsHelper.convertAsmToJava(affectedMethod);
            Set<String> specs = methodsToSpecs.getOrDefault(javaMethodName, new HashSet<>());
            affectedSpecs.addAll(specs);
        }
    }

    private void computeImpactedSpecs() throws MojoExecutionException {
        for (String affectedMethod : getImpactedMethods()) {
            // Convert method name from asm to java
            String javaMethodName = MethodsHelper.convertAsmToJava(affectedMethod);
            Set<String> specs = methodsToSpecs.getOrDefault(javaMethodName, new HashSet<>());
            affectedSpecs.addAll(specs);
        }
    }

    /**
     * Compute a mapping from affected classes to specifications based on the
     * messages from AJC.
     * 
     * @param ms An array of IMessage objects
     * @throws Exception
     */
    private void computeMapFromMessage(IMessage[] ms) throws Exception {
        Classpath sfClassPath = getSureFireClassPath();
        ClassLoader loader = createClassLoader(sfClassPath);
        for (IMessage message : ms) {
            String[] lexedMessage = message.getMessage().split("'");
            String klasName = lexedMessage[CLASS_INDEX_IN_MSG];
            String spec = lexedMessage[SPEC_INDEX_IN_MSG].substring(TRIMMED_SPEC_NAME_INDEX);
            int specLineNumber = Integer
                    .parseInt(lexedMessage[SPEC_LINE_NUMBER].split(" ")[1].split(":")[1].replace(")", ""));

            String klas = ChecksumUtil.toClassName(klasName);
            URL url = loader.getResource(klas);
            String filePath = url.getPath();

            filePath = filePath.replace(".class", ".java").replace("target", "src").replace("test-classes", "test/java")
                    .replace("classes", "main/java");

            try {
                MethodsHelper.getMethodLineNumbers(filePath);
            } catch (ParserException e) {
                getLog().warn("Cannot find method line numbers for " + filePath);
            }
            String method = MethodsHelper.getWrapMethod(filePath, specLineNumber);
            if (method == null) {
                getLog().warn("Cannot find method for " + filePath + " at line " + specLineNumber);
                continue;
            }
            String key = filePath.replace(".java", "#") + method;
            Set<String> methodSpecs = methodsToSpecs.getOrDefault(key, new HashSet<>());
            methodSpecs.add(spec);
            key = klas.replace(".class", "") + "#" + method;
            methodsToSpecs.put(key, methodSpecs);
        }
    }

    private String[] createAJCArguments() throws MojoExecutionException {
        // extract the aspects for all available specs from the jar and make a list of
        // them in a file
        String destinationDir = getArtifactsDir() + File.separator + "weaved-specs";
        String aspectList = getArtifactsDir() + File.separator + "aspects.lst";
        List<String> aspects = extractOrFind(destinationDir, ".aj", "weaved-specs");
        Writer.writeToFile(aspects, aspectList);

        // the source files that we want to weave are the impacted classes, write them
        // to a file
        String sourceList = getArtifactsDir() + File.separator + "sources.lst";

        // Get changed instead of impacted to reduce compile time
        // get both changed (existing) and new classes
        makeSourcesFile(sourceList, getNewClasses());
        // extract the argument file that we want to use from the jar to the .starts
        // directory
        String argsList = getArtifactsDir() + File.separator + "argz";
        List<String> args = extractOrFind(argsList, ".lst", "argz");
        // prepare the classpath that we want to call AJC with
        String classpath = getClassPath() + File.pathSeparator + getRuntimeJars();
        // prepare an array of arguments that the aspectj compiler will be called with
        return new String[] { "-classpath", classpath, "-argfile", aspectList, "-argfile", sourceList, "-argfile",
                args.get(0), "-d", "weaved-bytecode" };
    }

    /**
     * We need to put aspectjrt and rv-monitor-rt on the classpath for AJC.
     * 
     * @return classpath with only the runtime jars
     * @throws MojoExecutionException throws MojoExecutionException
     */
    protected String getRuntimeJars() throws MojoExecutionException {
        String destinationDir = getArtifactsDir() + File.separator + "lib";
        List<String> runtimeJars = extractOrFind(destinationDir, ".jar", "lib");
        return String.join(File.pathSeparator, runtimeJars);
    }

    protected String getClassPath() throws MojoExecutionException {
        return Writer.pathToString(getSureFireClassPath().getClassPath());
    }

    /**
     * Given a path to a class file, returns a path to its corresponding source
     * file. Assumes a standard directory
     * layout, i.e., one where the source for {@code com.abc.A} resides in
     * {@code sourceDir/com/abc/A.java}.
     * 
     * @param classFile  the path to the class file
     * @param classesDir the base class file directory
     * @param sourceDir  the base sources directory
     * @return the path to the source file
     */
    private static Path classFileToSource(Path classFile, Path classesDir, Path sourceDir) {
        Path parent = sourceDir.resolve(classesDir.relativize(classFile)).getParent();
        return parent.resolve(classFile.getFileName().toString().replace(".class", ".java"));
    }

    private void makeSourcesFile(String sourceList, Set<String> newClasses) throws MojoExecutionException {
        Set<String> classes = new HashSet<>();
        List<String> compileSourceRoots = mavenProject.getCompileSourceRoots();
        List<String> testCompileSourceRoots = mavenProject.getTestCompileSourceRoots();
        List<Path> sourceDirs = Stream.concat(compileSourceRoots.stream(), testCompileSourceRoots.stream())
                .map(path -> Paths.get(path).toAbsolutePath())
                .collect(Collectors.toList());

        classes: for (String newClass : newClasses) {
            if (newClass.contains("$")) {
                newClass = newClass.substring(0, newClass.indexOf("$"));
            }
            String relativeSourcePath = newClass.replace(".", File.separator) + ".java";

            for (Path dir : sourceDirs) {
                File source = dir.resolve(relativeSourcePath).toFile();
                if (source.exists()) {
                    classes.add(source.getAbsolutePath());
                    continue classes;
                }
            }

            // Source file not found in any standard directory
            getLog().error("No source file found for class " + newClass);
        }

        Path mainClassesDir = getClassesDirectory().toPath().toAbsolutePath();
        Path testClassesDir = getTestClassesDirectory().toPath().toAbsolutePath();
        classes: for (String changedClass : getChangedClasses()) {
            if (changedClass.contains("$")) {
                changedClass = changedClass.substring(0, changedClass.indexOf('$')) + ".class";
            }

            try {
                Path classFile = Paths.get(new URI(changedClass)).toAbsolutePath();
                Path classesDir = null;

                if (!classFile.toFile().exists()) {
                    getLog().warn("Class file does not exist: " + classFile.toString());
                    continue;
                }

                if (classFile.startsWith(mainClassesDir)) {
                    classesDir = mainClassesDir;
                } else if (classFile.startsWith(testClassesDir)) {
                    classesDir = testClassesDir;
                } else {
                    getLog().error("Class file not found in standard directories: " + classFile.toString());
                    continue;
                }

                for (Path dir : sourceDirs) {
                    Path sourceFile = classFileToSource(classFile, classesDir, dir);
                    if (sourceFile.toFile().exists()) {
                        classes.add(sourceFile.toString());
                        continue classes;
                    }
                }

                // Source file not found in any standard directory
                getLog().error("No source file found for class file " + classFile.toString());
            } catch (URISyntaxException ex) {
                throw new MojoExecutionException("Couldn't parse URI for changed class", ex);
            }
        }

        Writer.writeToFile(classes, sourceList);
    }

    private List<String> extractOrFind(String destinationDir, String extension, String name) {
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
