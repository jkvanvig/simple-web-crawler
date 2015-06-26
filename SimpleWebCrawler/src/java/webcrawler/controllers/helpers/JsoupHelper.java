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
  
  public Response connect(String url) throws IOException {
    logger.entry(url);
    Connection con =
        Jsoup
            .connect(url)
            .userAgent(
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
            .timeout(10000);
    
    Response resp;
    resp = con.execute();
    for (int retries = 0; retries < 3 && resp.statusCode() < 200 && resp.statusCode() >= 300; retries++)
      resp = con.execute();
    
    return logger.exit(resp);
  }
  
  public Document parseResponse(Response resp) throws IOException {
    Document doc;
    doc = resp.parse();
    return doc;
  }
}
