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
        System.out.println("CCC: " + Arrays.asList(files));
////        if (files != null) {
////            for (File currFile : files) {
//                if (currFile.isDirectory()) {
//                    List<String> internal = findFilesOfType(currFile, extension);
//                    returnedFileNames.addAll(internal);
//                } else {
//                    System.out.println("BBBB: " + Arrays.asList(files));
//                    String fileName = currFile.getName();
//                    if (fileName.endsWith(extension)) {
//                        returnedFileNames.add(fileName);
//                    }
//                }
//            }
//        }
        return returnedFileNames;
    }

}
