package edu.cornell.emop.util;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.objectweb.asm.Type;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MethodsHelper {

    /* Mapping to keep methods to their beginning and ending line number */
    private static Map<String, ArrayList<Integer>> methodsToLineNumbers = new HashMap<>();
    /* Mapping to keep classes to all the their methods */
    private static Map<String, ArrayList<String>> classToMethods = new HashMap<>();

    private static Set<String> cachedFile = new HashSet<>();

    /**
     * This method returns a map of method names to their beginning and ending line
     * numbers in the given filepath.
     * The method uses Roaster to parse the Java source code and extract the line
     * numbers of each method.
     * The method also caches the results for faster access in future calls.
     *
     * @param filePath The path of the Java source file to be parsed.
     * @return A map of method names to their line numbers in the given file.
     * @throws Exception If an error occurs while reading or parsing the file.
     */
    public static Map<String, ArrayList<Integer>> getMethodLineNumbers(String filePath) throws Exception {
        if (cachedFile.contains(filePath)) {
            return methodsToLineNumbers;
        }

        String tp = filePath.replace(".java", "");
        String[] classesNames = tp.split("\\$");
        File file = new File(classesNames[0] + ".java");

        JavaClassSource javaClass = Roaster.parse(JavaClassSource.class, Files.newInputStream(file.toPath()));
        String sourceCode = new String(Files.readAllBytes(Paths.get(file.toURI())));
        ArrayList<String> methods = new ArrayList<>();

        for (int i = 1; i < classesNames.length; i++) {
            for (JavaType<?> innerclass : javaClass.getNestedTypes()) {
                if (innerclass instanceof JavaClassSource) {
                    JavaClassSource innerClassSource = (JavaClassSource) innerclass;
                    if (innerclass.getName().equals(classesNames[i])) {
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

            String temp = method.toSignature().split(" :")[0];
            String[] temps = temp.split(" ");
            temp = "";
            for (int i = 1; i < temps.length; i++) {
                temp = temp + temps[i];
            }
            methods.add(temp);
            methodsToLineNumbers.put(filePath + "#" + temp, nums);
        }
        classToMethods.put(filePath, methods);
        cachedFile.add(filePath);
        return methodsToLineNumbers;
    }

    /**
     * This method converts an ASM method signature to a Java method signature.
     * The method uses the Type class from the ASM library to extract the argument
     * types from the ASM signature.
     * The method then constructs a Java method signature by appending the class
     * names of the argument types.
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
     * This method converts an ASM method signature to a Java method signature.
     * The method splits the given ASM signature into its name and argument parts.
     * The method then calls convertAsmSignatureToJava to convert the argument part
     * of the ASM signature to a Java signature.
     * The method finally constructs the full Java method signature by appending the
     * name and argument parts.
     *
     * @param methodAsmSignature The ASM method signature to be converted. It should
     *                           have the format filePath#methodSignature.
     * @return The Java method signature corresponding to the given ASM signature.
     */
    public static String convertAsmToJava(String methodAsmSignature) {
        String methodArgs = "(" + methodAsmSignature.split("\\(")[1];
        String javaArgs = convertAsmSignatureToJava(methodArgs);
        String temp = methodAsmSignature.split("\\(")[0] + javaArgs;
        return temp;
    }

    /**
     * This method returns the name of the method that wraps the given line number
     * in the given file.
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
     *         found.
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

    // test use
    public static void putclassTomethod(String a, ArrayList<String> b) {
        classToMethods.put(a, b);
    }

    // test use
    public static void putmethodsToLineNumbers(String a, ArrayList<Integer> b) {
        methodsToLineNumbers.put(a, b);
    }

}
