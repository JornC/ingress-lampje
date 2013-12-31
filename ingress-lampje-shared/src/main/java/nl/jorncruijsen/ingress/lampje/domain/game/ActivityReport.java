package nl.jorncruijsen.ingress.lampje.domain.game;

import java.util.Date;

public class ActivityReport {
  private final String playerName;
  private final String portalName;
  private final String portalAddress;
  private final String portalCity;
  private final Date date;

  public ActivityReport(final String name, final String portalName, final String portalAddress, final String portalCity, final Date date) {
    this.playerName = name;
    this.portalName = portalName;
    this.portalAddress = portalAddress;
    this.portalCity = portalCity;
    this.date = date;
  }

  public String getName() {
    return playerName;
  }

  public String getPortalName() {
    return portalName;
  }

  public String getPortalAddress() {
    return portalAddress;
  }

  public Date getDate() {
    return date;
  }

  public String getPortalCity() {
    return portalCity;
  }
}
