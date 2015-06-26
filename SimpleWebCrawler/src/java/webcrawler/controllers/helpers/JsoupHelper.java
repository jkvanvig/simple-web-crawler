package webcrawler.controllers.helpers;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

/**
 * The main point of this file is to have a Component to inject into the other classes, which makes
 * testing them easier.
 */
@Component
public class JsoupHelper {
  private final Logger logger = LogManager.getLogger(this.getClass());
  
  public Response connect(String url) {
    logger.entry(url);
    Connection con =
        Jsoup
            .connect(url)
            .userAgent(
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
            .timeout(10000);
    
    Response resp;
    try {
      resp = con.execute();
      for (int retries = 0; retries < 3 && resp.statusCode() < 200 && resp.statusCode() >= 300; retries++)
        resp = con.execute();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    return logger.exit(resp);
  }
  
  public Document parseResponse(Response resp) {
    Document doc;
    try {
      doc = resp.parse();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return doc;
  }
}
