package com.example;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.*;

public class DclassTest extends TestCase {

    public DclassTest(String Testname) {
        super(Testname);
    }


    private D dInstance = new D();

    


    public void testDmethod1WithFlagTrue() {
       
        String input = "Hello World Test";

    
        String result = dInstance.Dmethod1(input, true);


        assertEquals("Hello", result);
    }


    public void testDmethod1WithFlagFalse() {

        String input = "Hello World Test";

        // When
        String result = dInstance.Dmethod1(input, false);

        // Then
        assertEquals("Hello", result);
    }


    public void testDmethod1WithEmptyStringAndFlagTrue() {
        // Given
        String input = "";

        // When
        String result = dInstance.Dmethod1(input, true);

        // Then
        assertEquals("", result);
    }

    
    public void testDmethod1WithEmptyStringAndFlagFalse() {
       
        String input = "";

       try{
        String result = dInstance.Dmethod1(input, false);
       }catch(Exception e){
            assertTrue( true);
            return;
       }

       assertTrue( false);




      

    }
    

}
