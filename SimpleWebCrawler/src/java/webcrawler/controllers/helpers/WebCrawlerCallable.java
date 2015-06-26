package webcrawler.controllers.helpers;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.UnsupportedMimeTypeException;
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
  protected String absoluteUrl;
  protected long maxSize;
  
  public WebCrawlerCallable(ExecutorService pool, SiteGraphHelper siteGraphHelper,
      JsoupHelper jsoupHelper, SiteGraph siteGraph, String absoluteUrl, long maxSize) {
    this.pool = pool;
    this.siteGraphHelper = siteGraphHelper;
    this.jsoupHelper = jsoupHelper;
    this.siteGraph = siteGraph;
    this.absoluteUrl = absoluteUrl;
    this.maxSize = maxSize;
  }
  
  @Override
  public SiteGraphNode call() throws Exception {
    logger.entry();
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
    List<Future<SiteGraphNode>> futureNodes = new ArrayList<>(as.size());
    for (Element a : as) {
      if (siteGraph.size() >= maxSize)
        break;
      String absoluteHref = a.attr("abs:href");
      if (absoluteHref.startsWith(siteGraph.getBaseUrl()) && !absoluteHref.endsWith(".png") &&
          !absoluteHref.endsWith(".zip") && !absoluteHref.endsWith(".eps")) {
        logger.trace("Adding link: {}", absoluteHref);
        futureNodes.add(pool.submit(new WebCrawlerCallable(pool, siteGraphHelper, jsoupHelper,
            siteGraph, absoluteHref, maxSize)));
      } else {
        logger.trace("Skipping link: {}", absoluteHref);
      }
    }
    for (Future<SiteGraphNode> futureNode : futureNodes) {
      try {
        SiteGraphNode linkNode = futureNode.get();
        if (linkNode != null) {
          logger.trace("Adding link: {}", linkNode.getHref());
          siteGraph.addLink(siteGraphNode, linkNode);
        }
      } catch (Exception e) {
        if (e.getCause() instanceof UnsupportedMimeTypeException) {
          siteGraph.addInvalidUrl(relativeUrl);
          logger.warn("Ignoring {}, unsupported mime type", relativeUrl);
        } else if (e.getCause() instanceof HttpStatusException) {
          siteGraph.addInvalidUrl(relativeUrl);
          int statusCode = ((HttpStatusException) e.getCause()).getStatusCode();
          logger.warn("Ignoring {}, status: {}", relativeUrl, statusCode);
        } else if (e.getCause() instanceof SocketTimeoutException) {
          logger.warn("Timeout retrieving {}", relativeUrl);
        } else {
          siteGraph.addInvalidUrl(relativeUrl);
          logger.error(e.toString());
        }
      }
    }
    return logger.exit(siteGraphNode);
  }
}
