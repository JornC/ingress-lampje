package nl.jorncruijsen.ingress.lampje.commands.impl.db.reports;

import java.sql.SQLException;
import java.util.List;

import nl.jorncruijsen.ingress.lampje.DBRepository;
import nl.jorncruijsen.ingress.lampje.commands.GlobalCommand;
import nl.jorncruijsen.ingress.lampje.commands.SimpleBaseCommand;
import nl.jorncruijsen.ingress.lampje.domain.game.PortalReportInfo;
import nl.jorncruijsen.ingress.lampje.util.ReportService;

public class NavigationCommand extends SimpleBaseCommand implements GlobalCommand {
  private static final int EXPECTED_PARTS = 2;

  public NavigationCommand() {
    super(EXPECTED_PARTS);
  }

  @Override
  protected String doCommand(final String[] splitBody, final List<String> flags) throws Exception {
    try {
      final PortalReportInfo portalInfo = DBRepository.getPortalInfo(splitBody[1]);
      return ReportService.reportNavigationInfo(portalInfo);
    } catch (final SQLException e) {
      e.printStackTrace();
    }

    throw new Exception("Database not connected to bot.");
  }
}
