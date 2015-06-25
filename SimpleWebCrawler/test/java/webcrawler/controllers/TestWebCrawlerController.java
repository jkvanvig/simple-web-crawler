package webcrawler.controllers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import webcrawler.helpers.WebCrawlerHelper;
import webcrawler.models.InMemorySiteGraph;

public class TestWebCrawlerController {
  
  @Test
  public void testCrawlInMemorySmall() throws IOException {
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
  public void testCrawlInMemoryLarge() throws IOException {
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
  public void testCrawlPersistent() throws IOException {
    WebCrawlerHelper helper = EasyMock.createMock(WebCrawlerHelper.class);
    String baseUrl = "http://blah.com";
    EasyMock.expect(helper.findBaseUrl("blah.com")).andReturn(baseUrl);
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
