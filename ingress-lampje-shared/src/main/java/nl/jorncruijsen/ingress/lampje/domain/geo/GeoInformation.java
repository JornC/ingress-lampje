package nl.jorncruijsen.ingress.lampje.domain.geo;

public class GeoInformation {
  private final Bounds bounds;
  private final String full_address;
  private final String city;
  private final String address;

  private final double lon;
  private final double lat;

  public GeoInformation(final Bounds bounds, final String name) {
    this(bounds, name, null, null, -1, -1);
  }

  public GeoInformation(final Bounds bounds, final String full_address, final String city, final String address, final double lon, final double lat) {
    this.bounds = bounds;
    this.full_address = full_address;
    this.city = city;
    this.address = address;
    this.lon = lon;
    this.lat = lat;
  }

  public Bounds getBounds() {
    return bounds;
  }

  public String getFullAddress() {
    return full_address;
  }

  public String getCity() {
    return city;
  }

  public String getAddress() {
    return address;
  }

  public double getLon() {
    return lon;
  }

  public double getLat() {
    return lat;
  }

  @Override
  public String toString() {
    return "GeoInformation [bounds=" + bounds + ", address=" + full_address + "]";
  }
}
