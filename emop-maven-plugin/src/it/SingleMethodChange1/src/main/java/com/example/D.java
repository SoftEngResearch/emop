package com.example;
import java.util.*;
public class D {

    public String Dmethod1(String s, boolean flag) {
        StringTokenizer t = new StringTokenizer(s);
        String out = "";
        if (flag) {
            if (t.hasMoreTokens()) {
                out = t.nextToken();
            }
        } else {
            out = t.nextToken();
        }
        return out;
    }

}
