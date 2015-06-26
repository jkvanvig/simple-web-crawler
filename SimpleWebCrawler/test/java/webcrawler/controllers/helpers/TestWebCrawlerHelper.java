package webcrawler.controllers.helpers;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;

import webcrawler.models.InMemorySiteGraph;
import webcrawler.models.SiteGraph;
import webcrawler.models.SiteGraphNode;
import webcrawler.stubs.StubElement;
import webcrawler.stubs.StubResponse;

public class TestWebCrawlerHelper {
  
  public static SiteGraph crawlDomainForSiteGraph(String baseUrl, long maxSize) throws Exception {
    Document mockBaseDocument = mockBaseDocument(baseUrl);;
    Document mockSub1Document = mockSub1Document(baseUrl);
    Document mockSub2Document = mockSub2Document(baseUrl);
    Document mockThird1Document = mockThird1Document(baseUrl, false);
    
    Response stubBaseResponse = StubResponse.basicSuccess(baseUrl);
    Response stubSub1Response = StubResponse.basicSuccess(baseUrl + "sub1");
    Response stubSub2Response = StubResponse.basicSuccess(baseUrl + "sub2");
    Response stubThird1Response = StubResponse.basicSuccess(baseUrl + "sub3/secondLevel/third1");
    Response stubInvalidResponse = StubResponse.basicFailure(baseUrl + "invalid");
    
    JsoupHelper mockJsoupHelper =
        mockJsoupHelper(baseUrl, stubBaseResponse, mockBaseDocument, stubSub1Response,
            mockSub1Document, stubSub2Response, mockSub2Document, stubThird1Response,
            mockThird1Document, stubInvalidResponse, false);
    
    WebCrawlerHelper webCrawlerHelper = new WebCrawlerHelper();
    webCrawlerHelper.jsoupHelper = mockJsoupHelper;
    webCrawlerHelper.siteGraphHelper = new SiteGraphHelper();
    
    SiteGraph siteGraph = new InMemorySiteGraph(baseUrl);
    
    webCrawlerHelper.crawlDomain(siteGraph, baseUrl, maxSize);
    
    if (maxSize >= 4)
      EasyMock.verify(mockJsoupHelper, mockBaseDocument, mockSub1Document, mockSub2Document,
          mockThird1Document);
    
    return siteGraph;
  }
  
  public static SiteGraph crawlDomainForSiteGraphWithExceptions(String baseUrl, long maxSize)
      throws Exception {
    Document mockBaseDocument = mockBaseDocument(baseUrl);
    Document mockSub2Document = mockSub2Document(baseUrl);
    Document mockThird1Document = mockThird1Document(baseUrl, true);
    
    Response stubBaseResponse = StubResponse.basicSuccess(baseUrl);
    Response stubSub2Response = StubResponse.basicSuccess(baseUrl + "sub2");
    Response stubThird1Response = StubResponse.basicSuccess(baseUrl + "sub3/secondLevel/third1");
    
    JsoupHelper mockJsoupHelper =
        mockJsoupHelper(baseUrl, stubBaseResponse, mockBaseDocument, null, null, stubSub2Response,
            mockSub2Document, stubThird1Response, mockThird1Document, null, true);
    
    WebCrawlerHelper webCrawlerHelper = new WebCrawlerHelper();
    webCrawlerHelper.jsoupHelper = mockJsoupHelper;
    webCrawlerHelper.siteGraphHelper = new SiteGraphHelper();
    
    SiteGraph siteGraph = new InMemorySiteGraph(baseUrl);
    
    webCrawlerHelper.crawlDomain(siteGraph, baseUrl, maxSize);
    
    if (maxSize >= 4)
      EasyMock.verify(mockJsoupHelper, mockBaseDocument, mockSub2Document, mockThird1Document);
    
    return siteGraph;
  }
  
