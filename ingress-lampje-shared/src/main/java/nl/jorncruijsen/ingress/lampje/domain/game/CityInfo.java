package nl.jorncruijsen.ingress.lampje.domain.game;

import java.util.ArrayList;

public class CityInfo {
  private int attackers;
  private int contributors;
  private int attacks;

  private String cityName;
  private int dayDiff;
  private ArrayList<NameCountInfo> mostActiveAttackers;
  private ArrayList<NameCountInfo> mostActivePortals;

  public int getAttackers() {
    return attackers;
  }

  public int getContributors() {
    return contributors;
  }

  public int getAttacks() {
    return attacks;
  }

  public int getDayDiff() {
    return dayDiff;
  }

  public void setDayDiff(final int dayDiff) {
    this.dayDiff = dayDiff;
  }

  public String getCityName() {
    return cityName;
  }

  public void setCityName(final String cityName) {
    this.cityName = cityName;
  }

  public ArrayList<NameCountInfo> getMostActiveAttackers() {
    return mostActiveAttackers;
  }

  public ArrayList<NameCountInfo> getMostActivePortals() {
    return mostActivePortals;
  }

  public void setAttackers(final int attackers) {
    this.attackers = attackers;
  }

  public void setContributors(final int contributors) {
    this.contributors = contributors;
  }

  public void setAttacks(final int attacks) {
    this.attacks = attacks;
  }

  public void setMostActiveAttackers(ArrayList<NameCountInfo> mostActiveAttackers) {
    this.mostActiveAttackers = mostActiveAttackers;
  }

  public void setMostActivePortals(ArrayList<NameCountInfo> mostActivePortals) {
    this.mostActivePortals = mostActivePortals;
  }
}
