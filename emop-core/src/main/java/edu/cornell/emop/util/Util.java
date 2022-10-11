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
     * @param currRoot the current location.
     * @param dirName the name of the directory.
     * @return set of all package names within the project.
     */
    private static Set<String> classFilesWalk(File currRoot, String dirName) {
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
            String packageName = currRoot.getAbsolutePath().split(dirName + File.separator)[1];
            packageNameSet.add(packageName.replace(File.separator, "."));
        } else {
            // all contents of this directory are subdirectories or non-*.class files.
            // we need to traverse through the directories
            for (File currFile : files) {
                if (currFile.isDirectory()) {
                    Set<String> packageNameSet2 = classFilesWalk(currFile, dirName);
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
}
