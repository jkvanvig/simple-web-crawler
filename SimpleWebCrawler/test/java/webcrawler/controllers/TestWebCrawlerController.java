package webcrawler.controllers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import webcrawler.controllers.helpers.WebCrawlerHelper;
import webcrawler.models.InMemorySiteGraph;

public class TestWebCrawlerController {
  
  @Test
  public void testCrawlInMemorySmall() throws Exception {
    WebCrawlerHelper helper = EasyMock.createMock(WebCrawlerHelper.class);
    EasyMock.expect(helper.findBaseUrl("blah.com")).andReturn("http://blah.com");
    helper.crawlDomain(EasyMock.anyObject(InMemorySiteGraph.class), EasyMock.anyString(),
        EasyMock.anyLong());
    EasyMock.replay(helper);
    WebCrawlerController controller = new WebCrawlerController();
    controller.webCrawlerHelper = helper;
    Map<String, String> data = new HashMap<>();
    data.put("baseUrl", "blah.com");
    data.put("maxSites", "5");
    Assert.assertTrue(controller.crawlInMemory(data) instanceof Map);
  }
  
  @Test
  public void testCrawlInMemoryLarge() throws Exception {
    WebCrawlerHelper helper = EasyMock.createMock(WebCrawlerHelper.class);
    EasyMock.expect(helper.findBaseUrl("blah.com")).andReturn("http://blah.com");
    helper.crawlDomain(EasyMock.anyObject(InMemorySiteGraph.class), EasyMock.anyString(),
        EasyMock.anyLong());
    EasyMock.replay(helper);
    WebCrawlerController controller = new WebCrawlerController();
    controller.webCrawlerHelper = helper;
    Map<String, String> data = new HashMap<>();
    data.put("baseUrl", "blah.com");
    data.put("maxSites", "500");
    Assert.assertTrue(controller.crawlInMemory(data) instanceof Map);
  }
  
  @Test
  public void testCrawlPersistent() throws Exception {
    String baseUrl = "http://blah.com";
    
    WebCrawlerHelper helper = EasyMock.createMock(WebCrawlerHelper.class);
    EasyMock.expect(helper.findBaseUrl("blah.com")).andReturn(baseUrl);
    helper.clearData();
    helper.crawlDomain(EasyMock.anyObject(InMemorySiteGraph.class), EasyMock.anyString(),
        EasyMock.anyLong());
    EasyMock.replay(helper);
    
    WebCrawlerController controller = new WebCrawlerController();
    controller.webCrawlerHelper = helper;
    Map<String, String> data = new HashMap<>();
    data.put("baseUrl", "blah.com");
    
    File fileStore = new File("data/" + baseUrl.hashCode() + ".mapdb").getAbsoluteFile();
    fileStore.createNewFile();
    controller.crawlPersistent(data);
    Assert.assertNotNull(fileStore);
    Assert.assertTrue(fileStore.exists());
    fileStore.delete();
  }
}
