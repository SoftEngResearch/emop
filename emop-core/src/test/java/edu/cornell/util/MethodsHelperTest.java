package edu.cornell.util;
import org.junit.Before;
import org.junit.Test;
 
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import edu.cornell.emop.util.MethodsHelper;;
public class MethodsHelperTest {
    @Before
    public void setUp() throws Exception{

        String sampleFilePath = "samplePath.java";
        ArrayList<String> methods = new ArrayList<>();
        methods.add("method1(int)");
        methods.add("method2(String)");
        MethodsHelper.putclassTomethod(sampleFilePath, methods);

        ArrayList<Integer> method1Lines = new ArrayList<>();
        method1Lines.add(10);
        method1Lines.add(20);
        MethodsHelper.putmethodsToLineNumbers(sampleFilePath + "#method1(int)", method1Lines);

        ArrayList<Integer> method2Lines = new ArrayList<>();
        method2Lines.add(25);
        method2Lines.add(35);
        MethodsHelper.putmethodsToLineNumbers(sampleFilePath + "#method2(String)", method2Lines);

    }


    @Test
    public void testConvertAsmSignatureToJava() {
        String asmSignature = "(Lcom/example/TestClass;)V"; // A simple ASM signature
        String expectedJavaSignature = "(TestClass)"; // Should return Signature
        String result = MethodsHelper.convertAsmSignatureToJava(asmSignature);
        assertEquals(expectedJavaSignature, result);
    }

    @Test
    public void testConvertAsmToJava() {
        String asmMethodSignature = "methodName(Lcom/example/TestClass;)V"; // A simple ASM method signature
        String expectedJavaMethodSignature = "methodName(TestClass)"; // Should return Signature
        String result = MethodsHelper.convertAsmToJava(asmMethodSignature);
        assertEquals(expectedJavaMethodSignature, result);
    }


    @Test 
    public void testGetWrapMethod() {
        // Test within the range of method1
        assertEquals("method1(int)", MethodsHelper.getWrapMethod("samplePath.java", 15));

        // Test within the range of method2
        assertEquals("method2(String)", MethodsHelper.getWrapMethod("samplePath.java", 30));

        // Test outside the range of any method
        assertNull(MethodsHelper.getWrapMethod("samplePath.java", 5));

        // Test with a file path that doesn't exist
        assertNull(MethodsHelper.getWrapMethod("nonexistentPath.java", 15));
    }





    

}
