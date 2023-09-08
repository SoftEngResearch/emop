package demo;

import java.util.StringTokenizer;

class D {
  String d(String s, boolean flag) {
    StringTokenizer t = new StringTokenizer(s);
    String out = "";
    if (flag) {
      if (t.hasMoreTokens()) {
        out = t.nextToken();
      }
    } else {
      out = t.nextToken();
    } return out;
  }
}
