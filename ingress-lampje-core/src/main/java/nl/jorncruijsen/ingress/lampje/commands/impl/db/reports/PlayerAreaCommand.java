package nl.jorncruijsen.ingress.lampje.commands.impl.db.reports;

import java.sql.SQLException;
import java.util.List;

import nl.jorncruijsen.ingress.lampje.DBRepository;
import nl.jorncruijsen.ingress.lampje.commands.SimpleBaseCommand;
import nl.jorncruijsen.ingress.lampje.domain.game.Portal;
import nl.jorncruijsen.ingress.lampje.retriever.ShortURLRetriever;
import nl.jorncruijsen.ingress.lampje.util.ReportService;

public class PlayerAreaCommand extends SimpleBaseCommand {
  private static final int MINIMAL_PORTALS_REQUIRED = 5;
  private static final int EXPECTED_PARTS = 2;

  public PlayerAreaCommand() {
    super(EXPECTED_PARTS);
  }

  @Override
  protected String doCommand(final String[] splitBody, final List<String> flags) throws Exception {
    final String[] relevantParts = splitBody[1].split(" ", 2);

    final String playerName = relevantParts.length == 1 ? "" : relevantParts[0];
    final String city = relevantParts.length == 1 ? relevantParts[0] : relevantParts[1];

    try {
      List<Portal> portals;
      if (playerName.isEmpty()) {
        portals = DBRepository.getPortalsInCity(city);
      } else {
        portals = DBRepository.getPortalsInCityForPlayer(playerName, city);
      }

      if (portals.isEmpty()) {
        return "No activity tracked for this area. For allies: ( http://yogh.nl/lampje )";
      }

      if (portals.size() < MINIMAL_PORTALS_REQUIRED) {
        return "Fewer than " + MINIMAL_PORTALS_REQUIRED + " portals found. Not doing this command for this area. (privacy)";
      }

      final String longUrl = ReportService.createAreaCoverageUrl(portals, flags.contains("markers"));

      return ReportService.reportAreaActivityConcise(portals, ShortURLRetriever.I.getShortUrl(longUrl));
    } catch (final SQLException e) {
      throw new Exception("Database not connected to bot.");
    } catch (final Exception e) {
      e.printStackTrace();
      throw new Exception("Something mucked up while trying to parse the found portals.");
    }
  }

  @Override
  protected String getInvalidCommandSyntaxError() {
    return "Command syntax: !activity [player] [area]";
  }
}
