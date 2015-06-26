package webcrawler.models;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mapdb.DBMaker;

import webcrawler.models.persistent.FileStoreSiteGraph;

public class TestSiteGraph {
  
  @BeforeClass
  @AfterClass
  public static void cleanupData() {
    File dataDir = new File("data/").getAbsoluteFile();
    for (File file : dataDir.listFiles())
      if (!".gitignore".equals(file.getName()))
        file.delete();
  }
  
  @Test
  public void testInMemorySiteGraph() {
    testSiteGraph(new InMemorySiteGraph("http://dummyUrl.com/"));
  }
  
  @Test
  public void testFileStoreSiteGraph() throws IOException {
    
    String baseUrl = "http://dummyUrl.com/";
    File storageFile = new File("data/" + baseUrl.hashCode() + ".mapdb").getAbsoluteFile();
    storageFile.createNewFile();
    
    testSiteGraph(new FileStoreSiteGraph(baseUrl, DBMaker.newFileDB(storageFile).make()));
  }
  
  protected void testSiteGraph(SiteGraph siteGraph) {
    siteGraph.addSiteGraphNode("");
    siteGraph.addParsedSiteGraphNode("");
    siteGraph.addSiteGraphNode("one");
    siteGraph.addParsedSiteGraphNode("two/three");
    siteGraph.addParsedSiteGraphNode("one/two/three/four/five");
    siteGraph.addParsedSiteGraphNode("one");
    
    SiteGraphNode one = siteGraph.findSiteGraphNode("one");
    SiteGraphNode three = siteGraph.findSiteGraphNode("two/three");
    SiteGraphNode five = siteGraph.findSiteGraphNode("one/two/three/four/five");
    
    siteGraph.addLink(one, three);
    siteGraph.addLink(three, five);
    siteGraph.addLink(five, one);
    siteGraph.addLink(five, three);
    
    siteGraph.addInvalidUrl("invalid");
    
    StaticAsset common = new StaticAsset("common");
    StaticAsset uncommon = new StaticAsset("uncommon");
    
    siteGraph.addStaticAssets(one, Arrays.asList(common, uncommon));
    three.getStaticAssets().add(common);
    five.getStaticAssets().add(common);
    
    Collection<DirectedEdge> linksDirectedEdges = siteGraph.linksDirectedEdges();
    Collection<DirectedEdge> staticAssetsDirectedEdges = siteGraph.staticAssetsDirectedEdges();
    
    String toString = siteGraph.toString();
    String toHtml = siteGraph.toHtml();
    
    Assert.assertEquals(4, siteGraph.size());
    Assert.assertEquals(8, siteGraph.totalSize());
    Assert.assertEquals(1, siteGraph.getInvalidUrls().size());
    Assert.assertTrue(siteGraph.getInvalidUrls().contains("invalid"));
    Assert.assertTrue(siteGraph.contains("one"));
    Assert.assertTrue(siteGraph.contains("two/three"));
    Assert.assertTrue(siteGraph.contains("one/two/three/four/five"));
    Assert.assertTrue(siteGraph.contains(""));
    Assert.assertFalse(siteGraph.contains("two"));
    Assert.assertFalse(siteGraph.contains("one/two"));
    Assert.assertFalse(siteGraph.contains("one/two/three"));
    Assert.assertFalse(siteGraph.contains("one/two/three/four"));
    
    Assert.assertTrue(toString.contains("http://dummyUrl.com/\n"));
    Assert.assertTrue(toString.contains("http://dummyUrl.com/one\n"));
    Assert.assertTrue(toString.contains("(http://dummyUrl.com/two)"));
    Assert.assertTrue(toString.contains("http://dummyUrl.com/two/three\n"));
    Assert.assertTrue(toString.contains("(http://dummyUrl.com/one/two)"));
    Assert.assertTrue(toString.contains("(http://dummyUrl.com/one/two/three)"));
    Assert.assertTrue(toString.contains("(http://dummyUrl.com/one/two/three/four)"));
    Assert.assertTrue(toString.contains("http://dummyUrl.com/one/two/three/four/five\n"));
    
    Assert.assertTrue(toHtml.contains("<li>http://dummyUrl.com/</li>"));
    Assert.assertTrue(toHtml.contains("<li>http://dummyUrl.com/one</li>"));
    Assert.assertTrue(toHtml.contains("<li>(http://dummyUrl.com/two)</li>"));
    Assert.assertTrue(toHtml.contains("<li>http://dummyUrl.com/two/three</li>"));
    Assert.assertTrue(toHtml.contains("<li>(http://dummyUrl.com/one/two)</li>"));
    Assert.assertTrue(toHtml.contains("<li>(http://dummyUrl.com/one/two/three)</li>"));
    Assert.assertTrue(toHtml.contains("<li>(http://dummyUrl.com/one/two/three/four)</li>"));
    Assert.assertTrue(toHtml.contains("<li>http://dummyUrl.com/one/two/three/four/five</li>"));
    
    Assert.assertEquals(4, linksDirectedEdges.size());
    Assert.assertTrue(linksDirectedEdges.contains(new DirectedEdge("one", "two/three", false)));
    Assert.assertTrue(linksDirectedEdges.contains(new DirectedEdge("two/three",
        "one/two/three/four/five", false)));
    Assert.assertTrue(linksDirectedEdges.contains(new DirectedEdge("one/two/three/four/five",
        "two/three", false)));
    Assert.assertTrue(linksDirectedEdges.contains(new DirectedEdge("one/two/three/four/five",
        "one", false)));
    Assert.assertEquals(4, staticAssetsDirectedEdges.size());
    Assert.assertTrue(staticAssetsDirectedEdges.contains(new DirectedEdge(
        "one/two/three/four/five", "common", true)));
    Assert.assertTrue(staticAssetsDirectedEdges.contains(new DirectedEdge("two/three", "common",
        true)));
    Assert.assertTrue(staticAssetsDirectedEdges.contains(new DirectedEdge("one", "common", true)));
    Assert
        .assertTrue(staticAssetsDirectedEdges.contains(new DirectedEdge("one", "uncommon", true)));
  }
}
