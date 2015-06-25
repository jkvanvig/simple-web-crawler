package webcrawler.models;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class SiteGraph {
  protected SiteGraphNode root;
  protected Map<String, StaticAsset> staticAssetsMap;
  protected Set<String> invalidUrls;
  protected long size;
  protected long totalSize;
  
  public SiteGraphNode getRoot() {
    return root;
  }
  
  public void setRoot(SiteGraphNode root) {
    this.root = root;
  }
  
  public Map<String, StaticAsset> getStaticAssetsMap() {
    return staticAssetsMap;
  }
  
  public void setStaticAssetsMap(Map<String, StaticAsset> staticAssetsMap) {
    this.staticAssetsMap = staticAssetsMap;
  }
  
  public Set<String> getInvalidUrls() {
    return invalidUrls;
  }
  
  public void setInvalidUrls(Set<String> invalidUrls) {
    this.invalidUrls = invalidUrls;
  }
  
  public long size() {
    return size;
  }
  
  public long totalSize() {
    return totalSize;
  }
  
  public String getBaseUrl() {
    return root.getValue();
  }
  
  public boolean contains(String relativeUrl) {
    SiteGraphNode node = findSiteGraphNode(relativeUrl);
    return node != null && node.isParsedPage();
  }
  
  public SiteGraphNode findSiteGraphNode(String relativeUrl) {
    SiteGraphNode node = findSiteGraphNode(relativeUrl, root);
    return node;
  }
  
  private SiteGraphNode findSiteGraphNode(String relativeUrl, SiteGraphNode node) {
    if (relativeUrl == null || relativeUrl.equals(""))
      return node;
    if (node == null)
      return null;
    int slash = relativeUrl.indexOf('/');
    if (slash < 0)
      return node.getSubNodes() != null ? node.getSubNodes().get(relativeUrl) : null;
    return findSiteGraphNode(relativeUrl.substring(slash + 1),
        node.getSubNodes().get(relativeUrl.substring(0, slash)));
  }
  
  public SiteGraphNode addSiteGraphNode(String relativeUrl) {
    root = addSiteGraphNode(relativeUrl, root, false);
    return findSiteGraphNode(relativeUrl);
  }
  
  public SiteGraphNode addParsedSiteGraphNode(String relativeUrl) {
    root = addSiteGraphNode(relativeUrl, root, true);
    return findSiteGraphNode(relativeUrl);
  }
  
  protected abstract SiteGraphNode addSiteGraphNode(String relativeUrl, SiteGraphNode node,
      boolean parsed);
  
  public String toHtml() {
    StringBuilder sb = new StringBuilder("<ul>\n");
    toHtml(root, sb, "", "");
    return sb.append("</ul>").toString();
  }
  
  private void toHtml(SiteGraphNode node, StringBuilder sb, String prefix, String indent) {
    if (node.isParsedPage())
      sb.append(indent).append("<li>").append(prefix).append(node.getValue())
          .append("</li>\n<ul>\n");
    else
      sb.append(indent).append("<li>(").append(prefix).append(node.getValue())
          .append(")</li>\n<ul>\n");
    for (SiteGraphNode subNode : node.getSubNodes().values())
      toHtml(subNode, sb, prefix + node.getValue() + (node.getValue().endsWith("/") ? "" : "/"),
          indent + "\t");
    sb.append("</ul");
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    toString(root, sb, "", "");
    return sb.toString();
  }
  
  private void toString(SiteGraphNode node, StringBuilder sb, String prefix, String indent) {
    if (node.isParsedPage())
      sb.append(indent).append(prefix).append(node.getValue()).append("\n");
    else
      sb.append(indent).append("(").append(prefix).append(node.getValue()).append(")\n");
    for (SiteGraphNode subNode : node.getSubNodes().values())
      toString(subNode, sb, prefix + node.getValue() + (node.getValue().endsWith("/") ? "" : "/"),
          indent + "\t");
  }
  
  public Collection<DirectedEdge> linksDirectedEdges() {
    Set<DirectedEdge> edges = new LinkedHashSet<DirectedEdge>();
    linksDirectedEdges(this.root, edges);
    return edges;
  }
  
  private void linksDirectedEdges(SiteGraphNode node, Set<DirectedEdge> edges) {
    if (node == null)
      return;
    if (node.isParsedPage()) {
      String href = node.getHref();
      for (SiteGraphNode linked : node.getLinks())
        if (linked.isParsedPage())
          edges.add(new DirectedEdge(href, linked.getHref(), false));
    }
    for (SiteGraphNode subNode : node.getSubNodes().values())
      linksDirectedEdges(subNode, edges);
  }
  
  public Collection<DirectedEdge> staticAssetsDirectedEdges() {
    Set<DirectedEdge> edges = new LinkedHashSet<DirectedEdge>();
    staticAssetsDirectedEdges(this.root, edges);
    return edges;
  }
  
  private void staticAssetsDirectedEdges(SiteGraphNode node, Set<DirectedEdge> edges) {
    if (node == null)
      return;
    String href = node.getHref();
    for (StaticAsset asset : node.getStaticAssets()) {
      String value = asset.getValue();
      if (value.lastIndexOf('/') > -1)
        value = value.substring(value.lastIndexOf('/') + 1);
      edges.add(new DirectedEdge(href, value, true));
    }
    for (SiteGraphNode subNode : node.getSubNodes().values())
      staticAssetsDirectedEdges(subNode, edges);
  }
  
  public abstract SiteGraphNode buildSiteGraphNode(String value, SiteGraphNode parent);
  
  public abstract SiteGraphNode addLink(SiteGraphNode node, SiteGraphNode link);
  
  public abstract SiteGraphNode addStaticAssets(SiteGraphNode node,
      Collection<StaticAsset> staticAsset);
  
  public abstract void addInvalidUrl(String relativeUrl);
}
