package webcrawler.controllers;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.DBMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import webcrawler.controllers.helpers.WebCrawlerHelper;
import webcrawler.models.DirectedEdge;
import webcrawler.models.InMemorySiteGraph;
import webcrawler.models.SiteGraph;
import webcrawler.models.persistent.FileStoreSiteGraph;

@Controller
@RequestMapping
public class WebCrawlerController {
  
  private final Logger logger = LogManager.getLogger(this.getClass());
  
  @Autowired
  protected WebCrawlerHelper webCrawlerHelper;
  
  @RequestMapping(value = "/")
  public ModelAndView getCrawler() {
    return new ModelAndView("index");
  }
  
  @ResponseBody
  @RequestMapping(value = "/crawl/in-memory", method = RequestMethod.POST)
  public Object crawlInMemory(@RequestBody Map<String, String> data) throws Exception {
    logger.entry(data);
    String baseUrl = webCrawlerHelper.findBaseUrl(data.get("baseUrl"));
    logger.info("crawling {} in memory", baseUrl);
    return logger.exit(crawl(data, baseUrl, new InMemorySiteGraph(baseUrl)));
  }
  
  @ResponseBody
  @RequestMapping(value = "/crawl/persistent", method = RequestMethod.POST)
  public Object crawlPersistent(@RequestBody Map<String, String> data) throws Exception {
    logger.entry(data);
    webCrawlerHelper.clearData();
    String baseUrl = webCrawlerHelper.findBaseUrl(data.get("baseUrl"));
    File storageFile = new File("data/" + baseUrl.hashCode() + ".mapdb").getAbsoluteFile();
    if (storageFile.exists())
      storageFile.delete();
    storageFile.createNewFile();
    logger.info("crawling {} with file store", baseUrl);
    return logger.exit(crawl(data, baseUrl,
        new FileStoreSiteGraph(baseUrl, DBMaker.newFileDB(storageFile).make())));
  }
  
  private Object crawl(Map<String, String> data, String baseUrl, SiteGraph siteGraph)
      throws Exception {
    long maxSize =
        StringUtils.isEmpty(data.get("maxSites")) ? Long.MAX_VALUE : Long.parseLong(data
            .get("maxSites"));
    long millis = System.currentTimeMillis();
    webCrawlerHelper.crawlDomain(siteGraph, baseUrl, maxSize);
    millis = System.currentTimeMillis() - millis;
    logger.info("crawled in {} ms", millis);
    
    if (siteGraph.size() <= 1000) {
      Map<String, Collection<DirectedEdge>> edgesMap =
          new HashMap<String, Collection<DirectedEdge>>();
      edgesMap.put("links", siteGraph.linksDirectedEdges());
      edgesMap.put("staticAssets", siteGraph.staticAssetsDirectedEdges());
      logger.info("Returning {} nodes, {} links edges, {} static asset edges.", siteGraph.size(),
          edgesMap.get("links").size(), edgesMap.get("staticAssets").size());;
      return edgesMap;
    }
    logger.info("Graph is too large for the JS library.  Returning a simple list instead.");
    return siteGraph.toHtml();
  }
}
