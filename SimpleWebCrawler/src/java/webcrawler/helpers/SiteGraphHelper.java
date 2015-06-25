package webcrawler.helpers;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import webcrawler.models.SiteGraph;
import webcrawler.models.SiteGraphNode;
import webcrawler.models.StaticAsset;

@Component
public class SiteGraphHelper {
  
  private final Logger logger = LogManager.getLogger(this.getClass());
  
  public boolean validate(SiteGraph siteGraph, String relativeUrl, long maxSize) {
    if (siteGraph.getInvalidUrls().contains(relativeUrl) || siteGraph.contains(relativeUrl) ||
        siteGraph.size() >= maxSize) {
      logger.trace("Already visited url: {}", relativeUrl);
      return false;
    }
    return true;
  }
  
  public boolean revalidate(SiteGraph siteGraph, String absoluteUrl, String relativeUrl,
      Response resp, long maxSize) {
    if (!resp.url().toString().startsWith(siteGraph.getBaseUrl())) {
      siteGraph.addInvalidUrl(relativeUrl);
      logger.debug("Revalidate - external url: {}, original: {}", resp.url().toString(),
          relativeUrl);
      return false;
    }
    if (!validate(siteGraph, getRelativeUrl(siteGraph.getBaseUrl(), resp.url().toString()), maxSize)) {
      siteGraph.addInvalidUrl(relativeUrl);
      logger.debug("Revalidate - already visited url: {}, original: {}",
          getRelativeUrl(siteGraph.getBaseUrl(), resp.url().toString()), relativeUrl);
      return false;
    }
    if (resp.statusCode() < 200 || resp.statusCode() > 299) {
      siteGraph.addInvalidUrl(relativeUrl);
      logger.debug("Unsuccessful GET, url: {}, status: {}", resp.url().toString(),
          resp.statusCode());
      return false;
    }
    return true;
  }
  
  public void addStaticAssets(SiteGraph siteGraph, SiteGraphNode siteGraphNode, Document doc) {
    // get all script dependencies
    this.addStaticAssets(siteGraph, siteGraphNode, doc, "script[src]", "src");
    
    // get all img dependencies
    this.addStaticAssets(siteGraph, siteGraphNode, doc, "img[src]", "src");
    
    // get all media dependencies
    this.addStaticAssets(siteGraph, siteGraphNode, doc, "source[src]", "src");
    
    // get all stylesheet dependencies
    this.addStaticAssets(siteGraph, siteGraphNode, doc, "link[href][rel=stylesheet]", "href");
    
    // get all icon dependencies
    this.addStaticAssets(siteGraph, siteGraphNode, doc, "link[href][rel=icon]", "href");
  }
  
  protected void addStaticAssets(SiteGraph siteGraph, SiteGraphNode siteGraphNode, Document doc,
      String select, String attr) {
    Elements elements = doc.select(select);
    List<StaticAsset> assets = new ArrayList<>(elements.size());
    for (Element element : elements) {
      String value = element.attr(attr);
      if (!siteGraph.getStaticAssetsMap().containsKey(value))
        siteGraph.getStaticAssetsMap().put(value, new StaticAsset(value));
      assets.add(siteGraph.getStaticAssetsMap().get(value));
    }
    siteGraph.addStaticAssets(siteGraphNode, assets);
  }
  
  public String getRelativeUrl(String baseUrl, String absoluteUrl) {
    int index = absoluteUrl.indexOf('?');
    String relativeUrl =
        index < 0 ? absoluteUrl.substring(baseUrl.length()) : absoluteUrl.substring(
            baseUrl.length(), index);
    index = relativeUrl.indexOf('#');
    if (index >= 0)
      relativeUrl = relativeUrl.substring(0, index);
    relativeUrl =
        relativeUrl.endsWith("/") ? relativeUrl.substring(0, relativeUrl.length() - 1)
            : relativeUrl;
    
    return relativeUrl;
  }
}
