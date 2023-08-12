package edu.cornell.emop.util;

import org.jboss.forge.roaster.Roaster;
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


public class MethodsLineNumbers {
    private static Map<String, ArrayList<Integer>> methodsToLineNumbers = new HashMap<>();
    private static Map <String, ArrayList<String>> classToMethods = new HashMap<>(); 
    private static Set<String> cachedFile = new HashSet<>();


    public static Map<String, ArrayList<Integer>> getMethodLineNumbers(String filePath) throws Exception {
        System.out.println(filePath);
        
        if (cachedFile.contains(filePath)){
            return methodsToLineNumbers;
        }
        File file = new File(filePath);
        JavaClassSource javaClass = Roaster.parse(JavaClassSource.class, Files.newInputStream(file.toPath()));
        String sourceCode = new String(Files.readAllBytes(Paths.get(file.toURI())));
        ArrayList<String> methods = new ArrayList<>();
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

    /*
     * @param methodAsmName should have the following format filePath#methodSignature
     */
    public static ArrayList<Integer> getLineNumbers(Map<String, ArrayList<Integer>> lst, String methodAsmSignature) {
        String methodArgs = "("  + methodAsmSignature.split("(")[1];
        String javaArgs = convertAsmSignatureToJava(methodArgs);
        String temp = methodAsmSignature.split("(")[0] + javaArgs; 
        return lst.get(temp);
    }


    public static String getWrapMethod(String filePath, int lineNum){
        ArrayList<String> methods = classToMethods.getOrDefault(filePath, null); 
        for (String m : methods){
            ArrayList<Integer> nums = methodsToLineNumbers.get(filePath + "#" + m);
            if (nums.get(0) <= lineNum && nums.get(1) >= lineNum){
                return m;
            }
        }
        return null;
    }


}
