package nl.jorncruijsen.ingress.lampje.domain.game;

import java.util.ArrayList;

public class PortalReportInfo extends ArrayList<NameCountInfo> {
  private static final long serialVersionUID = -5883768447649831712L;

  private String portalName;
  private String portalCity;
  private String portalAddress;

  public String getPortalName() {
    return portalName;
  }

  public String getPortalCity() {
    return portalCity;
  }

  public String getPortalAddress() {
    return portalAddress;
  }

  public void setPortalName(String portalName) {
    this.portalName = portalName;
  }

  public void setPortalCity(String portalCity) {
    this.portalCity = portalCity;
  }

  public void setPortalAddress(String portalAddress) {
    this.portalAddress = portalAddress;
  }
}
