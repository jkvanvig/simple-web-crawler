package webcrawler.models;

public class DirectedEdge {
  private String fromHref;
  private String toHref;
  private boolean staticAsset;
  
  public DirectedEdge(String fromHref, String toHref, boolean staticAsset) {
    this.fromHref = fromHref;
    this.toHref = toHref;
    this.staticAsset = staticAsset;
  }
  
  public String getFromHref() {
    return fromHref;
  }
  
  public void setFromHref(String fromHref) {
    this.fromHref = fromHref;
  }
  
  public String getToHref() {
    return toHref;
  }
  
  public void setToHref(String toHref) {
    this.toHref = toHref;
  }
  
  public boolean isStaticAsset() {
    return staticAsset;
  }
  
  public void setStaticAsset(boolean staticAsset) {
    this.staticAsset = staticAsset;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fromHref == null) ? 0 : fromHref.hashCode());
    result = prime * result + (staticAsset ? 1231 : 1237);
    result = prime * result + ((toHref == null) ? 0 : toHref.hashCode());
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
    DirectedEdge other = (DirectedEdge) obj;
    if (fromHref == null) {
      if (other.fromHref != null)
        return false;
    } else if (!fromHref.equals(other.fromHref))
      return false;
    if (staticAsset != other.staticAsset)
      return false;
    if (toHref == null) {
      if (other.toHref != null)
        return false;
    } else if (!toHref.equals(other.toHref))
      return false;
    return true;
  }
}
