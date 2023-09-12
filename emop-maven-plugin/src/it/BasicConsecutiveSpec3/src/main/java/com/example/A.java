package com.example;

import java.util.*;

public class A {
    public String Amethod1(List i, String sep) {
        String o = "";
        int h = 10;
        
        for (Object a : i) {
            o += a.toString() + sep;
        }
        
        return o;
    }

    public String Amethod1SameMethod(List i, String sep){
        String o = "";
        int h = 10;
        for (Object a : i) {
            o += a.toString() + sep;
        }
        return o;
    }

    public int AsameNameMethod(){
        int a = 1;
        //-
        int b = 2;
        return 0;
    }

    public int AsameNameMethod(int a, int b){
        return a + b;
    }





    
}
