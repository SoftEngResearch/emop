package edu.cornell.emop.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.objectweb.asm.Type;

public class MethodsHelper {

    /** Map from method name to their line range in the format of (begin, end). */
    private static Map<String, ArrayList<Integer>> methodsToLineNumbers = new HashMap<>();
    /** Map from a class to the methods it contains. */
    private static Map<String, ArrayList<String>> classToMethods = new HashMap<>();
    /** Set to keep track of files that have been parsed. */
    private static Set<String> cachedFile = new HashSet<>();

    public static Map<String, ArrayList<Integer>> getMethodsToLineNumbers() {
        return Collections.unmodifiableMap(methodsToLineNumbers);
    }

    /**
     * Returns a modified version of methodsToLineNumbers mapping.
     * In the original format, keys are of the format:
     *   /$full_path_to_project/src/test/java/org/example/project/SomeTest.java#method(String,String)
     * This method will return a modified version of the mapping with key in the format of:
     *   org/example/project/SomeTest#method(String,String)
     * in order to match the format of impactedMethods.
     * @return a modified methodsToLineNumbers mapping.
     */
    public static Map<String, ArrayList<Integer>> getModifiedMethodsToLineNumbers() {
        Map<String, ArrayList<Integer>> modifiedMethodsToLineNumbers = methodsToLineNumbers.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey()
                        .split("/src/main/java/|/src/test/java/")[1]
                        .replace(".java", ""),
                    e -> new ArrayList<>(e.getValue()))
                );
        return Collections.unmodifiableMap(modifiedMethodsToLineNumbers);
    }

    /**
     * Returns a map of method names to their beginning and ending line
     * numbers in the given filepath.
     * The method uses Roaster to parse the Java source code and extract the line
     * numbers of each method.
     * The method also caches the results for faster access in future calls.
     *
     * @param filePath The path of the Java source file to be parsed.
     * @throws Exception If an error occurs while reading or parsing the file.
     */
    public static void computeMethodToLineNumbers(String filePath) throws Exception {
        if (cachedFile.contains(filePath)) {
            return;
        }

        String tempPath = filePath.replace(".java", "");
        String[] classesNames = tempPath.split("\\$");
        File file = new File(classesNames[0] + ".java");

        JavaClassSource javaClass = Roaster.parse(JavaClassSource.class, Files.newInputStream(file.toPath()));
        String sourceCode = new String(Files.readAllBytes(Paths.get(file.toURI())));

        ArrayList<String> methods = new ArrayList<>();
        for (int i = 1; i < classesNames.length; i++) {
            for (JavaType<?> innerClass : javaClass.getNestedTypes()) {
                if (innerClass instanceof JavaClassSource) {
                    JavaClassSource innerClassSource = (JavaClassSource) innerClass;
                    if (innerClass.getName().equals(classesNames[i])) {
                        javaClass = innerClassSource;
                        break;
                    }
                }
            }
        }

        for (MethodSource<?> method : javaClass.getMethods()) {
            int beginLine = sourceCode.substring(0, method.getStartPosition()).split("\n").length;
            int endLine = sourceCode.substring(0, method.getEndPosition()).split("\n").length;
            ArrayList<Integer> nums = new ArrayList<>();
            nums.add(beginLine);
            nums.add(endLine);

            StringBuilder temp = new StringBuilder(method.toSignature().split(" :")[0]);
            String[] temps = temp.toString().split(" ");
            temp = new StringBuilder();
            for (int i = 1; i < temps.length; i++) {
                temp.append(temps[i]);
            }
            methods.add(temp.toString());
            methodsToLineNumbers.put(filePath + "#" + temp, nums);
        }
        classToMethods.put(filePath, methods);
        cachedFile.add(filePath);
    }

    /**
     * This method converts an ASM method signature to a Java method signature.
     * The method uses the Type class from the ASM library to extract the argument
     * types from the ASM signature.
     * The method then constructs a Java method signature by appending the class
     * names of the argument types.
     * Example: (Ljava/lang/String;I)V -> (String,int)
     *
     * @param asmSignature The ASM method signature to be converted.
     * @return The Java method signature corresponding to the given ASM signature.
     */
    public static String convertAsmSignatureToJava(String asmSignature) {
        StringBuilder javaSignature = new StringBuilder();
        Type[] argumentTypes = Type.getArgumentTypes(asmSignature);
        javaSignature.append("(");
        for (int i = 0; i < argumentTypes.length; i++) {
            String temp = argumentTypes[i].getClassName();
            String[] temps = temp.split("\\.");
            javaSignature.append(temps[temps.length - 1]);
            if (i < argumentTypes.length - 1) {
                javaSignature.append(",");
            }
        }
        javaSignature.append(")");
        return javaSignature.toString();
    }

    /**
     * This method is the higher level method that converts an ASM method signature
     * to a Java method signature.
     * The main part of conversion is done in the convertAsmSignatureToJava method.
     * Example: (Ljava/lang/String;I)V -> (String,int)
     *
     * @param methodAsmSignature The ASM method signature to be converted. It should
     *                           have the format filePath#methodSignature.
     * @return The Java method signature corresponding to the given ASM signature.
     */
    public static String convertAsmToJava(String methodAsmSignature) {
        String methodArgs = "(" + methodAsmSignature.split("\\(")[1];
        String javaArgs = convertAsmSignatureToJava(methodArgs);
        return methodAsmSignature.split("\\(")[0] + javaArgs;
    }

    /**
     * Returns the name of the method that wraps the given line number in the given
     * file.
     * The method first retrieves the list of methods in the given file from a
     * cache.
     * The method then iterates over the methods and checks if their line numbers
     * contain the given line number.
     * If a wrapping method is found, its name is returned. Otherwise, null is
     * returned.
     *
     * @param filePath The path of the Java source file to be searched.
     * @param lineNum  The line number to be searched for.
     * @return The name of the wrapping method, or null if no wrapping method is
     *         found. (Null means there is probably a bug)
     */
    public static String getWrapMethod(String filePath, int lineNum) {
        ArrayList<String> methods = classToMethods.getOrDefault(filePath, new ArrayList<>());
        for (String m : methods) {
            ArrayList<Integer> nums = methodsToLineNumbers.get(filePath + "#" + m);
            if (nums.get(0) <= lineNum && nums.get(1) >= lineNum) {
                return m;
            }
        }
        return null;
    }
}
