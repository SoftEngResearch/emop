package edu.cornell.emop.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.illinois.starts.constants.StartsConstants;
import edu.illinois.starts.helpers.Writer;
import edu.illinois.starts.util.Pair;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

public class Util {

    /** Defines a SimpleDateFormat. */
    public static SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    /**
     * Find files under a path that matches the extension.
     *
     * @param path The path in which to search for files
     * @param extension The file extension being searched for
     * @return A list of file names under the specified path that matches the extension
     */
    public static List<String> findFilesOfType(File path, String extension) {
        List<String> returnedFileNames = new ArrayList<>();
        String[] files = path.list();
        if (files != null) {
            for (String currFile : files) {
                File file = new File(currFile);
                if (file.isDirectory()) {
                    List<String> internal = findFilesOfType(file, extension);
                    returnedFileNames.addAll(internal);
                } else {
                    String fileName = file.getName();
                    if (fileName.endsWith(extension)) {
                        returnedFileNames.add(path.getAbsolutePath() + File.separator + fileName);
                    }
                }
            }
        }
        return returnedFileNames;
    }

    /**
     * Replace a file inside a jar.
     *
     * @param jarPath Path to the jar file
     * @param oldPath Path to the file being replaced in the jar
     * @param newPath Path to the new file on the user's filesystem
     */
    public static void replaceFileInJar(String jarPath, String oldPath, String newPath) {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        URI jarFile = URI.create("jar:file:" + jarPath);
        try (FileSystem jarfs = FileSystems.newFileSystem(jarFile, env)) {
            Path newFile = Paths.get(newPath);
            Path pathInJarFile = jarfs.getPath(oldPath);
            Files.copy(newFile, pathInJarFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Obtains a set of all the specifications in a JavaMOP agent jar.
     *
     * @param jarPath Path to the .jar file
     * @param pathInJar Path in the .jar file that contains all the specifications
     * @return A set that contains all specs used in the JavaMOP agent.
     */
    public static Set<String> getFullSpecSet(String jarPath, String pathInJar) {
        URI jarFile = URI.create("jar:file:" + jarPath);
        try (FileSystem jarfs = FileSystems.newFileSystem(jarFile, new HashMap<String, String>())) {
            Path pathInJarFile = jarfs.getPath(pathInJar);
            try (Stream<Path> stream = Files.list(pathInJarFile)) {
                Set<String> specs = stream
                        .filter(file -> !Files.isDirectory(file))
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .map(spec -> spec.split("[.]")[0])
                        .filter(spec -> !spec.contains("$"))
                        .filter(spec -> spec.contains("Aspect"))
                        .collect(Collectors.toSet());
                specs.remove("BaseAspect");
                return specs;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new HashSet<>();
    }

    /**
     * Retrieve a set of all specifications that are built into the specified jar.
     *
     * @param jarPath Path to the jar file.
     * @param log The log object.
     * @return a set of all specifications that are built into the specified jar.
     */
    public static Set<String> retrieveSpecListFromJar(String jarPath, Log log) {
        // Assume that the jar contains a specs.txt.
        Set<String> specs = new HashSet<>();
        URL specsFileInJar = null;
        try {
            specsFileInJar = new URL("jar:file:" + jarPath + "!/specs.txt");
        } catch (MalformedURLException ex) {
            log.error("JavaMOP agent used does not contain specs.txt, a list of all specs created.");
            log.error("Please rebuild the JavaMOP agent using the provided script.");
            System.exit(1);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(specsFileInJar.openStream()))) {
            while (reader.ready()) {
                specs.add(reader.readLine());
            }
        } catch (IOException ex) {
            log.error("An I/O error occurred while reading the JavaMOP agent's specs.txt.");
            System.exit(1);
        }
        return specs;
    }

    /**
     * Recursive routine accumulating the set of package names within the project.
     *
     * @param currRoot the current directory location.
     * @param classesDirName the absolute path of the classes directory.
     * @return set of all package names within the project.
     */
    private static Set<String> classFilesWalk(File currRoot, String classesDirName) {
        Set<String> packageNameSet = new HashSet<>();
        File[] files = currRoot.listFiles();
        if (files == null) {
            return packageNameSet;
        }
        // we want to list all the potential *.class files in this directory
        File[] classFiles = currRoot.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.isDirectory() && file.getName().endsWith(".class");
            }
        });
        if (classFiles.length > 0) {
            // we found a class file, which means we are in a directory with class files (package)
            // No need to traverse further because AspectJ within(${PACKAGE_NAME}..*) syntax instruments subpackages.
            // The [1] here points to the part of the path after the location of the classes directory.
            // ex) for commons-fileupload, classesDirName would be /home/*/commons-fileupload/target/classes/
            // and packageName would be org/apache/commons/fileupload2
            String packageName = currRoot.getAbsolutePath().split(classesDirName + File.separator)[1];
            packageNameSet.add(packageName.replace(File.separator, "."));
        } else {
            // all contents of this directory are subdirectories or non-*.class files.
            // we need to traverse through the directories
            for (File currFile : files) {
                if (currFile.isDirectory()) {
                    Set<String> packageNameSet2 = classFilesWalk(currFile, classesDirName);
                    packageNameSet.addAll(packageNameSet2);
                }
            }
        }
        return packageNameSet;
    }

    /**
     * Wrapper method for retrieving the package names within the project.
     *
     * @param classesDir the classes directory of the project.
     * @return set of strings containing package names (without subpackages).
     */
    public static Set<String> retrieveProjectPackageNames(File classesDir) {
        return classFilesWalk(classesDir, classesDir.getAbsolutePath());
    }

    /**
     * Generates a new agent configuration file, usually, aop-ajc.xml.
     *
     * @param agentConfigurationPath The path to store the new agent configuration file in
     * @param specsToMonitor The set of specs to instrument
     * @param includedPackageNames The set of the client program's package names to instrument
     * @param excludedClasses The set of client program's classes to NOT instrument
     * @param enableStats Decides whether to enable statistics or not
     * @param verboseAgent Decides whether to show weave info or not
     */
    public static void generateNewAgentConfigurationFile(String agentConfigurationPath,
                                                         Set<String> specsToMonitor,
                                                         Set<String> includedPackageNames,
                                                         Set<String> excludedClasses,
                                                         boolean enableStats,
                                                         boolean verboseAgent) {
        try (PrintWriter writer = new PrintWriter(agentConfigurationPath)) {
            // Write header
            writer.println("<aspectj>");
            writer.println("<aspects>");
            // Write body
            for (String spec : specsToMonitor) {
                writer.println("<aspect name=\"mop." + spec + "\"/>");
            }
            // Write footer
            writer.println("</aspects>");
            if (verboseAgent) {
                writer.println("<weaver options=\"-nowarn -Xlint:ignore -verbose -showWeaveInfo\">");
            } else {
                writer.println("<weaver options=\"-nowarn -Xlint:ignore\">");
            }
            if (includedPackageNames != null) {
                for (String packageName : includedPackageNames) {
                    writer.println("<include within=\"" + packageName + "..*\"/>");
                }
                if (enableStats && !includedPackageNames.isEmpty()) {
                    writer.println("<include within=\"org.apache.maven.surefire..*\"/>");
                }
            }
            if (excludedClasses != null) {
                for (String nonAffectedClass : excludedClasses) {
                    writer.println("<exclude within=\"" + nonAffectedClass + "\"/>");
                }
            }
            writer.println("</weaver>");

            writer.println("</aspectj>");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // TODO: Currently this approach does not consider library.
    // It should be addressed at some point.
    @Deprecated
    public static void generateNewBaseAspect(String outputPath, Set<String> impactedMethods) {
        try (PrintWriter writer = new PrintWriter(outputPath)) {
            writer.println("package mop;");
            writer.println("");
            writer.println("public aspect BaseAspect {");
            writer.println("  pointcut notwithin() :");
            writer.println("  !within(sun..*) &&");
            writer.println("  !within(java..*) &&");
            writer.println("  !within(javax..*) &&");
            writer.println("  !within(javafx..*) &&");
            writer.println("  !within(com.sun..*) &&");
            writer.println("  !within(org.dacapo.harness..*) &&");
            writer.println("  !within(net.sf.cglib..*) &&");
            writer.println("  !within(mop..*) &&");
            writer.println("  !within(org.h2..*) &&");
            writer.println("  !within(org.sqlite..*) &&");
            writer.println("  !within(javamoprt..*) &&");
            writer.println("  !within(rvmonitorrt..*) &&");
            writer.println("  !within(org.junit..*) &&");
            writer.println("  !within(junit..*) &&");
            writer.println("  !within(java.lang.Object) &&");
            writer.println("  !within(com.runtimeverification..*) &&");
            writer.println("  !within(org.apache.maven.surefire..*) &&");
            writer.println("  !within(org.mockito..*) &&");
            writer.println("  !within(org.powermock..*) &&");
            writer.println("  !within(org.easymock..*) &&");
            writer.println("  !within(com.mockrunner..*) &&");
            if (impactedMethods.isEmpty()) {
                writer.println("  !within(org.jmock..*);");
            } else {
                writer.println("  !within(org.jmock..*) &&");
                writer.print("(");
                boolean firstImpactedMethod = true;
                for (String impactedMethod : impactedMethods) {
                    String reformatted = MethodsHelper.convertAsmToJava(impactedMethod);
                    reformatted = reformatted.replaceAll("\\$[0-9]*#", "#");
                    // Cannot have "<" or ">":
                    if (reformatted.contains("<clinit>")) {
                        // Nothing we can do about <clinit>
                        continue;
                    }
                    boolean isConstructor = reformatted.contains("<init>");
                    reformatted = reformatted.replace("<init>", "new");
                    if (firstImpactedMethod) {
                        firstImpactedMethod = false;
                    } else {
                        writer.print(" || ");
                    }
                    // TODO: Currently doesn't consider signature, maybe do something about it in the future.
                    writer.print("withincode("
                            + (isConstructor ? "" : "* ")
                            + reformatted.replace('/', '.').replace('#', '.')
                            .split("\\(")[0] + "(..))");
                }
                writer.print(");");
            }
            writer.println("}");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Relocates the generated violation-counts file.
     *
     * @param originalDir directory that should contain the original violation-counts
     * @param newDir directory where violation-counts should be moved to
     * @param mode the phase for the relocated violation-counts file (either "critical" or "background")
     * @return absolute path to the new location of violation-counts if move was successful, empty string if not
     */
    public static String moveViolationCounts(File originalDir, String newDir, String mode) {
        // If we get a handle on violation-counts from VMS, then we don't have to do this in the first place...
        File violationCounts = new File(originalDir + File.separator + "violation-counts");
        try {
            File newViolationCounts = new File(newDir + File.separator + mode + "-violation-counts.txt");
            if (!violationCounts.exists()) {
                newViolationCounts.delete();
                newViolationCounts.createNewFile();
            } else {
                Files.move(violationCounts.toPath(), newViolationCounts.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return newViolationCounts.getAbsolutePath();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Writes the provided set of specifications to the specified file, delimited by newline.
     *
     * @param specs set of specs to write to the file
     * @param file the file to write to
     * @throws FileNotFoundException when file cannot be found
     */
    public static void writeSpecsToFile(Set<String> specs, File file) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(file)) {
            for (String spec : specs) {
                writer.println(spec);
            }
        }
    }

    // Copied from STARTS
    // Determines whether classpath for the project has been modified.
    // For instance, version changes in jars may be reflected by renames to jar files.
    public static boolean hasDifferentClassPath(List<String> sfPathString, String artifactsDir)
            throws MojoExecutionException {
        if (sfPathString.isEmpty()) {
            return false;
        }
        String oldSfPathFileName = Paths.get(artifactsDir, StartsConstants.SF_CLASSPATH).toString();
        if (!new File(oldSfPathFileName).exists()) {
            return true;
        }
        try {
            List<String> oldClassPathLines = Files.readAllLines(Paths.get(oldSfPathFileName));
            if (oldClassPathLines.size() != 1) {
                throw new MojoExecutionException(StartsConstants.SF_CLASSPATH + " is corrupt! Expected only 1 line.");
            }
            List<String> oldClassPathElements = getCleanClassPath(oldClassPathLines.get(0));
            // comparing lists and not sets in case order changes
            if (sfPathString.equals(oldClassPathElements)) {
                return false;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return true;
    }

    // Copied from STARTS
    // Determines whether jar dependencies still have the same checksum.
    public static boolean hasDifferentJarChecksum(List<String> cleanSfClassPath,
                                                  List<Pair> jarCheckSums,
                                                  String artifactsDir) throws MojoExecutionException {
        if (cleanSfClassPath.isEmpty()) {
            return false;
        }
        String oldChecksumPathFileName = Paths.get(artifactsDir, StartsConstants.JAR_CHECKSUMS).toString();
        if (!new File(oldChecksumPathFileName).exists()) {
            return true;
        }
        boolean noException = true;
        try {
            List<String> lines = Files.readAllLines(Paths.get(oldChecksumPathFileName));
            Map<String, String> checksumMap = new HashMap<>();
            for (String line : lines) {
                String[] elems = line.split(StartsConstants.COMMA);
                checksumMap.put(elems[0], elems[1]);
            }
            jarCheckSums = new ArrayList<>();
            for (String path : cleanSfClassPath) {
                Pair<String, String> pair = Writer.getJarToChecksumMapping(path);
                jarCheckSums.add(pair);
                String oldCS = checksumMap.get(pair.getKey());
                noException &= pair.getValue().equals(oldCS);
            }
        } catch (IOException ioe) {
            noException = false;
            // reset to null because we don't know what/when exception happened
            jarCheckSums = null;
            ioe.printStackTrace();
        }
        return !noException;
    }

    // Copied from STARTS
    // Return a list of classpath.
    public static List<String> getCleanClassPath(String cp) {
        List<String> cpPaths = new ArrayList<>();
        String[] paths = cp.split(File.pathSeparator);
        String classes = File.separator + StartsConstants.TARGET +  File.separator + StartsConstants.CLASSES;
        String testClasses = File.separator + StartsConstants.TARGET + File.separator + StartsConstants.TEST_CLASSES;
        for (String path : paths) {
            // TODO: should we also exclude SNAPSHOTS from same project?
            if (path.contains(classes) || path.contains(testClasses)) {
                continue;
            }
            cpPaths.add(path);
        }
        return cpPaths;
    }

    /**
     * A utility method to modify environment variable.
     * @param key key of the environment variable.
     * @param value value of the environment variable.
     */
    public static void setEnv(String key, String value) {
        try {
            Map<String, String> env = System.getenv();
            Class<?> classes = env.getClass();
            Field field = classes.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.put(key, value);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to set environment variable", ex);
        }
    }
}
