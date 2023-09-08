package com.example;
import java.util.*;
import java.net.*;
public class E {
    void Emethod1(String u, String e) throws Exception {
        D d = new D();
        assert (!u.isEmpty());
        String url = d.Dmethod1(u, false);
        if (url.startsWith("https")) {
            String s = URLDecoder.decode(url, e);
            System.out.print(s);
        }
    }

}

/*
 *  line11 -> " "
 */
