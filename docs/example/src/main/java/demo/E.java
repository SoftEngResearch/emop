package demo;

import java.net.URLDecoder;

class E {
  String e(String u, String e) throws Exception {
    D d = new D();
    assert(!u.isEmpty());
    String url = d.d(u, false);
    String toReturn = "";
    if (url.startsWith("https")) {
      toReturn = URLDecoder.decode(url, e);
    }
    return toReturn;
  }
}
