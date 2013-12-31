package nl.jorncruijsen.ingress.lampje.domain.game;

public class NameCountInfo {
  private final String attackerName;
  private final int count;

  public NameCountInfo(final String attackerName, final int count) {
    this.attackerName = attackerName;
    this.count = count;
  }

  public String getAttackerName() {
    return attackerName;
  }

  public int getCount() {
    return count;
  }

}
