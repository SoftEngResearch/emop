package demo;

import java.util.List;

class C {
  String c(List<String> l) {
    B b = new B();
    D d = new D();
    String s = b.b(l);
    return d.d(s, b.flag()) + ": " + s;
  }
}
