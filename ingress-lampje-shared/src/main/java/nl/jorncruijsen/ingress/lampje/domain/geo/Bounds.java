package nl.jorncruijsen.ingress.lampje.domain.geo;

public class Bounds {
  private final Point topLeft;
  private final Point bottomRight;

  public Bounds(final double minX, final double maxX, final double minY, final double maxY) {
    topLeft = new Point(minX, minY);
    bottomRight = new Point(maxX, maxY);
  }

  public Bounds(final Point nw, final Point se) {
    topLeft = nw;
    bottomRight = se;
  }

  public double getMinX() {
    return getTopLeft().getX();
  }

  public double getMaxX() {
    return getBottomRight().getX();
  }

  public double getMinY() {
    return getTopLeft().getY();
  }

  public double getMaxY() {
    return getBottomRight().getY();
  }

  public Point getBottomRight() {
    return bottomRight;
  }

  public Point getTopLeft() {
    return topLeft;
  }

  @Override
  public String toString() {
    return "Bounds [topLeft=" + topLeft + ", bottomRight=" + bottomRight + "]";
  }
}
