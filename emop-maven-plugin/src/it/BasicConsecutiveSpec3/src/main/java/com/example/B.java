package com.example;
import java.util.*;
public class B extends A {
    public String Bmethod1(List l) {
        String i;
        // -
        i = Amethod1(l, " ");
        // +
//      i = Amethod1(Collections.synchronizedList(l), " ");
        return i.trim();
    }

    //-
    public boolean Bmethod2(int a){
        return a > 18;
    }
    //+
    /* 
    public boolean Bmethod2 (int a, int b){
        return a > b;
    }
    */

    


    Boolean flag() {
        return true;
    }
}
