package webcrawler.controllers.helpers;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import webcrawler.models.SiteGraph;
import webcrawler.models.SiteGraphNode;

public class WebCrawlerCallable implements Callable<SiteGraphNode> {
  private static final Logger logger = LogManager.getLogger(WebCrawlerCallable.class);
  
  protected final ExecutorService pool;
  protected SiteGraphHelper siteGraphHelper;
  protected JsoupHelper jsoupHelper;
  protected SiteGraph siteGraph;
  protected Queue<String> urlQueue;
  protected long maxSize;
  
  public WebCrawlerCallable(ExecutorService pool, SiteGraphHelper siteGraphHelper,
      JsoupHelper jsoupHelper, SiteGraph siteGraph, Queue<String> urlQueue, long maxSize) {
    this.pool = pool;
    this.siteGraphHelper = siteGraphHelper;
    this.jsoupHelper = jsoupHelper;
    this.siteGraph = siteGraph;
    this.urlQueue = urlQueue;
    this.maxSize = maxSize;
  }
  
  @Override
  public SiteGraphNode call() throws Exception {
    logger.entry();
    String absoluteUrl;
    synchronized (urlQueue) {
      absoluteUrl = urlQueue.poll();
    }
    if (absoluteUrl == null)
      return logger.exit(null);
    String relativeUrl = siteGraphHelper.getRelativeUrl(siteGraph.getBaseUrl(), absoluteUrl);
    
    // Initial validation - make sure we haven't already seen this page
    if (!siteGraphHelper.validate(siteGraph, relativeUrl, maxSize))
      return logger.exit(siteGraph.findSiteGraphNode(relativeUrl));
    
    Response resp = jsoupHelper.connect(absoluteUrl);
    
    // Secondary validation - make sure it didn't redirect to a page we've seen
    if (!siteGraphHelper.revalidate(siteGraph, absoluteUrl, relativeUrl, resp, maxSize))
      return logger.exit(siteGraph.findSiteGraphNode(relativeUrl));
    
    relativeUrl = siteGraphHelper.getRelativeUrl(siteGraph.getBaseUrl(), resp.url().toString());
    absoluteUrl = resp.url().toString();
    logger.debug("New url: {}", relativeUrl);
    
    Document doc = jsoupHelper.parseResponse(resp);
    
    SiteGraphNode siteGraphNode = siteGraph.addParsedSiteGraphNode(relativeUrl);
    
    // get all script, img, stylesheet, and icon dependencies
    siteGraphHelper.addStaticAssets(siteGraph, siteGraphNode, doc);
    
    // get all links and recursively call the processPage method
    Elements as = doc.select("a[href]");
    Set<String> linkHrefs = new HashSet<>(as.size());
    for (Element a : as) {
      if (siteGraph.size() >= maxSize)
        break;
      String absoluteHref = a.attr("abs:href");
      if (absoluteHref.startsWith(siteGraph.getBaseUrl()) && !absoluteHref.endsWith(".png") &&
          !absoluteHref.endsWith(".zip") && !absoluteHref.endsWith(".eps")) {
        logger.trace("Adding link: {}", absoluteHref);
        linkHrefs.add(absoluteHref);
        siteGraph.addLink(siteGraphNode, siteGraph.addSiteGraphNode(siteGraphHelper.getRelativeUrl(
            siteGraph.getBaseUrl(), absoluteHref)));
      } else {
        logger.trace("Skipping link: {}", absoluteHref);
      }
    }
    synchronized (urlQueue) {
      urlQueue.addAll(linkHrefs);
    }
    return logger.exit(siteGraphNode);
  }
}
