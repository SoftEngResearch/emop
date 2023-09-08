package com.example;
import java.util.*;
public class B extends A {
    public String Bmethod1(List l) {
        String i;
        i = Amethod1(l, " ");
        return i.trim();
    }

    //-
    public boolean Bmethod2(int a){
        return a > 18;
    }
/*
 * line 6 ->  i = Amethod1(Collections.synchronizedList(l), " ");

 */

    Boolean flag() {
        return true;
    }
}
