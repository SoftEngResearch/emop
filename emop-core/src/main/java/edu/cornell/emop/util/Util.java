package edu.cornell.emop.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
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
import java.util.stream.Collectors;

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
        Set<String> fullSet = classFilesWalk(classesDir, classesDir.getAbsolutePath());
        return fullSet;
    }

    /**
     * Analyzes a violations file and returns a set of violations.
     *
     * @param violationsPath The file where violations are located
     * @return A set of violations, each violation is a list containing the specification, a class, and a line number
     */
    public static Set<List<String>> parseViolations(String violationsPath) {
        try {
            return Files.readAllLines(new File(violationsPath).toPath())
                    .stream()
                    .map(Util::parseViolation)
                    .collect(Collectors.toSet());
        } catch (IOException exception) {
            return new HashSet<>();
        }
    }

    /**
     * Analyzes a violations file and returns a list of violations.
     *
     * @param violationsPath The file where violations are located
     * @return A set of violation specifications
     */
    public static Set<String> parseViolationSpecs(String violationsPath) {
        try {
            return Files.readAllLines(new File(violationsPath).toPath())
                    .stream()
                    .map(violation -> parseViolation(violation).get(0))
                    .collect(Collectors.toSet());
        } catch (IOException exception) {
            return new HashSet<>();
        }
    }

    /**
     * Parses the string form of a violation from the file into a list containing the specification, class, and line number.
     *
     * @param violation Violation line to parse
     * @return Triple of violation specification, class, and line number
     */
    public static List<String> parseViolation(String violation) {
        List<String> result = new ArrayList<>();
        String[] parsedViolation = violation.split(" ");
        result.add(parsedViolation[2]); // name of specification

        String[] classAndLineNum = parsedViolation[8].split(":");
        String classInfo = classAndLineNum[0];
        String[] classLocationAndNameExt = classInfo.split("\\(");
        String classNameExt = classLocationAndNameExt[1];
        String classLocation = classLocationAndNameExt[0];
        String[] classLocationFragments = classLocation.split("\\.");
        classLocationFragments[classLocationFragments.length - 1] = null; // remove function name
        classLocationFragments[classLocationFragments.length - 2] = classNameExt; // add extension to class name
        String classResult = String.join("/", classLocationFragments);
        classResult = classResult.substring(0, classResult.length() - 5); // remove function and final '/'
        result.add(classResult); // class, formatted as the diff will expect it to be

        String lineNum = classAndLineNum[1];
        result.add(lineNum.substring(0, lineNum.indexOf(")"))); // line number
        return result;
    }
}
