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
  private final int POOL_SIZE = 10;
  @Autowired
  protected SiteGraphHelper siteGraphHelper;
  
  @Autowired
  protected JsoupHelper jsoupHelper;
  
  protected ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(POOL_SIZE);
  
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
    Queue<String> urlQueue = new ConcurrentLinkedQueue<>();
    urlQueue.add(absoluteUrl);
    while (!urlQueue.isEmpty() && siteGraph.size() < maxSize) {
      boolean isEmpty = false;
      while (siteGraph.size() < maxSize && !(isEmpty = urlQueue.isEmpty()) ||
          pool.getActiveCount() > 0 || !pool.getQueue().isEmpty()) {
        if (!isEmpty && pool.getQueue().size() < POOL_SIZE)
          pool.submit(new WebCrawlerCallable(siteGraphHelper, jsoupHelper, siteGraph, urlQueue,
              maxSize));
      }
      Thread.sleep(500);
    }
    logger.exit();
  }
  
  public void clearData() {
    logger.entry();
    File dataDir = new File("data/").getAbsoluteFile();
    for (File file : dataDir.listFiles())
      if (!".gitignore".equals(file.getName()))
        file.delete();
    logger.exit();
  }
}
