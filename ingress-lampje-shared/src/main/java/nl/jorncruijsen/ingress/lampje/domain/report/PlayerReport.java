package nl.jorncruijsen.ingress.lampje.domain.report;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import nl.jorncruijsen.ingress.lampje.domain.game.Portal;

/**
 * TODO Refactor, 1 map, keys being the city, values being full report for that city
 */
public class PlayerReport {
  private final Map<String, Map<String, Integer>> playerEncounters = new LinkedHashMap<>();
  private final Map<String, Map<Portal, Integer>> portalEncounters = new LinkedHashMap<>();

  private final String playerName;

  public PlayerReport(final String playerName) {
    this.playerName = playerName;
  }

  public void addPlayerEncounter(final String city, final String attacker, final int count) {
    if (!playerEncounters.containsKey(city)) {
      playerEncounters.put(city, new HashMap<String, Integer>());
    }

    playerEncounters.get(city).put(attacker, count);
  }

  public void addPortalEncounter(final String city, final Portal portal, final int count) {
    if (!portalEncounters.containsKey(city)) {
      portalEncounters.put(city, new HashMap<Portal, Integer>());
    }

    portalEncounters.get(city).put(portal, count);
  }

  public Map<String, Map<String, Integer>> getPlayerEncounters() {
    return playerEncounters;
  }

  public Map<String, Map<Portal, Integer>> getPortalEncounters() {
    return portalEncounters;
  }

  public String getPlayerName() {
    return playerName;
  }
}
