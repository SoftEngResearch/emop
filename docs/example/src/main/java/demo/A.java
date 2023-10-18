package demo;

import java.util.List;

class A {
  String a(List i, String sep) {
    String o = "";
    for (Object a : i) {
      o += a.toString() + sep;
    }
    return o;
  }
}