  private static JsoupHelper mockJsoupHelper(String baseUrl, Response stubBaseResponse,
      Document mockBaseDocument, Response stubSub1Response, Document mockSub1Document,
      Response stubSub2Response, Document mockSub2Document, Response stubThird1Response,
      Document mockThird1Document, Response stubInvalidResponse, boolean invalid)
      throws IOException {
    JsoupHelper mockJsoupHelper = EasyMock.createMock(JsoupHelper.class);
    EasyMock.expect(mockJsoupHelper.connect(baseUrl)).andReturn(stubBaseResponse);
    EasyMock.expect(mockJsoupHelper.parseResponse(stubBaseResponse)).andReturn(mockBaseDocument);
    if (invalid) {
      EasyMock.expect(mockJsoupHelper.connect(baseUrl + "sub1"))
          .andThrow(new UnsupportedMimeTypeException("", "", "")).anyTimes();
      EasyMock.expect(mockJsoupHelper.connect(baseUrl + "invalid")).andThrow(
          new HttpStatusException("", 404, ""));
      EasyMock.expect(mockJsoupHelper.connect(baseUrl + "invalid2")).andThrow(
          new SocketTimeoutException());
      EasyMock.expect(mockJsoupHelper.connect(baseUrl + "invalid3")).andThrow(new IOException());
    } else {
      EasyMock.expect(mockJsoupHelper.connect(baseUrl + "sub1")).andReturn(stubSub1Response);
      EasyMock.expect(mockJsoupHelper.parseResponse(stubSub1Response)).andReturn(mockSub1Document);
      EasyMock.expect(mockJsoupHelper.connect(baseUrl + "invalid")).andReturn(stubInvalidResponse);
    }
    EasyMock.expect(mockJsoupHelper.connect(baseUrl + "sub2")).andReturn(stubSub2Response);
    EasyMock.expect(mockJsoupHelper.parseResponse(stubSub2Response)).andReturn(mockSub2Document);
    EasyMock.expect(mockJsoupHelper.connect(baseUrl + "sub3/secondLevel/third1")).andReturn(
        stubThird1Response);
    EasyMock.expect(mockJsoupHelper.parseResponse(stubThird1Response))
        .andReturn(mockThird1Document);
    EasyMock.replay(mockJsoupHelper);
    return mockJsoupHelper;
  }
  
  private static Document mockThird1Document(String baseUrl, boolean invalid) {
    // First third-level-url mock document and stub response
    Document mockThird1Document = EasyMock.createMock(Document.class);
    EasyMock.expect(mockThird1Document.select("script[src]")).andReturn(
        new Elements(StubElement.stubScript(baseUrl + "script1.js"), StubElement
            .stubScript(baseUrl + "script2.js")));
    EasyMock.expect(mockThird1Document.select("img[src]")).andReturn(
        new Elements(StubElement.stubScript(baseUrl + "img1.jpg"), StubElement.stubScript(baseUrl +
            "/img2.jpg")));
    EasyMock.expect(mockThird1Document.select("source[src]")).andReturn(new Elements());
    EasyMock.expect(mockThird1Document.select("link[href][rel=stylesheet]")).andReturn(
        new Elements(StubElement.stubLink(baseUrl + "link1.css"), StubElement.stubLink(baseUrl +
            "/link2.css")));
    EasyMock.expect(mockThird1Document.select("link[href][rel=icon]")).andReturn(
        new Elements(StubElement.stubLink(baseUrl + "icon1.jpg")));
    List<Element> elements = new ArrayList<>();
    elements.add(StubElement.stubA(baseUrl));
    elements.add(StubElement.stubA(baseUrl + "invalid"));
    if (invalid) {
      elements.add(StubElement.stubA(baseUrl + "invalid2"));
      elements.add(StubElement.stubA(baseUrl + "invalid3"));
    }
    EasyMock.expect(mockThird1Document.select("a[href]")).andReturn(new Elements(elements));
    EasyMock.replay(mockThird1Document);
    return mockThird1Document;
  }
  
