package webcrawler.models;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class InMemorySiteGraph extends SiteGraph {
  public InMemorySiteGraph(String baseUrl) {
    super.root = buildSiteGraphNode(baseUrl, null);
    super.staticAssetsMap = new LinkedHashMap<>();
    super.invalidUrls = new HashSet<>();
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
      return node;
    }
    String nextPath = relativeUrl.substring(0, slash);
    if (node.getSubNodes().get(nextPath) == null)
      node.getSubNodes().put(nextPath, buildSiteGraphNode(nextPath, node));
    addSiteGraphNode(relativeUrl.substring(slash + 1), node.getSubNodes().get(nextPath), parsed);
    return node;
  }
  
  @Override
  public synchronized SiteGraphNode addLink(SiteGraphNode node, SiteGraphNode link) {
    node.getLinks().add(link);
    return node;
  }
  
  @Override
  public synchronized SiteGraphNode addStaticAssets(SiteGraphNode node,
      Collection<StaticAsset> staticAsset) {
    node.getStaticAssets().addAll(staticAsset);
    return node;
  }
  
  @Override
  public synchronized void addInvalidUrl(String relativeUrl) {
    super.invalidUrls.add(relativeUrl);
  }
  
  @Override
  protected  SiteGraphNode buildSiteGraphNode(String value, SiteGraphNode parent) {
    super.totalSize++;
    SiteGraphNode newNode = new SiteGraphNode(value, parent);
    newNode.setLinks(new LinkedHashSet<>());
    newNode.setStaticAssets(new LinkedHashSet<>());
    newNode.setSubNodes(new LinkedHashMap<>());
    return newNode;
  }
}
