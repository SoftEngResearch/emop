package demo;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class CTest {
  @Test
  public void testC() {
    B b = new B();
    C c = new C();
    D d = new D();
    List<String> l1 = Arrays.asList("1", "2");
    assert(b.b(l1).equals("1 2"));
    assert(c.c(l1).equals("1: 1 2"));
    assert(d.d("1 2", false).equals("1"));
  }
}
