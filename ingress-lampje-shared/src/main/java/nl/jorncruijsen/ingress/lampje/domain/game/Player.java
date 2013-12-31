package nl.jorncruijsen.ingress.lampje.domain.game;

import java.util.ArrayList;
import java.util.Date;

public class Player {
  private final String guid;
  private final String name;
  private final TEAM team;

  private final ArrayList<ActivityReport> reports = new ArrayList<ActivityReport>();
  private final ArrayList<ActivityReport> ownedReports = new ArrayList<ActivityReport>();

  private Date lastSeen;
  private int level;
  private int ap;
  private String locationPrimary;
  private String locationSecondary;

  public Player(final String name, final TEAM team, final int level) {
    this.guid = null;
    this.name = name;
    this.team = team;
    this.level = level;
  }

  public Player(final String guid, final String name, final TEAM team, final int level, final Date lastSeen) {
    this.guid = guid;
    this.name = name;
    this.team = team;
    this.level = level;
    this.lastSeen = lastSeen;
  }

  public Player(final String guid, final String name, final TEAM team, final int level, final Date lastSeen, final int ap) {
    this(guid, name, team, level, lastSeen);
    this.ap = ap;
  }

  public Player(final String guid, final String name, final TEAM team, final int level, final String locationPrimary, final String locationSecondary, final Date lastSeen, final int ap) {
    this(guid, name, team, level, lastSeen, ap);

    this.locationPrimary = locationPrimary;
    this.locationSecondary = locationSecondary;
  }

  public Player(final String name, final String locationPrimary, final String locationSecondary) {
    this.name = name;
    this.locationPrimary = locationPrimary;
    this.locationSecondary = locationSecondary;

    // Temp, TODO remove
    this.team = TEAM.ENLIGHTENED;
    this.guid = null;
  }

  public TEAM getTeam() {
    return team;
  }

  public String getName() {
    return name;
  }

  public String getGuid() {
    return guid;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((guid == null) ? 0 : guid.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Player other = (Player) obj;
    if (guid == null) {
      if (other.guid != null)
        return false;
    } else if (!guid.equals(other.guid))
      return false;
    return true;
  }

  public Date getLastSeen() {
    return lastSeen;
  }

  @Override
  public String toString() {
    return "Player [name=" + name + ", team=" + team + ", level=" + level + ", ap=" + ap + ", locationPrimary=" + locationPrimary + ", locationSecondary=" + locationSecondary + "]";
  }

  public int getLevel() {
    return level;
  }

  public void setLastSeen(final Date lastSeen) {
    this.lastSeen = lastSeen;
  }

  public void setLevel(final int level) {
    this.level = level;
  }

  public int getAp() {
    return ap;
  }

  public void setAp(final int ap) {
    this.ap = ap;
  }

  public String getLocationPrimary() {
    return locationPrimary;
  }

  public void setLocationPrimary(final String locationPrimary) {
    this.locationPrimary = locationPrimary;
  }

  public String getLocationSecondary() {
    return locationSecondary;
  }

  public void setLocationSecondary(final String locationSecondary) {
    this.locationSecondary = locationSecondary;
  }

  public void addReport(final ActivityReport activityReport) {
    reports.add(activityReport);
  }

  public ArrayList<ActivityReport> getReports() {
    return reports;
  }

  public void addOwnedReport(final ActivityReport activityReport) {
    ownedReports.add(activityReport);
  }

  public ArrayList<ActivityReport> getOwnedReports() {
    return ownedReports;
  }
}
