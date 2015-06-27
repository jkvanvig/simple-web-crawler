package webcrawler.models.persistent;

import java.util.Collection;

import org.mapdb.DB;

import webcrawler.models.SiteGraph;
import webcrawler.models.SiteGraphNode;
import webcrawler.models.StaticAsset;

public class FileStoreSiteGraph extends SiteGraph {
  protected DB mapDB;
  
  public FileStoreSiteGraph(String baseUrl, DB mapDB) {
    this.mapDB = mapDB;
    super.root = buildSiteGraphNode(baseUrl, null);
    super.staticAssetsMap = this.mapDB.getHashMap("staticAssets_" + baseUrl.hashCode());
    super.invalidUrls = this.mapDB.getHashSet("invalidUrls_" + baseUrl.hashCode());
    this.mapDB.commit();
  }
  
  @Override
  protected SiteGraphNode addSiteGraphNode(String relativeUrl, SiteGraphNode node, boolean parsed) {
    if (relativeUrl == null || relativeUrl.equals("")) {
      if (parsed && !node.isParsedPage()) {
        node.setParsedPage(true);
        super.size++;
      }
      return node;
    }
    int slash = relativeUrl.indexOf('/');
    if (slash < 0) {
      SiteGraphNode newNode = node.getSubNodes().get(relativeUrl);
      if (parsed && (newNode == null || !newNode.isParsedPage()))
        super.size++;
      if (newNode == null) {
        newNode = buildSiteGraphNode(relativeUrl, node);
        newNode.setParsedPage(parsed);
      } else if (parsed) {
        newNode.setParsedPage(true);
      }
      node.getSubNodes().put(relativeUrl, newNode);
      this.mapDB.commit();
      return node;
    }
    String nextPath = relativeUrl.substring(0, slash);
    if (node.getSubNodes().get(nextPath) == null) {
      node.getSubNodes().put(nextPath, buildSiteGraphNode(nextPath, node));
      this.mapDB.commit();
    }
    addSiteGraphNode(relativeUrl.substring(slash + 1), node.getSubNodes().get(nextPath), parsed);
    return node;
  }
  
  @Override
  protected SiteGraphNode buildSiteGraphNode(String value, SiteGraphNode parent) {
    super.totalSize++;
    SiteGraphNode newNode = new SiteGraphNode(value, parent);
    newNode.setLinks(mapDB.getHashSet("links_" + newNode.hashCode()));
    newNode.setStaticAssets(mapDB.getHashSet("staticAssets_" + newNode.hashCode()));
    newNode.setSubNodes(mapDB.getHashMap("subNodes_" + newNode.hashCode()));
    this.mapDB.commit();
    return newNode;
  }
  
  @Override
  public synchronized SiteGraphNode addLink(SiteGraphNode node, SiteGraphNode link) {
    node.getLinks().add(link);
    this.mapDB.commit();
    return node;
  }
  
  @Override
  public synchronized SiteGraphNode addStaticAssets(SiteGraphNode node,
      Collection<StaticAsset> staticAssets) {
    for (StaticAsset asset : staticAssets)
      node.getStaticAssets().add(asset);
    this.mapDB.commit();
    return node;
  }
  
  @Override
  public synchronized void addInvalidUrl(String relativeUrl) {
    super.invalidUrls.add(relativeUrl);
    this.mapDB.commit();
  }
}