  private static Document mockSub1Document(String baseUrl) {
    // First sub-url mock document and stub response
    Document mockSub1Document = EasyMock.createMock(Document.class);
    EasyMock.expect(mockSub1Document.select("script[src]")).andReturn(
        new Elements(StubElement.stubScript(baseUrl + "script1.js"), StubElement
            .stubScript(baseUrl + "sub1/script1.js")));
    EasyMock.expect(mockSub1Document.select("img[src]")).andReturn(
        new Elements(StubElement.stubScript(baseUrl + "img2.jpg"), StubElement.stubScript(baseUrl +
            "/img4.jpg")));
    EasyMock.expect(mockSub1Document.select("source[src]")).andReturn(new Elements());
    EasyMock.expect(mockSub1Document.select("link[href][rel=stylesheet]")).andReturn(
        new Elements(StubElement.stubLink(baseUrl + "link1.css"), StubElement.stubLink(baseUrl +
            "/sub1/link2.css")));
    EasyMock.expect(mockSub1Document.select("link[href][rel=icon]")).andReturn(
        new Elements(StubElement.stubLink(baseUrl + "icon1.jpg")));
    EasyMock.expect(mockSub1Document.select("a[href]")).andReturn(
        new Elements(StubElement.stubA(baseUrl + "sub1"), StubElement.stubA(baseUrl + "sub2"),
            StubElement.stubA(baseUrl)));
    EasyMock.replay(mockSub1Document);
    return mockSub1Document;
  }
  
  private static Document mockSub2Document(String baseUrl) {
    // Second sub-url mock document and stub response
    Document mockSub2Document = EasyMock.createMock(Document.class);
    EasyMock.expect(mockSub2Document.select("script[src]")).andReturn(
        new Elements(StubElement.stubScript(baseUrl + "script1.js"), StubElement
            .stubScript(baseUrl + "script2.js")));
    EasyMock.expect(mockSub2Document.select("img[src]")).andReturn(
        new Elements(StubElement.stubScript(baseUrl + "img1.jpg")));
    EasyMock.expect(mockSub2Document.select("source[src]")).andReturn(
        new Elements(StubElement.stubSource(baseUrl + "/source1.mp4")));
    EasyMock.expect(mockSub2Document.select("link[href][rel=stylesheet]")).andReturn(
        new Elements(StubElement.stubLink(baseUrl + "link3.css"), StubElement.stubLink(baseUrl +
            "/link4.css")));
    EasyMock.expect(mockSub2Document.select("link[href][rel=icon]")).andReturn(new Elements());
    EasyMock.expect(mockSub2Document.select("a[href]")).andReturn(
        new Elements(StubElement.stubA(baseUrl), StubElement.stubA(baseUrl + "sub1"), StubElement
            .stubA(baseUrl + "sub3/secondLevel/third1")));
    EasyMock.replay(mockSub2Document);
    return mockSub2Document;
  }
  
  private static Document mockBaseDocument(String baseUrl) {
    // Base url mock document and stub response
    Document mockBaseDocument = EasyMock.createMock(Document.class);
    EasyMock.expect(mockBaseDocument.select("script[src]")).andReturn(
        new Elements(StubElement.stubScript(baseUrl + "script1.js"), StubElement
            .stubScript(baseUrl + "script2.js")));
    EasyMock.expect(mockBaseDocument.select("img[src]")).andReturn(
        new Elements(StubElement.stubScript(baseUrl + "img1.jpg")));
    EasyMock.expect(mockBaseDocument.select("source[src]")).andReturn(new Elements());
    EasyMock.expect(mockBaseDocument.select("link[href][rel=stylesheet]")).andReturn(
        new Elements(StubElement.stubLink(baseUrl + "link1.css"), StubElement.stubLink(baseUrl +
            "/link2.css")));
    EasyMock.expect(mockBaseDocument.select("link[href][rel=icon]")).andReturn(
        new Elements(StubElement.stubLink(baseUrl + "icon5.jpg")));
    EasyMock.expect(mockBaseDocument.select("a[href]")).andReturn(
        new Elements(StubElement.stubA(baseUrl + "sub1"), StubElement.stubA(baseUrl + "sub2"),
            StubElement.stubA(baseUrl + "sub3/secondLevel/third1"), StubElement.stubA(baseUrl +
                "sub3/secondLevel/third1/blah.zip")));
    EasyMock.replay(mockBaseDocument);
    return mockBaseDocument;
  }
  
