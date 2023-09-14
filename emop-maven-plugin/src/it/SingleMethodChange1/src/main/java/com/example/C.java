package com.example;
import java.util.*;
public class C {
    public String Cmethod1(List<String> l) {
        B b = new B();
        D d = new D();
        String s = b.Bmethod1(l);
        return d.Dmethod1(s, b.flag()) + ": " + s;
    }

}
