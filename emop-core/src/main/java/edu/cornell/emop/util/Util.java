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

    public static void replaceSpecSelectionWithFile(String javamopAgentPath, String newFilePath) {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        URI javamopAgent = URI.create("jar:file:" + javamopAgentPath);
        try (FileSystem jarfs = FileSystems.newFileSystem(javamopAgent, env)) {
            Path newFile = Paths.get(newFilePath);
            Path pathInJarFile = jarfs.getPath("/META-INF/aop-ajc.xml");
            Files.copy(newFile, pathInJarFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Recursive routine accumulating the set of String indicating files to include.
     * Borrowed from DSI.
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
                    String packageName = parentName.replaceAll(File.separator, ".") + "..*";
                    packageNameSet.add(packageName);
                }
            }
        }
        return packageNameSet;
    }

}
