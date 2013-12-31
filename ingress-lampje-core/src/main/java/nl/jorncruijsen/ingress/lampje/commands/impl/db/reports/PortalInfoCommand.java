package nl.jorncruijsen.ingress.lampje.commands.impl.db.reports;

import java.sql.SQLException;
import java.util.List;

import nl.jorncruijsen.ingress.lampje.DBRepository;
import nl.jorncruijsen.ingress.lampje.commands.SimpleBaseCommand;
import nl.jorncruijsen.ingress.lampje.domain.game.PortalReportInfo;
import nl.jorncruijsen.ingress.lampje.util.ReportService;

public class PortalInfoCommand extends SimpleBaseCommand {
  private static final int EXPECTED_PARTS = 2;

  public PortalInfoCommand() {
    super(EXPECTED_PARTS);
  }

  @Override
  protected String doCommand(final String[] splitBody, final List<String> flags) {
    try {
      final PortalReportInfo portalInfo = DBRepository.getPortalInfo(splitBody[1]);
      return ReportService.reportPortalInfo(portalInfo);
    } catch (final SQLException e) {
      e.printStackTrace();
    }

    return "Database is afk.";
  }
}
