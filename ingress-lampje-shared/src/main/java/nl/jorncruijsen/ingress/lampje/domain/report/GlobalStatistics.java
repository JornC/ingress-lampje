package nl.jorncruijsen.ingress.lampje.domain.report;

public class GlobalStatistics {
  private final int attackerNum;
  private final int reportNum;
  private final int contributorNum;
  private final int portalNum;
  private final int cityNum;

  public GlobalStatistics(final int attackerNum, final int reportNum, final int contributorNum, final int portalNum, final int cityNum) {
    this.attackerNum = attackerNum;
    this.reportNum = reportNum;
    this.contributorNum = contributorNum;
    this.portalNum = portalNum;
    this.cityNum = cityNum;
  }

  public int getAttackerNum() {
    return attackerNum;
  }

  public int getReportNum() {
    return reportNum;
  }

  public int getContributorNum() {
    return contributorNum;
  }

  public int getPortalNum() {
    return portalNum;
  }

  public int getCityNum() {
    return cityNum;
  }
}
