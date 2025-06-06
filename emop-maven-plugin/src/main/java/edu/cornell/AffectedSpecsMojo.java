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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import edu.illinois.starts.enums.Granularity;
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

@Mojo(name = "affected-specs", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
public class AffectedSpecsMojo extends ImpactedComponentsMojo {

    private static final int CLASS_INDEX_IN_MSG = 3;
    private static final int SPEC_LINE_NUMBER = 4;
    private static final int TRIMMED_SPEC_NAME_INDEX = 4;
    private static final int SPEC_INDEX_IN_MSG = 5;
    private static final String CLASSES_TO_SPECS_FILE_NAME = "classesToSpecs.bin";
    private static final String CLASSES_TO_SPECS_DEBUG_FILE_NAME = "classesToSpecs.txt";
    private static final String METHODS_TO_SPECS_FILE_NAME = "methodsToSpecs.bin";
    private static final String METHODS_TO_SPECS_DEBUG_FILE_NAME = "methodsToSpecs.txt";

    /** Path to the Javamop Agent JAR file. */
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
     * Whether to perform instrumentation at a finer granularity than class level.
     * Setting this option to true will also include finer granularity like method level.
     * TODO: Although it is called finerInstrumentation, this approach actually can only reduce monitoring.
     */
    @Parameter(property = "finerInstrumentation", required = false, defaultValue = "false")
    protected boolean finerInstrumentation;

    /**
     * Whether to use an alternative implementation of finer instrumentation.
     * Note that this depends on the parameter of finerInstrumentation being set to true,
     * otherwise it will not take effect.
     */
    @Parameter(property = "finerInstrumentationAlt", required = false, defaultValue = "false")
    protected boolean finerInstrumentationAlt;

    /**
     * Whether to find affected specs by using a finer-grained mapping to specs.
     * Setting this to true will enable the use of method -> specs mapping if there is one,
     * instead of reducing to class -> specs mapping.
     */
    @Parameter(property = "finerSpecMapping", required = false, defaultValue = "false")
    protected boolean finerSpecMapping;

    /**
     * A mapping from class to all the line numbers that are impacted.
     * This data structure enables the use of thisJoinPointStaticPart over
     * the more expensive thisEnclosingJoinPointStaticPart.
     */
    protected HashMap<String, HashSet<Integer>> classToImpactedLineNumbers = new HashMap<>();

    /**
     * A map from affected classes to affected specs, for debugging purposes.
     */
    // TODO: Duplicate
    protected Map<String, Set<String>> classToSpecs = new HashMap<>();
    protected Map<String, Set<String>> classesToSpecs = new HashMap<>();
    protected Map<String, Set<String>> methodsToSpecs = new HashMap<>();
    protected Map<String, Set<String>> methodToSpecsUpdateMap = new HashMap<>();
    protected Map<String, Set<String>> classToSpecsUpdateMap = new HashMap<>();

    /**
     * A set of affected specs to monitor for javamop agent.
     */
    protected Set<String> affectedSpecs = new HashSet<>();

    private enum OutputContent { MAP, SET }

    enum OutputFormat { BIN, TXT }

    private Map<String, Set<String>> changedMap = new HashMap<>();

    // TODO: Consider merging the following two:
    /**
     * Defines whether the output content is a set or a map.
     */
    @Parameter(property = "classToSpecsContent", defaultValue = "SET")
    private OutputContent classToSpecsContent;

    /**
     * Defines whether the output content is a set or a map.
     */
    @Parameter(property = "methodsToSpecsContent", defaultValue = "MAP")
    private OutputContent methodsToSpecsContent;

    // TODO: Consider merging the following two:
    /**
     * Defines the output format of the map.
     */
    @Parameter(property = "classToSpecsFormat", defaultValue = "TXT")
    private OutputFormat classToSpecsFormat;

    /**
     * Defines the output format of the map.
     */
    @Parameter(property = "methodsToSpecsFormat", defaultValue = "TXT")
    private OutputFormat methodsToSpecsFormat;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject mavenProject;

    /**
     * Doc from METHODS:
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
        if (javamopAgent == null) {
            javamopAgent = getLocalRepository().getBasedir() + File.separator + "javamop-agent"
                    + File.separator + "javamop-agent"
                    + File.separator + "1.0"
                    + File.separator + "javamop-agent-1.0.jar";
        }
        if (getGranularity() == Granularity.CLASS || getGranularity() == Granularity.FINE) {
            if (!dependencyChanged && getImpacted().isEmpty()) {
                getLog().info("[eMOP] No impacted classes, returning...");
                return;
            }
            getLog().info("[eMOP] Invoking the AffectedSpecs Mojo...");

            IMessage[] ms = doCompileTimeInstrumentation();

            long start = System.currentTimeMillis();
            classToSpecs = readMapFromFile("classToSpecs.bin");
            computeMapFromMessage(ms);

            List<String> classesToInstrument = getNewlyUsedLibraries();
            if (classesToInstrument != null) {
                IMessage[] ms2 = doCompileTimeInstrumentation(classesToInstrument);
                computeMapFromMessage(ms2);
                Util.deleteRecursively(Paths.get(getArtifactsDir(), "lib-jars-tmp"));

                changedMap.forEach((key, value) -> classToSpecs.merge(key, value, (oldValue, newValue) -> newValue));
                // Handle case where we instrumented but we did not find any specs from ajc
                for (String klass : classesToInstrument) {
                    if (!classToSpecs.containsKey(klass)) {
                        classToSpecs.put(klass, new HashSet<>());
                    }
                }
            } else {
                changedMap.forEach((key, value) -> classToSpecs.merge(key, value, (oldValue, newValue) -> newValue));
            }

            computeAffectedSpecs(dependencyChanged);

            long end = System.currentTimeMillis();
            getLog().info("[eMOP Timer] Compute affected specs takes " + (end - start) + " ms");

            start = System.currentTimeMillis();
            // Write map
            writeMapToFile(OutputFormat.BIN);
            // Write affectedSpecs
            // TODO: This is not really a map anymore, make sure the implementation matches the name
            writeMapToFile(OutputFormat.TXT);
            end = System.currentTimeMillis();
            getLog().info("[eMOP Timer] Write affected specs to disk takes " + (end - start) + " ms");

            getLog().info("[eMOP] Number of impacted classes: " + getImpacted().size());
            getLog().info("[eMOP] Number of messages to process: " + Arrays.asList(ms).size());
        } else if (getGranularity() == Granularity.METHOD) {
            // TODO: Shouldn't this part be done by the Monitor Mojo?
            if (dependencyChanged) {
                // Revert to base RV, use all specs, include libraries and non-affected classes.
                affectedSpecs.addAll(Objects.requireNonNull(Util.getFullSpecSet(javamopAgent, "mop")));
                includeLibraries = true;
                includeNonAffected = true;
            }
            // This segment has to execute before return, otherwise it will pollute the next run
            recompileBaseAspect();
            // Why do it here? Because the program might exit early to revert to BaseRV
            // and use a modified version of BaseAspect instead, which we do not want.
            Util.replaceFileInJar(javamopAgent, "/mop/BaseAspect.class",
                    getArtifactsDir() + File.separator + "mop" + File.separator + "BaseAspect.class");
            if (!dependencyChanged
                    && (getComputeImpactedMethods() && getImpactedMethods().isEmpty() || getAffectedMethods().isEmpty())
            ) {
                return;
            }
            getLog().info("[eMOP] Invoking the AffectedSpecsMethods Mojo...");

            IMessage[] ms = doCompileTimeInstrumentation();

            long start = System.currentTimeMillis();
            methodsToSpecs = readMapFromFile(METHODS_TO_SPECS_FILE_NAME);

            try {
                MethodsHelper.loadMethodsToLineNumbers(getArtifactsDir());
//                System.out.println("! computeMapFromMessage on ms");
                computeMapFromMessage(ms);

                List<String> classesToInstrument = getNewlyUsedLibraries();
                if (classesToInstrument != null) {
                    IMessage[] ms2 = doCompileTimeInstrumentation(classesToInstrument);
                    computeMapFromMessage(ms2);
                    Util.deleteRecursively(Paths.get(getArtifactsDir(), "lib-jars-tmp"));
                }
                MethodsHelper.saveMethodsToLineNumbers(getArtifactsDir());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (finerInstrumentation) {
                if (!dependencyChanged) {
                    Util.setEnv("IMPACTED_METHODS_FILE", getArtifactsDir() + File.separator + "impactedMethods.bin");
                    getLog().info("IMPACTED_METHODS_FILE is set to " + System.getenv("IMPACTED_METHODS_FILE"));
                    try (FileOutputStream fos
                                 = new FileOutputStream(getArtifactsDir() + File.separator + "impactedMethods.bin");
                         ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                        if (finerInstrumentationAlt) {
                            // TODO: Repeated code with the process of output to impactedMethods.txt.
                            for (String impactedMethod : getImpactedMethods()) {
                                if (!impactedMethod.matches(".*\\(.*\\)")) {
                                    continue;
                                }
                                String javaFormat = MethodsHelper.convertAsmToJava(impactedMethod);
                                // Looks like: org/mitre/dsmiley/httpproxy/ProxyServletTest#tearDown(),67,71
                                String classDotFormat = javaFormat.replaceAll("\\$.*#", "#").replaceAll("/", ".");
                                // Get line range from mapping. At this stage inner class should not be removed.
                                ArrayList<Integer> range = MethodsHelper
                                        .getModifiedMethodsToLineNumbers()
                                        .get(javaFormat);
                                if (range != null) {
                                    int begin = range.get(0);
                                    int end = range.get(1);
                                    // First time
                                    classToImpactedLineNumbers.putIfAbsent(classDotFormat.split("#")[0],
                                            new HashSet<>());
                                    // Add all relevant lines
                                    for (int line = begin; line <= end; line++) {
                                        classToImpactedLineNumbers.get(classDotFormat.split("#")[0]).add(line);
                                    }
                                }
                            }
                            oos.writeObject(classToImpactedLineNumbers);
                        } else {
                            oos.writeObject(getImpactedMethods().stream()
                                    // Filter is needed to filter out variables.
                                    .filter(str -> str.matches(".*\\(.*\\)"))
                                    .map(str -> MethodsHelper.convertAsmToJava(str)
                                            .replace('/', '.')
                                            .split("\\(")[0]
                                    )
                                    .collect(Collectors.toSet())
                            );
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            // TODO: This debug segment looks really ugly, change it.
            if (debug) {
                try (PrintWriter writer = new PrintWriter(getArtifactsDir() + File.separator + "lineMapping.txt")) {
                    for (Map.Entry<String, ArrayList<Integer>> entry
                            : MethodsHelper.getModifiedMethodsToLineNumbers().entrySet()) {
                        ArrayList<Integer> range = entry.getValue();
                        writer.println(entry.getKey() + "," + (range != null ? range.get(0) + "," + range.get(1) : null));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                try (PrintWriter writer = new PrintWriter(getArtifactsDir() + File.separator + "impactedMethods.txt")) {
//                writer.println(MethodsHelper.getModifiedMethodsToLineNumbers());
                    for (String impactedMethod : getImpactedMethods()) {
                        if (!impactedMethod.matches(".*\\(.*\\)")) {
                            continue;
                        }
                        // Each entry in this metadata file contains a method signature to its line range in source file.
                        String javaFormat = MethodsHelper.convertAsmToJava(impactedMethod);
                        // Removes anonymous inner classes:
                        // Looks like: org/mitre/dsmiley/httpproxy/ProxyServletTest#tearDown(),67,71
                        String anonymousInnerClassesRemoved = javaFormat.replaceAll("\\$[0-9]*#", "#");
                        // Get line range from mapping.
                        ArrayList<Integer> range = MethodsHelper
                                .getModifiedMethodsToLineNumbers()
                                .get(anonymousInnerClassesRemoved);
                        writer.println(javaFormat + "," + (range != null ? range.get(0) + "," + range.get(1) : "0,0"));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            if (finerSpecMapping) {
                methodToSpecsUpdateMap
                        .forEach((key, value) -> methodsToSpecs.merge(key, value, (oldValue, newValue) -> newValue));
//                System.out.println(methodToSpecsUpdateMap);

                // Compute affected specs from changed methods or impacted methods
                // TODO: "impacted" and "affected" should mean the same thing.
                //  Rename this to something better to avoid confusion.
                // TODO: Currently this counts the number of impacted methods and variables,
                //  think about how to handle this detail.
                if (getComputeImpactedMethods()) {
                    computeAffectedSpecs(getImpactedMethods());
                    getLog().info("[eMOP] Number of Impacted methods: " + getImpactedMethods().size());
//                    System.out.println(getImpactedMethods());
                } else {
                    computeAffectedSpecs(getAffectedMethods());
                    getLog().info("[eMOP] Number of affected methods: " + getAffectedMethods().size());
                }
                long end = System.currentTimeMillis();
                getLog().info("[eMOP Timer] Compute affected specs takes " + (end - start) + " ms");

                start = System.currentTimeMillis();
                // TODO: Currently this is not taking effect, change related methods.
                writeMapToFile(methodsToSpecs, METHODS_TO_SPECS_DEBUG_FILE_NAME, OutputFormat.TXT);
                writeMapToFile(methodsToSpecs, METHODS_TO_SPECS_FILE_NAME, OutputFormat.BIN);
                end = System.currentTimeMillis();
                getLog().info("[eMOP Timer] Write affected specs to disk takes " + (end - start) + " ms");
            } else {
                classToSpecs = readMapFromFile("classToSpecs.bin");
                // Update map
                changedMap.forEach((key, value) -> classToSpecs.merge(key, value, (oldValue, newValue) -> newValue));
                computeAffectedSpecs(dependencyChanged);
                long end = System.currentTimeMillis();
                getLog().info("[eMOP Timer] Compute affected specs takes " + (end - start) + " ms");

                start = System.currentTimeMillis();
                // Write map
                writeMapToFile(OutputFormat.BIN);
                // Write affectedSpecs
                // TODO: This is not really a map anymore, make sure the implementation matches the name
                writeMapToFile(OutputFormat.TXT);
                end = System.currentTimeMillis();
                getLog().info("[eMOP Timer] Write affected specs to disk takes " + (end - start) + " ms");

//                getLog().info("[eMOP] Number of impacted classes: " + getImpacted().size());
                getLog().info("[eMOP] Number of messages to process: " + Arrays.asList(ms).size());
            }

            getLog().info("[eMOP] Number of changed classes: " + getChangedClasses().size());
            getLog().info("[eMOP] Number of new classes: " + getNewClasses().size());
            getLog().info("[eMOP] Number of messages to process: " + Arrays.asList(ms).size());
        } else if (getGranularity() == Granularity.HYBRID) {
            if (dependencyChanged) {
                // Revert to base RV, use all specs, include libraries and non-affected classes.
                affectedSpecs.addAll(Objects.requireNonNull(Util.getFullSpecSet(javamopAgent, "mop")));
                includeLibraries = true;
                includeNonAffected = true;
            }
            // This segment has to execute before return, otherwise it will pollute the next run
            recompileBaseAspect();
            // Why do it here? Because the program might exit early to revert to BaseRV
            // and use a modified version of BaseAspect instead, which we do not want.
            Util.replaceFileInJar(javamopAgent, "/mop/BaseAspect.class",
                    getArtifactsDir() + File.separator + "mop" + File.separator + "BaseAspect.class");
            if (!dependencyChanged && (
                    getComputeImpactedMethods() && getImpactedMethods().isEmpty() && getImpactedClasses().isEmpty()
                            // Affected classes are new classes, changed classes with changed headers only
                            || getAffectedMethods().isEmpty() && getAffectedClasses().isEmpty()
            )) {
                return;
            }
            if (debug) {
                getLog().info("Impacted Classes: " + getImpactedClasses());
            }

            getLog().info("[eMOP] Invoking the AffectedSpecsHybrid Mojo...");
            IMessage[] ms = doCompileTimeInstrumentation();

            long start = System.currentTimeMillis();
            classesToSpecs = readMapFromFile(CLASSES_TO_SPECS_FILE_NAME);
            methodsToSpecs = readMapFromFile(METHODS_TO_SPECS_FILE_NAME);

            if (finerInstrumentation) {
                if (!dependencyChanged) {
                    Util.setEnv("IMPACTED_METHODS_FILE", getArtifactsDir() + File.separator + "impactedMethods.bin");
                    getLog().info("IMPACTED_METHODS_FILE is set to " + System.getenv("IMPACTED_METHODS_FILE"));
                    try (FileOutputStream fos
                                 = new FileOutputStream(getArtifactsDir() + File.separator + "impactedMethods.bin");
                         ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                        if (finerInstrumentationAlt) {
                            for (String impactedClass : getImpactedClasses()) {
                                classToImpactedLineNumbers.putIfAbsent(impactedClass.replace("/", "."),
                                        new HashSet<>());
                            }
                            // TODO: Repeated code with the process of output to impactedMethods.txt.
                            for (String impactedMethod : getImpactedMethods()) {
                                if (!impactedMethod.matches(".*\\(.*\\)")) {
                                    continue;
                                }
                                String javaFormat = MethodsHelper.convertAsmToJava(impactedMethod);
                                // Looks like: org/mitre/dsmiley/httpproxy/ProxyServletTest#tearDown(),67,71
                                String classDotFormat = javaFormat.replaceAll("\\$.*#", "#").replaceAll("/", ".");
                                // Get line range from mapping. At this stage inner class should not be removed.
                                ArrayList<Integer> range = MethodsHelper
                                        .getModifiedMethodsToLineNumbers()
                                        .get(javaFormat);
                                if (range != null) {
                                    int begin = range.get(0);
                                    int end = range.get(1);
                                    // First time
                                    classToImpactedLineNumbers.putIfAbsent(classDotFormat.split("#")[0],
                                            new HashSet<>());
                                    // Add all relevant lines
                                    for (int line = begin; line <= end; line++) {
                                        classToImpactedLineNumbers.get(classDotFormat).add(line);
                                    }
                                }
                            }
                            oos.writeObject(classToImpactedLineNumbers);
                        } else {
                            Set<String> toWrite = getImpactedMethods().stream()
                                    // Filter is needed to filter out variables.
                                    .filter(str -> str.matches(".*\\(.*\\)"))
                                    .map(str -> MethodsHelper.convertAsmToJava(str)
                                            .replace('/', '.')
                                            .split("\\(")[0]
                                    )
                                    .collect(Collectors.toSet());
                            // Also add classes to this set.
                            toWrite.addAll(getImpactedClasses().stream()
                                    .map(str -> str.replace('/', '.')).
                                    collect(Collectors.toSet()));
                            oos.writeObject(toWrite);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            if (finerSpecMapping) {
                try {
                    computeMethodsToSpecsMapFromMessage(ms);
                    computeClassesToSpecsMapFromMessage(ms);

                    List<String> classesToInstrument = getNewlyUsedLibraries();
                    if (classesToInstrument != null) {
                        IMessage[] ms2 = doCompileTimeInstrumentation(classesToInstrument);

                        computeMethodsToSpecsMapFromMessage(ms2);
                        computeClassesToSpecsMapFromMessage(ms2);

                        Util.deleteRecursively(Paths.get(getArtifactsDir(), "lib-jars-tmp"));
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                methodToSpecsUpdateMap
                        .forEach((key, value) -> methodsToSpecs.merge(key, value, (oldValue, newValue) -> newValue));
                classToSpecsUpdateMap
                        .forEach((key, value) -> classesToSpecs.merge(key, value, (oldValue, newValue) -> newValue));

                // TODO: "impacted" and "affected" should mean the same thing.
                //  Rename this to something better to avoid confusion.
                // Compute affected specs from changed methods or impacted methods
                if (getComputeImpactedMethods()) {
                    computeMethodsAffectedSpecs(getImpactedMethods());
                    computeClassesAffectedSpecs(getImpactedClasses());
                    getLog().info("[eMOP] Number of Impacted methods: " + getImpactedMethods().size());
                    getLog().info("[eMOP] Number of Impacted classes: " + getImpactedClasses().size());
                } else {
                    computeMethodsAffectedSpecs(getAffectedMethods());
                    computeClassesAffectedSpecs(getAffectedClasses());
                    getLog().info("[eMOP] Number of affected methods: " + getAffectedMethods().size());
                    getLog().info("[eMOP] Number of affected classes: " + getAffectedClasses().size());
                }
                if (dependencyChanged) {
                    // Revert to base RV, use all specs, include libraries and non-affected classes.
                    affectedSpecs.addAll(Objects.requireNonNull(Util.getFullSpecSet(javamopAgent, "mop")));
                    includeLibraries = true;
                    includeNonAffected = true;
                }
                long end = System.currentTimeMillis();
                getLog().info("[eMOP Timer] Compute affected specs takes " + (end - start) + " ms");

                start = System.currentTimeMillis();
                writeMapToFile(classesToSpecs, CLASSES_TO_SPECS_DEBUG_FILE_NAME, OutputFormat.TXT);
                writeMapToFile(methodsToSpecs, METHODS_TO_SPECS_DEBUG_FILE_NAME, OutputFormat.TXT);
                writeMapToFile(classesToSpecs, CLASSES_TO_SPECS_FILE_NAME, OutputFormat.BIN);
                writeMapToFile(methodsToSpecs, METHODS_TO_SPECS_FILE_NAME, OutputFormat.BIN);
                end = System.currentTimeMillis();
                getLog().info("[eMOP Timer] Write affected specs to disk takes " + (end - start) + " ms");
                getLog().info("[eMOP] Number of messages to process: " + Arrays.asList(ms).size());
            } else {
                computeMapFromMessage(ms);
                List<String> classesToInstrument = getNewlyUsedLibraries();
                if (classesToInstrument != null) {
                    IMessage[] ms2 = doCompileTimeInstrumentation(classesToInstrument);
                    computeMapFromMessage(ms2);
                    Util.deleteRecursively(Paths.get(getArtifactsDir(), "lib-jars-tmp"));
                }

                classToSpecs = readMapFromFile("classToSpecs.bin");
                // Update map
                changedMap.forEach((key, value) -> classToSpecs.merge(key, value, (oldValue, newValue) -> newValue));
                computeAffectedSpecs(dependencyChanged);
                long end = System.currentTimeMillis();
                getLog().info("[eMOP Timer] Compute affected specs takes " + (end - start) + " ms");

                start = System.currentTimeMillis();
                // Write map
                writeMapToFile(OutputFormat.BIN);
                // Write affectedSpecs
                // TODO: This is not really a map anymore, make sure the implementation matches the name
                writeMapToFile(OutputFormat.TXT);
                end = System.currentTimeMillis();
                getLog().info("[eMOP Timer] Write affected specs to disk takes " + (end - start) + " ms");

//                getLog().info("[eMOP] Number of impacted classes: " + getImpacted().size());
                getLog().info("[eMOP] Number of messages to process: " + Arrays.asList(ms).size());
            }
        }
    }

    private IMessage[] doCompileTimeInstrumentation() throws MojoExecutionException {
        return doCompileTimeInstrumentation(null);
    }

    private IMessage[] doCompileTimeInstrumentation(List<String> classesToInstrument) throws MojoExecutionException {
        long start = System.currentTimeMillis();
        String[] arguments = createAJCArguments(classesToInstrument);
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
        getLog().info("[eMOP Timer] Compile-time weaving takes " + (end - start) + " ms.");

        if (debug) {
            StringBuilder ajcCommand = new StringBuilder();
            for (String arg : arguments) {
                ajcCommand.append(arg).append(" ");
            }
            getLog().info("AJC command: ajc " + ajcCommand);
            for (IMessage errMsg : mh.getErrors()) {
                getLog().error(errMsg.toString());
            }
            try (PrintWriter writer
                         = new PrintWriter(getArtifactsDir() + File.separator + "compileWeaveMessage.txt")) {
                for (IMessage message : ms) {
                    writer.println(message.getMessage());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return ms;
    }

    private void recompileBaseAspect() throws MojoExecutionException {
        Util.generateNewBaseAspect(getArtifactsDir() + File.separator + "BaseAspect.aj",
                dependencyChanged || !finerInstrumentation || !finerSpecMapping,
                includeLibraries,
                includeNonAffected,
                finerInstrumentationAlt,
                Util.retrieveProjectPackageNames(getClassesDirectory()));
        String[] arguments = new String[] {getArtifactsDir() + File.separator + "BaseAspect.aj",
                "-source", "1.8", "-target", "1.8", "-d", getArtifactsDir(),
                "-classpath", getClassPath() + File.pathSeparator + getRuntimeJars()};
        Main compiler = new Main();
        MessageHandler mh = new MessageHandler();
        try {
            compiler.run(arguments, mh);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (debug) {
            StringBuilder ajcCommand = new StringBuilder();
            for (String arg : arguments) {
                ajcCommand.append(" ").append(arg);
            }
            getLog().info("AJC command: ajc" + ajcCommand);
            getLog().info("AJC error messages:");
            for (IMessage errMsg : mh.getErrors()) {
                getLog().error(errMsg.toString());
            }
            getLog().info("AJC error messages end.");
        }
    }

    // Hybrid-only:
    private void computeClassesAffectedSpecs(Set<String> classes) {
        for (String affectedClass : classes) {
            Set<String> specs = classesToSpecs.getOrDefault(affectedClass.replace("/", "."), new HashSet<>());
            affectedSpecs.addAll(specs);
        }
    }

    private void computeMethodsAffectedSpecs(Set<String> methods) {
        for (String affectedMethod : methods) {
            if (!affectedMethod.matches(".*\\(.*\\)")) {
                continue;
            }
            // Convert method name from asm to java
            String javaMethodName = MethodsHelper.convertAsmToJava(affectedMethod);
            Set<String> specs = methodsToSpecs.getOrDefault(javaMethodName, new HashSet<>());
            affectedSpecs.addAll(specs);
        }
    }

    private void computeClassesToSpecsMapFromMessage(IMessage[] ms) throws MojoExecutionException {
        MethodsHelper.loadMethodsToLineNumbers(getArtifactsDir());
        for (IMessage message : ms) {
            String[] lexedMessage = message.getMessage().split("'");
            String key = lexedMessage[CLASS_INDEX_IN_MSG];
            String value = lexedMessage[SPEC_INDEX_IN_MSG].substring(TRIMMED_SPEC_NAME_INDEX);
            if (!classToSpecsUpdateMap.containsKey(key)) {
                classToSpecsUpdateMap.put(key, new HashSet<>());
            }
            classToSpecsUpdateMap.get(key).add(value);
        }
        for (String clazz : getImpactedClasses()) {
            classToSpecsUpdateMap.putIfAbsent(clazz.replace('/', '.'), new HashSet<>());
        }
        MethodsHelper.saveMethodsToLineNumbers(getArtifactsDir());
    }

    /**
     * Compute a mapping from affected classes to specifications based on the
     * messages from AJC.
     *
     * @param ms An array of IMessage objects
     */
    private void computeMethodsToSpecsMapFromMessage(IMessage[] ms) throws Exception {
        Classpath sfClassPath = getSureFireClassPath();
        ClassLoader loader = createClassLoader(sfClassPath);
        MethodsHelper.loadMethodsToLineNumbers(getArtifactsDir());
        for (IMessage message : ms) {
            String[] lexedMessage = message.getMessage().split("'");
            String klasName = lexedMessage[CLASS_INDEX_IN_MSG];
            String spec = lexedMessage[SPEC_INDEX_IN_MSG].substring(TRIMMED_SPEC_NAME_INDEX);
            int specLineNumber = Integer
                    .parseInt(lexedMessage[SPEC_LINE_NUMBER].split(" ")[1].split(":")[1].replace(")", ""));

            String klas = ChecksumUtil.toClassOrJavaName(klasName, false);
            URL url = loader.getResource(klas);
            String filePath = url.getPath();

            if (filePath.contains("jar!")) {
                filePath = getArtifactsDir() + "lib-jars" + filePath.split("!")[1];
            } else {
                filePath = filePath.replace(".class", ".java")
                        .replace("target", "src")
                        .replace("test-classes", "test/java")
                        .replace("classes", "main/java");
            }

            try {
                MethodsHelper.computeMethodToLineNumbers(filePath);
            } catch (ParserException exception) {
                getLog().warn("File contains interface only, no methods found in " + filePath);
            }
            String method = MethodsHelper.getWrapMethod(filePath, specLineNumber);
            if (method == null) {
                getLog().warn("Spec at line " + specLineNumber + " in " + filePath + " is not within a method");
                continue;
            }
            String key = klas.replace(".class", "") + "#" + method;
            Set<String> methodSpecs = methodToSpecsUpdateMap.getOrDefault(key, new HashSet<>());
            methodToSpecsUpdateMap.put(key, methodSpecs);
            methodSpecs.add(spec);
        }
        for (String method : getImpactedMethods()) {
            methodToSpecsUpdateMap.putIfAbsent(MethodsHelper.convertAsmToJava(method), new HashSet<>());
        }
        MethodsHelper.saveMethodsToLineNumbers(getArtifactsDir());
    }

    // TODO: Currently implemented as an overload, need to merge together eventually, and add documentation
    private void computeAffectedSpecs(boolean dependencyChangeDetected) {
        Set<String> impactedClasses = null;
        if (getGranularity() == Granularity.CLASS || getGranularity() == Granularity.FINE) {
            impactedClasses = new HashSet<>(getImpacted());
        } else if (getGranularity() == Granularity.METHOD) {
            impactedClasses = getImpactedMethods().stream()
                    .map(str -> str.split("#")[0].replace('/', '.'))
                    .collect(Collectors.toSet());
        } else if (getGranularity() == Granularity.HYBRID) {
            Set<String> combined = new HashSet<>(getImpactedMethods());
            combined.addAll(getImpactedClasses());
            impactedClasses = combined.stream()
                    .map(s -> s.split("#")[0].replace('/', '.'))
                    .collect(Collectors.toSet());
        }
        if (dependencyChangeDetected) {
            // Revert to base RV, use all specs, include libraries and non-affected classes.
            affectedSpecs.addAll(Objects.requireNonNull(Util.getFullSpecSet(javamopAgent, "mop")));
            includeLibraries = true;
            includeNonAffected = true;
        } else {
            for (String impactedClass : impactedClasses) {
                Set<String> associatedSpecs = classToSpecs.get(impactedClass);
                if (associatedSpecs != null) {
                    affectedSpecs.addAll(associatedSpecs);
                }
            }
        }
    }

    private void computeAffectedSpecs(Set<String> methods) {
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
     * specs inside that method. This is because the messages from AJC contain only
     * the class and line number of the spec not the method.
     * Example message entry lexed with "'":
     *   0: [AppClassLoader@18b4aac2] weaveinfo Join point
     *   1: method-call(java.lang.StringBuilder java.lang.StringBuilder.append(java.lang.String))
     *   2: in Type
     *   3: org.mitre.dsmiley.httpproxy.ProxyServletTest
     *   4: (ProxyServletTest.java:53) advised by before advice from
     *   5: mop.Appendable_ThreadSafeMonitorAspect
     *   6: (Appendable_ThreadSafeMonitorAspect.aj:34)
     *
     * @param ms An array of IMessage objects
     */
    private void computeMapFromMessage(IMessage[] ms) throws MojoExecutionException {
        if (getGranularity() == Granularity.CLASS || getGranularity() == Granularity.FINE || !finerSpecMapping) {
            for (IMessage message : ms) {
                String[] lexedMessage = message.getMessage().split("'");
                String key = lexedMessage[CLASS_INDEX_IN_MSG];
                String value = lexedMessage[SPEC_INDEX_IN_MSG].substring(TRIMMED_SPEC_NAME_INDEX);
                if (!changedMap.containsKey(key)) {
                    changedMap.put(key, new HashSet<>());
                }
                changedMap.get(key).add(value);
            }
        } else if (getGranularity() == Granularity.METHOD) {
            Classpath sfClassPath = getSureFireClassPath();
            ClassLoader loader = createClassLoader(sfClassPath);

            for (IMessage message : ms) {
                String[] lexedMessage = message.getMessage().split("'");
                String klasName = lexedMessage[CLASS_INDEX_IN_MSG];
                String spec = lexedMessage[SPEC_INDEX_IN_MSG].substring(TRIMMED_SPEC_NAME_INDEX);

                // It is possible that we don't have line number, so we need this tmp thing and set default to 0
                String[] tmp = lexedMessage[SPEC_LINE_NUMBER].split(" ")[1].split(":");
                int specLineNumber = 0;
                if (tmp.length > 1) {
                    specLineNumber = Integer.parseInt(tmp[1].replace(")", ""));
                }

                String klas = ChecksumUtil.toClassOrJavaName(klasName, false);
                URL url = loader.getResource(klas);
                String filePath = url.getPath();
//                System.out.println("Granularity.METHOD " + filePath + " and klas is " + klas);

                if (filePath.contains("jar!")) {
                    filePath = getArtifactsDir() + "lib-jars" + filePath.split("!")[1];
                } else {
                    filePath = filePath.replace(".class", ".java")
                            .replace("target", "src")
                            .replace("test-classes", "test/java")
                            .replace("classes", "main/java");
                }

                try {
                    // This method has a return value, but it also updated a global variable inside its class.
                    MethodsHelper.computeMethodToLineNumbers(filePath);
                } catch (ParserException | IOException exception) {
                    getLog().warn("File contains interface only, no methods found in " + filePath);
                }

                String method = MethodsHelper.getWrapMethod(filePath, specLineNumber);
                if (method == null) {
                    getLog().warn("Cannot find method for " + filePath + " at line " + specLineNumber);
                    continue;
                }
                String key = klas.replace(".class", "") + "#" + method;
                Set<String> methodSpecs = methodToSpecsUpdateMap.getOrDefault(key, new HashSet<>());
                methodToSpecsUpdateMap.put(key, methodSpecs);
                methodSpecs.add(spec);
//                System.out.println(">>> KEY IS " + key + ", methodSpecs is " + methodSpecs);
            }
            for (String method : getImpactedMethods()) {
//                System.out.println(">>> METHOD IS " + method);
                methodToSpecsUpdateMap.putIfAbsent(MethodsHelper.convertAsmToJava(method), new HashSet<>());
            }
        }
    }

    // TODO: Currently implemented as method overloading, eventually the two methods need to be merged.
    /**
     * Write map from class to specs in either text or binary format.
     * @param format Output format of the map, text or binary
     */
    public void writeMapToFile(Map<String, Set<String>> map, String fileName, OutputFormat format)
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
     * Write map from class to specs in either text or binary format.
     * @param format Output format of the map, text or binary
     */
    private void writeMapToFile(OutputFormat format) throws MojoExecutionException {
        switch (format) {
            case BIN:
                // Referenced from https://www.geeksforgeeks.org/how-to-serialize-hashmap-in-java/
                try (FileOutputStream fos
                             = new FileOutputStream(getArtifactsDir() + File.separator + "classToSpecs.bin");
                    ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(classToSpecs);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                break;
            case TXT:
            default:
                writeToText(classToSpecsContent, "classToSpecs.txt");
        }
    }

    /**
     * Write class and specification information to text file.
     * @param content What to output
     */
    private void writeToText(OutputContent content, String fileName) throws MojoExecutionException {
        // TODO: Change this fileName to not be hard-coded
        try (PrintWriter writer
                     = new PrintWriter(getArtifactsDir() + File.separator + fileName)) {
            if (getGranularity() == Granularity.METHOD) {
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
            } else if (getGranularity() == Granularity.CLASS || getGranularity() == Granularity.FINE) {
                switch (classToSpecsContent) {
                    case MAP:
                        for (Map.Entry<String, Set<String>> entry : classToSpecs.entrySet()) {
                            writer.println(entry.getKey() + ":" + String.join(",", entry.getValue()));
                        }
                        break;
                    case SET:
                    default:
                        for (String affectedSpec : affectedSpecs) {
                            writer.println(affectedSpec);
                        }
                }
            } else if (getGranularity() == Granularity.HYBRID) {
                // TODO: Defaulted to MAP
                writer.println("=====CLASS=====");
                // TODO: Address this inconsistent naming between classesToSpecs and classToSpecs
                for (Map.Entry<String, Set<String>> entry: classesToSpecs.entrySet()) {
                    writer.println(entry.getKey() + ":" + String.join(",", entry.getValue()));
                }
                writer.println("=====METHOD=====");
                for (Map.Entry<String, Set<String>> entry: methodsToSpecs.entrySet()) {
                    writer.println(entry.getKey() + ":" + String.join(",", entry.getValue()));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * TODO: use the one from Util.java instead
     * Reads a binary file that stores a map.
     * @param fileName Name of the file to read
     * @return The map read from file
     */
    public Map<String, Set<String>> readMapFromFile(String fileName) throws MojoExecutionException {
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
        return createAJCArguments(null);
    }
    private String[] createAJCArguments(List<String> classesToInstrument) throws MojoExecutionException {
        // extract the aspects for all available specs from the jar and make a list of them in a file
        String destinationDir = getArtifactsDir() + File.separator + "weaved-specs";
        String aspectList = getArtifactsDir() + File.separator + "aspects.lst";
        List<String> aspects = extractOrFind(destinationDir, ".aj", "weaved-specs");
        // Users have the freedom to delete specs. Simply using this list may lead to errors.
        Set<String> existingSpecs = Util.getFullSpecSet(javamopAgent, "mop");
        aspects = aspects.stream()
                        .filter(spec -> existingSpecs.contains(
                                spec.substring(spec.lastIndexOf(File.separator) + 1).split("\\.")[0]
                        ))
                        .collect(Collectors.toList());
        Writer.writeToFile(aspects, aspectList);
        // the source files that we want to weave are the impacted classes, write them to a file
        String sourceList = getArtifactsDir() + File.separator + "sources.lst";
        // Get changed instead of impacted to reduce compile time
        // get both changed (existing) and new classes

        Path inputRoot = Paths.get(getArtifactsDir(), "lib-jars");
        Path outputRoot = Paths.get(getArtifactsDir(), "lib-jars-tmp");
        if (classesToInstrument == null) {
            makeSourcesFile(sourceList, getNewClasses());
        } else {
            try {
                for (String fileToInstrument : classesToInstrument) {
                    String fileName = fileToInstrument.replace(".", File.separator) + ".class";
                    Path source = inputRoot.resolve(fileName);
                    Path destination = outputRoot.resolve(fileName);

                    Files.createDirectories(destination.getParent());
                    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // extract the argument file that we want to use from the jar to the .starts directory
        String argsList = getArtifactsDir() + File.separator + "argz";
        List<String> args = extractOrFind(argsList, ".lst", "argz");
        // prepare the classpath that we want to call AJC with
        String classpath = getClassPath() + File.pathSeparator + getRuntimeJars();
        // prepare an array of arguments that the aspectj compiler will be called with
        if (classesToInstrument == null) {
            return new String[]{"-classpath", classpath, "-argfile", aspectList, "-argfile", sourceList, "-argfile",
                    args.get(0), "-d", "weaved-bytecode"};
        } else {
            return new String[]{"-classpath", classpath, "-argfile", aspectList, "-inpath", outputRoot.toString(), "-argfile",
                    args.get(0), "-d", "weaved-bytecode"};
        }
    }

    private List<String> getNewlyUsedLibraries() throws MojoExecutionException {
        if (dependencyChanged) {
            // We are going to use LTW to get classes to specs, so we do not need to instrument code to find specs.
            return null;
        }

        List<String> libraries = new ArrayList<>();
        getLog().info("[eMOP] Checking for newly used libraries...");
//       Set<String> impactedClasses = getImpactedMethods().stream()
//                .map(str -> str.split("#")[0].replace('/', '.'))
//                .collect(Collectors.toSet());

        if (getGranularity() == Granularity.CLASS || getGranularity() == Granularity.FINE) {
            for (String klass : getImpacted()) {
                // Search if classes is a library class
                if (!classToSpecs.containsKey(klass)) {
                    // First time see, we need to get its specs
                    if (Files.exists(Paths.get(getArtifactsDir(), "lib-jars", klass.replace(".", File.separator) + ".class"))) {
                        libraries.add(klass);
                    }
                }
            }
        } else if (getGranularity() == Granularity.METHOD) {
            for (String method : getImpactedMethods()) {
                // Search if classes is a library class
                if (!methodsToSpecs.containsKey(method)) {
                    String klass = method.split("#")[0].replace('/', '.');
                    if (Files.exists(Paths.get(getArtifactsDir(), "lib-jars", klass.replace(".", File.separator) + ".class"))) {
                        libraries.add(klass);
                    }
                }
            }
        }

        getLog().info("[eMOP] Found " + libraries.size() + " newly used libraries.");
        return libraries;
    }

    /**
     * We need to put aspectjrt and rv-monitor-rt on the classpath for AJC.
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
     * Given a path to a class file, returns a path to its corresponding source file. Assumes a standard directory
     * layout, i.e., one where the source for {@code com.abc.A} resides in {@code sourceDir/com/abc/A.java}.
     * @param classFile the path to the class file
     * @param classesDir the base class file directory
     * @param sourceDir the base sources directory
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

        classes:
        for (String newClass : newClasses) {
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
//            getLog().error("No source file found for class " + newClass);
        }

        Path mainClassesDir = getClassesDirectory().toPath().toAbsolutePath();
        Path testClassesDir = getTestClassesDirectory().toPath().toAbsolutePath();
        // TODO: Think about combining them:
        Set<String> changedClasses = null;
        if (getGranularity() == Granularity.CLASS || getGranularity() == Granularity.FINE) {
            changedClasses = getChanged();
        } else if (getGranularity() == Granularity.METHOD || getGranularity() == Granularity.HYBRID) {
            changedClasses = getChangedClasses();
        }
        classes:
        for (String changedClass : changedClasses) {
            if (changedClass.contains("$")) {
                changedClass = changedClass.substring(0, changedClass.indexOf('$')) + ".class";
            }

            try {
                Path classFile = Paths.get(new URI(changedClass)).toAbsolutePath();
                Path classesDir;

                if (!classFile.toFile().exists()) {
                    getLog().warn("Class file does not exist: " + classFile);
                    continue;
                }

                if (classFile.startsWith(mainClassesDir)) {
                    classesDir = mainClassesDir;
                } else if (classFile.startsWith(testClassesDir)) {
                    classesDir = testClassesDir;
                } else {
                    getLog().error("Class file not found in standard directories: " + classFile);
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
                getLog().error("No source file found for class file " + classFile);
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
