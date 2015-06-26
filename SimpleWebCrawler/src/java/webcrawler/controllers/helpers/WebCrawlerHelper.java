package webcrawler.controllers.helpers;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import webcrawler.models.SiteGraph;

@Component
public class WebCrawlerHelper {
  private final Logger logger = LogManager.getLogger(this.getClass());
  
  @Autowired
  protected SiteGraphHelper siteGraphHelper;
  
  @Autowired
  protected JsoupHelper jsoupHelper;
  
  public WebCrawlerHelper() {
    this.clearData();
  }
  
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
    ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    Queue<String> urlQueue = new ConcurrentLinkedQueue<>();
    urlQueue.add(absoluteUrl);
    while (!urlQueue.isEmpty() || pool.getActiveCount() > 0) {
      pool.submit(new WebCrawlerCallable(pool, siteGraphHelper, jsoupHelper, siteGraph, urlQueue,
          maxSize));
    }
    pool.shutdown();
    logger.exit();
  }
  
  public void clearData() {
    File dataDir = new File("data/").getAbsoluteFile();
    for (File file : dataDir.listFiles())
      file.delete();
  }
}