  @Test
  public void testFindBaseUrl() throws IOException {
    String domain = "dummyurl.com";
    String url = "http://dummyurl.com";
    String baseUrl = "https://dummyurl.com";
    
    Response stubResponse = StubResponse.basicSuccess(baseUrl);
    
    JsoupHelper mockJsoupHelper = EasyMock.createMock(JsoupHelper.class);
    EasyMock.expect(mockJsoupHelper.connect(url)).andReturn(stubResponse);
    EasyMock.replay(mockJsoupHelper);
    
    WebCrawlerHelper webCrawlerHelper = new WebCrawlerHelper();
    webCrawlerHelper.jsoupHelper = mockJsoupHelper;
    
    String foundBaseUrl = webCrawlerHelper.findBaseUrl(domain);
    
    Assert.assertEquals(baseUrl, foundBaseUrl);
    
    EasyMock.verify(mockJsoupHelper);
  }
  
  @Test
  public void testCrawlDomain() throws Exception {
    String baseUrl = "https://dummyurl.com/";
    
    SiteGraph siteGraph = crawlDomainForSiteGraph(baseUrl, 5);
    
    SiteGraphNode baseNode = siteGraph.findSiteGraphNode("");
    SiteGraphNode sub1Node = siteGraph.findSiteGraphNode("sub1");
    SiteGraphNode sub2Node = siteGraph.findSiteGraphNode("sub2");
    SiteGraphNode sub3Node = siteGraph.findSiteGraphNode("sub3");
    SiteGraphNode secondLevelNode = siteGraph.findSiteGraphNode("sub3/secondLevel");
    SiteGraphNode third1Node = siteGraph.findSiteGraphNode("sub3/secondLevel/third1");
    SiteGraphNode invalidNode = siteGraph.findSiteGraphNode("invalid");
    
    // Graph-level properties - make sure we have 7 nodes, but only 4 are valid, parsed pages. 2 are
    // only url parts, not unique pages, and one returned a 404.
    Assert.assertEquals(4, siteGraph.size());
    Assert.assertEquals(6, siteGraph.totalSize());
    Assert.assertTrue(siteGraph.contains(""));
    Assert.assertTrue(siteGraph.contains("sub1"));
    Assert.assertTrue(siteGraph.contains("sub2"));
    Assert.assertFalse(siteGraph.contains("sub3"));
    Assert.assertFalse(siteGraph.contains("sub3/secondLevel"));
    Assert.assertTrue(siteGraph.contains("sub3/secondLevel/third1"));
    Assert.assertFalse(siteGraph.contains("invalid"));
    
    // Make sure we found all of those nodes
    Assert.assertNotNull(baseNode);
    Assert.assertNotNull(sub1Node);
    Assert.assertNotNull(sub2Node);
    Assert.assertNotNull(sub3Node);
    Assert.assertNotNull(secondLevelNode);
    Assert.assertNotNull(third1Node);
    Assert.assertNull(invalidNode);
    
    // Base node
    Assert.assertEquals(3, baseNode.getLinks().size());
    Assert.assertTrue(baseNode.getLinks().contains(sub1Node));
    Assert.assertTrue(baseNode.getLinks().contains(sub2Node));
    Assert.assertTrue(baseNode.getLinks().contains(third1Node));
    Assert.assertEquals(6, baseNode.getStaticAssets().size());
    Assert.assertEquals(3, baseNode.getSubNodes().size());
    Assert.assertTrue(baseNode.getSubNodes().containsKey("sub1"));
    Assert.assertTrue(baseNode.getSubNodes().containsKey("sub2"));
    Assert.assertTrue(baseNode.getSubNodes().containsKey("sub3"));
    Assert.assertFalse(baseNode.getSubNodes().containsKey("invalid"));
    Assert.assertEquals(sub1Node, baseNode.getSubNodes().get("sub1"));
    Assert.assertEquals(sub2Node, baseNode.getSubNodes().get("sub2"));
    Assert.assertEquals(sub3Node, baseNode.getSubNodes().get("sub3"));
    Assert.assertEquals(invalidNode, baseNode.getSubNodes().get("invalid"));
    
    
    // Sub1 node links
    Assert.assertEquals(3, sub1Node.getLinks().size());
    Assert.assertTrue(sub1Node.getLinks().contains(baseNode));
    Assert.assertTrue(sub1Node.getLinks().contains(sub1Node));
    Assert.assertTrue(sub1Node.getLinks().contains(sub2Node));
    Assert.assertEquals(7, sub1Node.getStaticAssets().size());
    Assert.assertEquals(0, sub1Node.getSubNodes().size());
    
    // Sub2 node links
    Assert.assertEquals(3, sub2Node.getLinks().size());
    Assert.assertTrue(sub2Node.getLinks().contains(baseNode));
    Assert.assertTrue(sub2Node.getLinks().contains(sub1Node));
    Assert.assertTrue(sub2Node.getLinks().contains(third1Node));
    Assert.assertEquals(6, sub2Node.getStaticAssets().size());
    Assert.assertEquals(0, sub2Node.getSubNodes().size());
    
    // Sub3 node links
    Assert.assertEquals(0, sub3Node.getLinks().size());
    Assert.assertEquals(0, sub3Node.getStaticAssets().size());
    Assert.assertEquals(1, sub3Node.getSubNodes().size());
    Assert.assertEquals(secondLevelNode, sub3Node.getSubNodes().get("secondLevel"));
    
    // SecondLevel node links
    Assert.assertEquals(0, secondLevelNode.getLinks().size());
    Assert.assertEquals(0, secondLevelNode.getStaticAssets().size());
    Assert.assertEquals(1, secondLevelNode.getSubNodes().size());
    Assert.assertEquals(third1Node, secondLevelNode.getSubNodes().get("third1"));
    
    // Third1 node links
    Assert.assertEquals(1, third1Node.getLinks().size());
    Assert.assertTrue(third1Node.getLinks().contains(baseNode));
    Assert.assertEquals(7, third1Node.getStaticAssets().size());
    Assert.assertEquals(0, third1Node.getSubNodes().size());
  }
  
  @Test
  public void testCrawlDomainMax2() throws Exception {
    String baseUrl = "https://dummyurl.com/";
    
    SiteGraph siteGraph = crawlDomainForSiteGraph(baseUrl, 2);
    
    Assert.assertNotNull(siteGraph.findSiteGraphNode(""));
    
    // Graph-level properties - make sure we have 7 nodes, but only 4 are valid, parsed pages. 2 are
    // only url parts, not unique pages, and one returned a 404.
    Assert.assertEquals(2, siteGraph.size());
  }
  
  @Test
  public void testCrawlDomainWithExceptions() throws Exception {
    String baseUrl = "https://dummyurl.com/";
    
    SiteGraph siteGraph = crawlDomainForSiteGraphWithExceptions(baseUrl, 5);
    
    Assert.assertNotNull(siteGraph.findSiteGraphNode(""));
    
    // Graph-level properties - make sure we have 7 nodes, but only 4 are valid, parsed pages. 2 are
    // only url parts, not unique pages, and one returned a 404.
    Assert.assertEquals(3, siteGraph.size());
  }
  
}
