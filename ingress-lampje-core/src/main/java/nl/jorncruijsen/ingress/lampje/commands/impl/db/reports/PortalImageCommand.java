package nl.jorncruijsen.ingress.lampje.commands.impl.db.reports;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nl.jorncruijsen.ingress.lampje.DBRepository;
import nl.jorncruijsen.ingress.lampje.commands.SimpleBaseCommand;
import nl.jorncruijsen.ingress.lampje.domain.game.Portal;
import nl.jorncruijsen.ingress.lampje.retriever.ShortURLRetriever;
import nl.jorncruijsen.ingress.lampje.util.ReportService;

public class PortalImageCommand extends SimpleBaseCommand {
  private static final int EXPECTED_PARTS = 2;

  private static final int DEFAULT_LIMIT = 5;

  public PortalImageCommand() {
    super(EXPECTED_PARTS);
  }

  @Override
  protected String doCommand(final String[] splitBody, final List<String> flags) {
    try {
      final ArrayList<Portal> portalInfo = DBRepository.getTopPortals(splitBody[1], DEFAULT_LIMIT);
      final String reportPortalImageUrl = ReportService.reportPortalImageUrl(portalInfo);

      final String shortUrl = ShortURLRetriever.I.getShortUrl(reportPortalImageUrl);

      return ReportService.reportPortalImage(portalInfo, shortUrl);
    } catch (final SQLException e) {
      e.printStackTrace();
    }

    return "Database is afk. [reported]";
  }
}
