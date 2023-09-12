package com.example;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.lang.reflect.Array;
import java.util.*;

public class CclassTest extends TestCase {

    private C Cclass = new C();
    public CclassTest( String Testname){
        super(Testname);
    
    }


    public void testCmethod1(){

        List<String> s = Arrays.asList("Hello","World","Me");

        String output = Cclass.Cmethod1(s);

        System.out.println(output);
        assertEquals("Hello: Hello World Me",output);


    }

}
