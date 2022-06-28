package edu.cornell.emop.util;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
                        returnedFileNames.add(fileName);
                    }
                }
            }
        }
        return returnedFileNames;
    }

}
