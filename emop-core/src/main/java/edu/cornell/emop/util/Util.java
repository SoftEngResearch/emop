package edu.cornell.emop.util;

import java.io.File;
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
     * Borrowed from DSI.
     *
     * @param currRoot the current location
     * @param dirName the name of the directory
     * @return set of all package names within the project.
     */
    public static Set<String> classFilesWalk(File currRoot, String dirName) {
        Set<String> packageNameSet = new HashSet<>();
        File[] files = currRoot.listFiles();
        if (files == null) {
            return packageNameSet;
        }
        for (File currFile : files) {
            if (currFile.isDirectory()) { // recurse into subdirectory
                Set<String> packageNameSet2 = classFilesWalk(currFile, dirName);
                packageNameSet.addAll(packageNameSet2);
            } else { // regular file, now we just need to get the name of the path and apply
                String fileName = currFile.getName();
                if (fileName.endsWith(".class")) { // only checking class files
                    // get parent directory
                    String parentName = currFile.getParent().split(dirName + File.separator)[1];
                    String packageName = parentName.replaceAll(File.separator, ".");
                    packageNameSet.add(packageName);
                }
            }
        }
        return packageNameSet;
    }

    /**
     * Wrapper method for retrieving the package names within the project.
     * FIXME: there is most likely a more efficient way to filter out the subpackages...
     *
     * @param classesDir the classes directory of the project.
     * @return set of strings containing package names (without subpackages).
     */
    public static Set<String> retrieveProjectPackageNames(File classesDir) {
        Set<String> fullSet = classFilesWalk(classesDir, classesDir.getAbsolutePath());
        // exclude subpackage names (not really necessary, but for the sake of having a clean BaseAspect)
        Set<String> subPackageNames = new HashSet<>();
        for (String packageName : fullSet) {
            for (String otherPackageName : fullSet) {
                if (!packageName.equals(otherPackageName) && packageName.contains(otherPackageName)) {
                    subPackageNames.add(packageName);
                }
            }
        }
        fullSet.removeAll(subPackageNames);
        return fullSet;
    }
}
