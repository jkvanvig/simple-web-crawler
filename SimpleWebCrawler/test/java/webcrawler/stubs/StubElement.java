package webcrawler.stubs;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

public class StubElement {
  public static Element stubScript(String src) {
    Attributes attributes = new Attributes();
    attributes.put("src", src);
    Element e = new Element(Tag.valueOf("script"), src, attributes);
    return e;
  }
  
  public static Element stubSource(String src) {
    Attributes attributes = new Attributes();
    attributes.put("src", src);
    Element e = new Element(Tag.valueOf("source"), src, attributes);
    return e;
  }
  
  public static Element stubLink(String href) {
    Attributes attributes = new Attributes();
    attributes.put("href", href);
    Element e = new Element(Tag.valueOf("link"), href, attributes);
    return e;
  }
  
  public static Element stubA(String href) {
    Attributes attributes = new Attributes();
    attributes.put("href", href);
    Element e = new Element(Tag.valueOf("a"), href, attributes);
    return e;
  }
}
