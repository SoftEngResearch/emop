package demo;

import java.util.Collections;
import java.util.List;

class B extends A {
  String b(List l) {
    String i;
    i = a(l, " ");
    return i.trim();
  }
  Boolean flag() {
    return true;
  }
}
