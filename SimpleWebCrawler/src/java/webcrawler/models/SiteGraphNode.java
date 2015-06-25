package webcrawler.models;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;

public class SiteGraphNode implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private String value;
  private Boolean parsedPage;
  private Set<SiteGraphNode> links = new LinkedHashSet<>();
  private Set<StaticAsset> staticAssets = new LinkedHashSet<>();
  private Map<String, SiteGraphNode> subNodes = new LinkedHashMap<>();
  private SiteGraphNode parent;
  
  public SiteGraphNode(String value, SiteGraphNode parent) {
    this.value = value;
    this.parsedPage = false;
    this.parent = parent;
  }
  
  public String getValue() {
    return value;
  }
  
  public void setValue(String value) {
    this.value = value;
  }
  
  public boolean isParsedPage() {
    return parsedPage;
  }
  
  public void setParsedPage(boolean parsedPage) {
    this.parsedPage = parsedPage;
  }
  
  public Set<SiteGraphNode> getLinks() {
    return links;
  }
  
  public void setLinks(Set<SiteGraphNode> links) {
    this.links = links;
  }
  
  public Set<StaticAsset> getStaticAssets() {
    return staticAssets;
  }
  
  public void setStaticAssets(Set<StaticAsset> staticAssets) {
    this.staticAssets = staticAssets;
  }
  
  public Map<String, SiteGraphNode> getSubNodes() {
    return subNodes;
  }
  
  public void setSubNodes(Map<String, SiteGraphNode> subNodes) {
    this.subNodes = subNodes;
  }
  
  public SiteGraphNode getParent() {
    return parent;
  }
  
  public void setParent(SiteGraphNode parent) {
    this.parent = parent;
  }
  
  public String getHref() {
    String href = getHref(this);
    return StringUtils.isEmpty(href) ? "/" : href;
  }
  
  private String getHref(SiteGraphNode node) {
    if (node == null || node.parent == null)
      return "";
    String parentHref = getHref(node.parent);
    return (StringUtils.isEmpty(parentHref) ? "" : parentHref + "/") + node.value;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((parent == null) ? 0 : parent.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SiteGraphNode other = (SiteGraphNode) obj;
    if (parent == null) {
      if (other.parent != null)
        return false;
    } else if (!parent.equals(other.parent))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }
}
