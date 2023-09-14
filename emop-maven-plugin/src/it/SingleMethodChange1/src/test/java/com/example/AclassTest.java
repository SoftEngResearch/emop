package com.example;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.*;
public class AclassTest extends TestCase{
    public AclassTest( String Testname){
        super(Testname);
    }

    public void testAmethod1() {
        A a = new A();
        List<String> list = Arrays.asList("one", "two", "three");
        String sep = "-";
        String result = a.Amethod1(list, sep);
        // Since the for loop in Amethod1 is commented out, the expected result is an empty string
        assertEquals("one-two-three-", result);
    }



}
