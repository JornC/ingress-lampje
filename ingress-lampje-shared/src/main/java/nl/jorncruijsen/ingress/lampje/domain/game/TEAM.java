package nl.jorncruijsen.ingress.lampje.domain.game;

public enum TEAM {
  RESISTANCE("Resistance"), ENLIGHTENED("Enlightened");

  private String name;

  private TEAM(final String name) {
    this.name = name;
  }

  public String getFriendlyName() {
    return name;
  }
}
