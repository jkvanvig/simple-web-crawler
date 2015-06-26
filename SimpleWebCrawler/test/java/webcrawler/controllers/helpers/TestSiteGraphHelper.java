package webcrawler.controllers.helpers;

import org.junit.Assert;
import org.junit.Test;

import webcrawler.models.SiteGraph;
import webcrawler.stubs.StubResponse;

public class TestSiteGraphHelper {
  
  @Test
  public void testValidate() throws Exception {
    SiteGraphHelper helper = new SiteGraphHelper();
    
    SiteGraph siteGraph = TestWebCrawlerHelper.crawlDomainForSiteGraph("http://dummyurl.com/", 5);
    Assert.assertTrue(helper.validate(siteGraph, "newurl", 5));
    Assert.assertFalse(helper.validate(siteGraph, "sub1", 5));
    Assert.assertFalse(helper.validate(siteGraph, "newurl", 4));
  }
  
  @Test
  public void testRevalidate() throws Exception {
    SiteGraphHelper helper = new SiteGraphHelper();
    
    SiteGraph siteGraph = TestWebCrawlerHelper.crawlDomainForSiteGraph("http://dummyurl.com/", 5);
    Assert.assertTrue(helper.revalidate(siteGraph, "http://dummyurl.com/newurl", "newurl",
        StubResponse.basicSuccess("http://dummyurl.com/newurl"), 5));
    Assert.assertFalse(helper.revalidate(siteGraph, "http://subdomain.dummyurl.com/newurl",
        "newurl", StubResponse.basicSuccess("http://subdomain.dummyurl.com/newurl"), 5));
    Assert.assertFalse(helper.revalidate(siteGraph, "http://dummyurl.com/newurl", "newurl",
        StubResponse.basicFailure("http://dummyurl.com/newurl"), 5));
    Assert.assertFalse(helper.revalidate(siteGraph, "http://dummyurl.com/sub1", "newurl",
        StubResponse.basicSuccess("http://dummyurl.com/sub1"), 5));
    Assert.assertFalse(helper.revalidate(siteGraph, "http://dummyurl.com/newurl", "newurl",
        StubResponse.basicSuccess("http://dummyurl.com/newurl"), 4));
  }
  
  @Test
  public void testGetRelativeUrl() {
    SiteGraphHelper helper = new SiteGraphHelper();
    Assert.assertEquals("rel",
        helper.getRelativeUrl("http://dummyurl.com/", "http://dummyurl.com/rel"));
    Assert.assertEquals("rel",
        helper.getRelativeUrl("http://dummyurl.com/", "http://dummyurl.com/rel?p=1"));
    Assert.assertEquals("rel",
        helper.getRelativeUrl("http://dummyurl.com/", "http://dummyurl.com/rel#anchor"));
    Assert.assertEquals("rel",
        helper.getRelativeUrl("http://dummyurl.com/", "http://dummyurl.com/rel/"));
  }
}
