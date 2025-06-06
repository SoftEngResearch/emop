package edu.cornell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cornell.emop.util.MethodsHelper;
import edu.cornell.emop.util.Util;
import edu.illinois.starts.enums.Granularity;
import edu.illinois.starts.util.ChecksumUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.surefire.booter.Classpath;
import org.aspectj.bridge.IMessage;
import org.jboss.forge.roaster.ParserException;

@Mojo(name = "rps", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST, lifecycle = "rps")
public class RpsMojo extends MonitorMojo {

    private static final int CLASS_INDEX_IN_MSG = 3;
    private static final int SPEC_LINE_NUMBER = 4;
    private static final int TRIMMED_SPEC_NAME_INDEX = 4;
    private static final int SPEC_INDEX_IN_MSG = 5;
    private static final String CLASSES_TO_SPECS_FILE_NAME = "classesToSpecs.bin";
    private static final String ASPECTJ_WEAVING_FILE = "aspectj-weaving-message.log";
    private static final String METHODS_TO_SPECS_FILE_NAME = "methodsToSpecs.bin";

    protected Map<String, Set<String>> classToSpecs = new HashMap<>();
    private Map<String, Set<String>> changedMap = new HashMap<>();

    public void execute() throws MojoExecutionException {
        long start = System.currentTimeMillis();
        getLog().info("[eMOP] Invoking the RPS Mojo...");
        System.setProperty("exiting-rps", "false");

        Path ajcLog = Paths.get(getArtifactsDir() + File.separator + ASPECTJ_WEAVING_FILE);
        if (Files.exists(ajcLog)) {
            getLog().info("AspectJ weaving log found: " + ajcLog.toString());

            if (getGranularity() == Granularity.CLASS || getGranularity() == Granularity.FINE) {
                classToSpecs = Util.readMapFromFile(getArtifactsDir(), "classToSpecs.bin");
                computeMapFromMessage(ajcLog);
                changedMap.forEach((key, value) -> classToSpecs.merge(key, value, (oldValue, newValue) -> newValue));
                writeMapToFile();
            } else if (getGranularity() == Granularity.METHOD) {
                methodsToSpecs = readMapFromFile(METHODS_TO_SPECS_FILE_NAME);

                try {
                    MethodsHelper.loadMethodsToLineNumbers(getArtifactsDir());
                    computeMapFromMessage(ajcLog);
                    MethodsHelper.saveMethodsToLineNumbers(getArtifactsDir());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (finerSpecMapping) {
                    methodToSpecsUpdateMap
                            .forEach((key, value) -> methodsToSpecs.merge(key, value, (oldValue, newValue) -> newValue));
//                    System.out.println(methodToSpecsUpdateMap);
                    writeMapToFile(methodsToSpecs, METHODS_TO_SPECS_FILE_NAME, OutputFormat.BIN);
                } else {
                    classToSpecs = readMapFromFile("classToSpecs.bin");
                    changedMap.forEach((key, value) -> classToSpecs.merge(key, value, (oldValue, newValue) -> newValue));
                    writeMapToFile();
                }
            } else if (getGranularity() == Granularity.HYBRID) {
                classesToSpecs = readMapFromFile(CLASSES_TO_SPECS_FILE_NAME);
                methodsToSpecs = readMapFromFile(METHODS_TO_SPECS_FILE_NAME);

                if (finerSpecMapping) {
                    try {
                        computeMethodsToSpecsMapFromMessage(ajcLog);
                        computeClassesToSpecsMapFromMessage(ajcLog);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    methodToSpecsUpdateMap
                            .forEach((key, value) -> methodsToSpecs.merge(key, value, (oldValue, newValue) -> newValue));
                    classToSpecsUpdateMap
                            .forEach((key, value) -> classesToSpecs.merge(key, value, (oldValue, newValue) -> newValue));

                    writeMapToFile(classesToSpecs, CLASSES_TO_SPECS_FILE_NAME, OutputFormat.BIN);
                    writeMapToFile(methodsToSpecs, METHODS_TO_SPECS_FILE_NAME, OutputFormat.BIN);
                } else {
                    classToSpecs = readMapFromFile("classToSpecs.bin");
                    computeMapFromMessage(ajcLog);
                    changedMap.forEach((key, value) -> classToSpecs.merge(key, value, (oldValue, newValue) -> newValue));
                    writeMapToFile();
                }
            }
            try {
                Files.delete(ajcLog);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        long end = System.currentTimeMillis();
        getLog().info("[eMOP Timer] Compute affected specs from log takes " + (end - start) + " ms");
    }

    private void computeMapFromMessage(Path ajcLog) throws MojoExecutionException {
        String[] ms;
        try {
            ms = Files.lines(ajcLog).toArray(String[]::new);
        } catch (IOException e) {
            throw new MojoExecutionException("Error reading ajcLog file", e);
        }

        int i = 0;
        if (getGranularity() == Granularity.CLASS || getGranularity() == Granularity.FINE || !finerSpecMapping) {
            for (String message : ms) {
                if (!message.contains("weaveinfo Join point")) {
                    continue;
                }

                String[] lexedMessage = message.split("'");
                String key = lexedMessage[CLASS_INDEX_IN_MSG];
                String value = lexedMessage[SPEC_INDEX_IN_MSG].substring(TRIMMED_SPEC_NAME_INDEX);

                if (!changedMap.containsKey(key)) {
                    changedMap.put(key, new HashSet<>());
                }
                changedMap.get(key).add(value);
                i += 1;
            }

            getLog().info("Added " + i + " class/spec to the changedMap from AspectJ's log.");
        } if (getGranularity() == Granularity.METHOD) {
            Classpath sfClassPath = getSureFireClassPath();
            ClassLoader loader = createClassLoader(sfClassPath);

            for (String message : ms) {
                if (!message.contains("weaveinfo Join point")) {
                    continue;
                }

//                System.out.println(message);
                String[] lexedMessage = message.split("'");
                String klasName = lexedMessage[CLASS_INDEX_IN_MSG];
                String spec = lexedMessage[SPEC_INDEX_IN_MSG].substring(TRIMMED_SPEC_NAME_INDEX);

                // It is possible that we don't have line number, so we need this tmp thing and set default to 0
                String[] tmp = lexedMessage[SPEC_LINE_NUMBER].split(" ")[1].split(":");
                int specLineNumber = 0;
                if (tmp.length > 1) {
                    specLineNumber = Integer.parseInt(tmp[1].replace(")", ""));
                }

                String klas = ChecksumUtil.toClassOrJavaName(klasName, false);
//                System.out.println("CLASS: " + klas + " and spec is " + spec + " at line " + specLineNumber);
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

//                System.out.println("Granularity.METHOD " + filePath + " and klas is " + klas);


                try {
                    // This method has a return value, but it also updated a global variable inside its class.
                    MethodsHelper.computeMethodToLineNumbers(filePath);
                } catch (ParserException | IOException exception) {
                    getLog().warn("File contains interface only, no methods found in " + filePath);
                }
//
                String method = MethodsHelper.getWrapMethod(filePath, specLineNumber);
                if (method == null) {
                    getLog().warn("Cannot find method for " + filePath + " at line " + specLineNumber);
                    continue;
                }

//                key should be com/conveyal/osmlib/RoundTripTest#compareMap(Map,Map)
                String key = klas.replace(".class", "") + "#" + method;
                Set<String> methodSpecs = methodToSpecsUpdateMap.getOrDefault(key, new HashSet<>());
                methodToSpecsUpdateMap.put(key, methodSpecs);
                methodSpecs.add(spec);
                i += 1;
            }

            getLog().info("Added " + i + " method/spec to the methodToSpecsUpdateMap from AspectJ's log.");
        }
    }

    private void computeClassesToSpecsMapFromMessage(Path ajcLog) throws MojoExecutionException {
        String[] ms;
        try {
            ms = Files.lines(ajcLog).toArray(String[]::new);
        } catch (IOException e) {
            throw new MojoExecutionException("Error reading ajcLog file", e);
        }

        MethodsHelper.loadMethodsToLineNumbers(getArtifactsDir());
        for (String message : ms) {
            if (!message.contains("weaveinfo Join point")) {
                continue;
            }

            String[] lexedMessage = message.split("'");
            String key = lexedMessage[CLASS_INDEX_IN_MSG];
            String value = lexedMessage[SPEC_INDEX_IN_MSG].substring(TRIMMED_SPEC_NAME_INDEX);
            if (!classToSpecsUpdateMap.containsKey(key)) {
                classToSpecsUpdateMap.put(key, new HashSet<>());
            }
            classToSpecsUpdateMap.get(key).add(value);
        }
        MethodsHelper.saveMethodsToLineNumbers(getArtifactsDir());
    }

    /**
     * Compute a mapping from affected classes to specifications based on the
     * messages from AJC log.
     */
    private void computeMethodsToSpecsMapFromMessage(Path ajcLog) throws Exception {
        String[] ms;
        try {
            ms = Files.lines(ajcLog).toArray(String[]::new);
        } catch (IOException e) {
            throw new MojoExecutionException("Error reading ajcLog file", e);
        }

        Classpath sfClassPath = getSureFireClassPath();
        ClassLoader loader = createClassLoader(sfClassPath);
        MethodsHelper.loadMethodsToLineNumbers(getArtifactsDir());
        for (String message : ms) {
            if (!message.contains("weaveinfo Join point")) {
                continue;
            }

            String[] lexedMessage = message.split("'");
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

        MethodsHelper.saveMethodsToLineNumbers(getArtifactsDir());
    }

    private void writeMapToFile() throws MojoExecutionException {
        try (FileOutputStream fos
                     = new FileOutputStream(getArtifactsDir() + File.separator + "classToSpecs.bin");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(classToSpecs);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
