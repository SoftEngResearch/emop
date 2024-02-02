package edu.cornell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
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
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.cornell.emop.util.MethodsHelper;
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

@Mojo(name = "asm", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class AffectedSpecsMethodsMojo extends ImpactedMethodsMojo {

    private static final int CLASS_INDEX_IN_MSG = 3;
    private static final int SPEC_LINE_NUMBER = 4;
    private static final int TRIMMED_SPEC_NAME_INDEX = 4;
    private static final int SPEC_INDEX_IN_MSG = 5;
    private static final String METHODS_TO_SPECS_FILE_NAME = "methodsToSpecs.bin";

    /**
     * The path to the Javamop Agent JAR file.
     */
    @Parameter(property = "javamopAgent")
    protected String javamopAgent;

    /**
     * Whether to instrument classes that are not affected by code changes.
     * Setting this option to false triggers the ^c weak RPS variants.
     */
    @Parameter(property = "includeNonAffected", required = false, defaultValue = "true")
    protected boolean includeNonAffected;

    /**
     * Whether to instrument third-party libraries.
     * Setting this option to false triggers the ^l weak RPS variants.
     */
    @Parameter(property = "includeLibraries", required = false, defaultValue = "true")
    protected boolean includeLibraries;

    /**
     * A map from affected classes to affected specs, for debugging purposes.
     */
    protected Map<String, Set<String>> methodsToSpecs = new HashMap<>();
    protected Map<String, Set<String>> changedMethodsToSpecs = new HashMap<>();

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
    @Parameter(property = "methodsToSpecsContent", defaultValue = "MAP")
    private OutputContent methodsToSpecsContent;

    /**
     * Defines the output format of the map.
     */
    @Parameter(property = "methodsToSpecsFormat", defaultValue = "TXT")
    private OutputFormat methodsToSpecsFormat;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject mavenProject;

    /**
     * This method executes the AffectedSpecsMethods Mojo.
     * If there are no impacted methods, the method returns.
     * Otherwise, it invokes the AspectJ compiler to perform compile-time weaving
     * and computes the affected specifications by changed (or impacted) methods
     * based on the messages generated by the compiler.
     *
     * @throws MojoExecutionException If an error occurs while executing the Mojo.
     */
    public void execute() throws MojoExecutionException {
        super.execute();
        if (!dependencyChangeDetected && (
                computeImpactedMethods && getImpactedMethods().isEmpty()
                || getAffectedMethods().isEmpty()
            )) {
            return;
        }

        if (javamopAgent == null) {
            javamopAgent = getLocalRepository().getBasedir() + File.separator + "javamop-agent"
                    + File.separator + "javamop-agent"
                    + File.separator + "1.0"
                    + File.separator + "javamop-agent-1.0.jar";
        }

        getLog().info("[eMOP] Invoking the AffectedSpecsMethods Mojo...");
        long start = System.currentTimeMillis();
        // If only computing changed classes, then these lines can stay the same
        String[] arguments = createAJCArguments();
        Main compiler = new Main();
        MessageHandler mh = new MessageHandler();
        try {
            compiler.run(arguments, mh);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            getLog().error("Arguments: " + Arrays.asList(arguments));
            IMessage[] ms = mh.getMessages(IMessage.WEAVEINFO, false);
            getLog().error("IMessages: " + Arrays.asList(ms));
        }
        IMessage[] ms = mh.getMessages(IMessage.WEAVEINFO, false);
        long end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Compile-time weaving takes " + (end - start) + " ms");

        start = System.currentTimeMillis();
        methodsToSpecs = readMapFromFile(METHODS_TO_SPECS_FILE_NAME);

        try {
            computeMapFromMessage(ms);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        changedMethodsToSpecs
                .forEach((key, value) -> methodsToSpecs.merge(key, value, (oldValue, newValue) -> newValue));

        // Compute affected specs from changed methods or impacted methods
        if (computeImpactedMethods) {
            computeAffectedSpecs(getImpactedMethods());

        } else {
            computeAffectedSpecs(getAffectedMethods());
        }

        if (computeImpactedMethods) {
            getLog().info("[eMOP] Number of Impacted methods: " + getAffectedMethods().size());

        } else {
            getLog().info("[eMOP] Number of affected methods: " + getAffectedMethods().size());
        }
        if (dependencyChangeDetected) {
            // Revert to base RV, use all specs, include libraries and non-affected classes.
            affectedSpecs.addAll(Objects.requireNonNull(Util.getFullSpecSet(javamopAgent, "mop")));
            includeLibraries = true;
            includeNonAffected = true;
        }
        end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Compute affected specs takes " + (end - start) + " ms");
        start = System.currentTimeMillis();
        writeMapToFile(methodsToSpecs, METHODS_TO_SPECS_FILE_NAME, OutputFormat.TXT);
        end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Write affected specs to disk takes " + (end - start) + " ms");

        getLog().info("[eMOP] Number of changed classes: " + getChangedClasses().size());
        getLog().info("[eMOP] Number of new classes: " + getNewClasses().size());
        getLog().info("[eMOP] Number of messages to process: " + Arrays.asList(ms).size());
    }

    /**
     * Write map from class to specs in either text or binary format.
     * @param format Output format of the map, text or binary
     */
    private void writeMapToFile(Map<String, Set<String>> map, String fileName, OutputFormat format)
            throws MojoExecutionException {
        switch (format) {
            case BIN:
                try (FileOutputStream fos = new FileOutputStream(getArtifactsDir() + File.separator + fileName);
                     ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(map);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                break;
            case TXT:
            default:
                writeToText(methodsToSpecsContent, fileName);
        }
    }

    /**
     * Write class and specification information to text file.
     * @param content What to output
     */
    private void writeToText(OutputContent content, String fileName) throws MojoExecutionException {
        try (PrintWriter writer
                     = new PrintWriter(getArtifactsDir() + File.separator + "classToSpecs.txt")) {
            switch (methodsToSpecsContent) {
                case MAP:
                    for (Map.Entry<String, Set<String>> entry : methodsToSpecs.entrySet()) {
                        writer.println(entry.getKey() + ":" + String.join(",", entry.getValue()));
                    }
                    break;
                case SET:
                default:
                    for (String affectedSpec : affectedSpecs) {
                        writer.println(affectedSpec);
                    }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private Map<String, Set<String>> readMapFromFile(String fileName) throws MojoExecutionException {
        Map<String, Set<String>> map = new HashMap<>();
        File oldMap = new File(getArtifactsDir() + File.separator + fileName);
        if (oldMap.exists()) {
            try (FileInputStream fileInput = new FileInputStream(
                    getArtifactsDir() + File.separator + fileName);
                    ObjectInputStream objectInput = new ObjectInputStream(fileInput)) {
                map = (Map) objectInput.readObject();
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        return map;
    }

    private void computeAffectedSpecs(Set<String> methods) throws MojoExecutionException {
        for (String affectedMethod : methods) {
            // Convert method name from asm to java

            // Skip variables that are not in the format of a method
            if (!affectedMethod.matches(".*\\(.*\\)")) {
                continue;
            }

            String javaMethodName = MethodsHelper.convertAsmToJava(affectedMethod);
            Set<String> specs = methodsToSpecs.getOrDefault(javaMethodName, new HashSet<>());
            affectedSpecs.addAll(specs);
        }
    }

    /**
     * Compute a mapping from affected methods to specifications based on the
     * messages from AJC.
     * It utilizes relevant methods from the MethodsHelper class. The idea is
     * that we can use the line numbers of the methods to find the corresponding
     * specs inside that method. This is becayse the messages from AJC contain only
     * the class and line number of the spec not the method.
     *
     * @param ms An array of IMessage objects
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
            String klas = ChecksumUtil.toClassOrJavaName(klasName, false);
            URL url = loader.getResource(klas);
            String filePath = url.getPath();

            filePath = filePath.replace(".class", ".java").replace("target", "src").replace("test-classes", "test/java")
                    .replace("classes", "main/java");

            try {
                MethodsHelper.getMethodLineNumbers(filePath);
            } catch (ParserException exception) {
                getLog().warn("File contains interface only, no methods found in " + filePath);
            }

            String method = MethodsHelper.getWrapMethod(filePath, specLineNumber);
            if (method == null) {
                getLog().warn("Cannot find method for " + filePath + " at line " + specLineNumber);
                continue;
            }
            String key = klas.replace(".class", "") + "#" + method;
            Set<String> methodSpecs = changedMethodsToSpecs.getOrDefault(key, new HashSet<>());
            changedMethodsToSpecs.put(key, methodSpecs);
            methodSpecs.add(spec);
        }
    }

    /**
     * This method creates an array of arguments for the AspectJ compiler (AJC).
     * It extracts aspects from the jar and writes them to a file, then prepares a
     * list of source files to weave.
     * The method also extracts an argument file from the jar and prepares the
     * classpath for AJC.
     * Finally, it returns an array of arguments that can be used to call AJC.
     *
     * @return An array of arguments for the AspectJ compiler
     * @throws MojoExecutionException if an error occurs during execution
     */
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

    /**
     * Returns the classpath for Surefire as a String.
     *
     * @return The classpath for Surefire as a String
     * @throws MojoExecutionException if an error occurs during execution
     */
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
