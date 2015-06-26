package webcrawler.controllers.helpers;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import webcrawler.models.SiteGraph;
import webcrawler.models.SiteGraphNode;

@Component
public class WebCrawlerHelper {
  private final Logger logger = LogManager.getLogger(this.getClass());
  
  @Autowired
  protected SiteGraphHelper siteGraphHelper;
  
  @Autowired
  protected JsoupHelper jsoupHelper;
  
  public String findBaseUrl(String url) throws IOException {
    logger.entry(url);
    if (!url.startsWith("http"))
      url = "http://" + url;
    Response resp = jsoupHelper.connect(url);
    return logger.exit(resp.statusCode() >= 200 && resp.statusCode() < 300 ? resp.url().toString()
        : null);
  }
  
  public void crawlDomain(SiteGraph siteGraph, String absoluteUrl, long maxSize) throws Exception {
    logger.entry(absoluteUrl, maxSize);
    ExecutorService pool = Executors.newCachedThreadPool();
    Future<SiteGraphNode> root =
        pool.submit(new WebCrawlerCallable(pool, siteGraphHelper, jsoupHelper, siteGraph,
            absoluteUrl, maxSize));
    root.get();
    logger.exit();
  }
}
