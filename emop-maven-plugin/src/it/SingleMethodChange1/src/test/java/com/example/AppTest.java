package com.example;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.*;



/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }


    public void testAmethod1() {
        A a = new A();
        List<String> list = Arrays.asList("one", "two", "three");
        String sep = "-";
        String result = a.Amethod1(list, sep);
        // Since the for loop in Amethod1 is commented out, the expected result is an empty string
        assertEquals("one-two-three-", result);
    }


    public void testAmethod1SameMethod() {
        A a = new A();
        List<String> list = Arrays.asList("one", "two", "three");
        String sep = "-";
        String result = a.Amethod1SameMethod(list, sep);
        String expected = "one-two-three-";
        assertEquals(expected, result);
    }


    public void testAsameNameMethodNoArgs() {
        A a = new A();
        int result = a.AsameNameMethod();
        assertEquals(0, result);
    }


    public void testAsameNameMethodWithArgs() {
        A a = new A();
        int result = a.AsameNameMethod(3, 4);
        assertEquals(7, result);
    }
}
