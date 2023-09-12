package com.example;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.*;
public class BclassTest extends TestCase {

    public BclassTest( String Testname){
        super(Testname);
    }

    private B bInstance = new B();

    public void testBmethod1(){
  
        List<String> inputList = Arrays.asList("Hello", "World");

        String result = bInstance.Bmethod1(inputList);

        assertEquals("Hello World", result);

    }
    
    
}
