package edu.cornell.emop.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

public class Util {
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

    public static Set<String> retrieveSpecListFromJar(String jarPath, Log log) {
        // we assume that the jar contains a specs.txt
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
     * @param currRoot       the current directory location.
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
            // we found a class file, which means we are in a directory with class files
            // (package)
            // No need to traverse further because AspectJ within(${PACKAGE_NAME}..*) syntax
            // instruments subpackages.
            // The [1] here points to the part of the path after the location of the classes
            // directory.
            // ex) for commons-fileupload, classesDirName would be
            // /home/*/commons-fileupload/target/classes/
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
        Set<String> fullSet = classFilesWalk(classesDir, classesDir.getAbsolutePath());
        return fullSet;
    }

    public static void generateNewMonitorFile(String monitorFilePath,
            Set<String> specsToMonitor,
            Set<String> includesPackageNames,
            Set<String> nonAffectedClasses) {

        System.out.println("Generating new monitor file at " + monitorFilePath);
        System.out.println("Specs to monitor: " + specsToMonitor);
        System.out.println("Packages to include: " + includesPackageNames);
        System.out.println("Classes to exclude: " + nonAffectedClasses);

        try (PrintWriter writer = new PrintWriter(monitorFilePath)) {
            // Write header
            writer.println("<aspectj>");
            writer.println("<aspects>");
            // Write body
            for (String spec : specsToMonitor) {
                writer.println("<aspect name=\"mop." + spec + "\"/>");
            }
            // Write footer
            writer.println("</aspects>");
            // TODO: Hard-coded for now, make optional later (-verbose -showWeaveInfo)
            writer.println("<weaver options=\"-nowarn -Xlint:ignore\">");
            if (includesPackageNames != null) {
                for (String packageName : includesPackageNames) {
                    writer.println("<include within=\"" + packageName + "..*\"/>");
                }
            }
            if (nonAffectedClasses != null) {
                for (String nonAffectedClass : nonAffectedClasses) {
                    writer.println("<exclude within=\"" + nonAffectedClass + "\"/>");
                }
            }
            writer.println("</weaver>");

            writer.println("</aspectj>");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Relocates the generated violation-counts file.
     * 
     * @param originalDir directory that should contain the original
     *                    violation-counts
     * @param newDir      directory where violation-counts should be moved to
     * @param mode        the phase for the relocated violation-counts file (either
     *                    "critical" or "background")
     * @return absolute path to the new location of violation-counts if move was
     *         successful, empty string if not
     */
    public static String moveViolationCounts(File originalDir, String newDir, String mode) {
        // If we get a handle on violation-counts from VMS, then we don't have to do
        // this in the first place...
        File violationCounts = new File(originalDir + File.separator + "violation-counts");
        try {
            File newViolationCounts = new File(newDir + File.separator + mode + "-violation-counts.txt");
            if (!violationCounts.exists()) {
                newViolationCounts.delete();
                newViolationCounts.createNewFile();
                return "";
            }
            Files.move(violationCounts.toPath(), newViolationCounts.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return newViolationCounts.getAbsolutePath();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Writes the provided set of specifications to the specified file, delimited by
     * newline.
     * 
     * @param specs set of specs to write to the file
     * @param file  the file to write to
     * @throws FileNotFoundException when file cannot be found
     */
    public static void writeSpecsToFile(Set<String> specs, File file) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(file)) {
            for (String spec : specs) {
                writer.println(spec);
            }
        }
    }
}
