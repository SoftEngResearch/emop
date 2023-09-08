package com.example;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.lang.reflect.Array;
import java.util.*;

public class EclassTest extends TestCase{

    public EclassTest(String Testname) {
        super(Testname);
    }

    public void testEmethod1() throws Exception{
        E testE = new E();
        
        testE.Emethod1("a","b");
    }

    
}
