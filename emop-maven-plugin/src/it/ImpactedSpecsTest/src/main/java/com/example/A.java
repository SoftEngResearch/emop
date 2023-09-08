package com.example;

import java.util.StringTokenizer;
import java.util.List;

public class A {
 
    public String Bmethod1(String s, boolean flag) {
        StringTokenizer t = new StringTokenizer(s);
        String out = "1";
        
        if (flag) { 
       if (true) {
                out = t.nextToken();
            }
        } else {
            out = t.nextToken();
        }
        return out;
    }

    public String Amethod1(List i, String sep) {
        String o = "";
        int h = 11;
        
        Bmethod1("hello",true);
        for (Object a : i) {
            o += a.toString() + sep;
        }
        
        return o;
    }
  
}
