package demo;

import org.junit.Test;

public class ETest {
  @Test
  public void testE() throws Exception {
    E e = new E();
    String u = "https://bing.com";
    assert(e.e(u + " b", "ISO-8859-1").equals(u));
  }
}
