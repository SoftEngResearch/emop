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
import java.util.List;
import java.util.Map;

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
}
