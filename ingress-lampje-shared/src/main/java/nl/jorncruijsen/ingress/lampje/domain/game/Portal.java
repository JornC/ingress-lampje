package nl.jorncruijsen.ingress.lampje.domain.game;

public class Portal {
  private final String guid;
  private final String name;
  private final String city;
  private final String address;

  private final double lon;
  private final double lat;

  public Portal() {
    this(null);
  }

  public Portal(final String guid) {
    this(guid, null, null, null);
  }

  @Deprecated
  public Portal(final String guid, final String name, final String address) {
    this(guid, name, null, address);
  }

  public Portal(final String guid, final String name, final String city, final String address) {
    this(guid, name, city, address, 0d, 0d);
  }

  public Portal(final String name, final String city, final double lon, final double lat) {
    this(name, null, city, lon, lat);
  }

  public Portal(final String name, final double lon, final double lat) {
    this(name, null, lon, lat);
  }

  public Portal(final String name, final String address, final String city, final double lon, final double lat) {
    this(null, name, address, city, lon, lat);
  }

  public Portal(final String guid, final String name, final String city, final String address, final double lon, final double lat) {
    this.guid = guid;
    this.name = name;
    this.city = city;
    this.address = address;
    this.lon = lon;
    this.lat = lat;
  }

  public String getGuid() {
    return guid;
  }

  public String getName() {
    return name;
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
    return "Portal [name=" + name + ", city=" + city + ", address=" + address + ", lon=" + lon + ", lat=" + lat + "]";
  }
}
