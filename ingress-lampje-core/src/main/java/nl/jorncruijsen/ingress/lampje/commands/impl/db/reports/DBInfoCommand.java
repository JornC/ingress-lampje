package nl.jorncruijsen.ingress.lampje.commands.impl.db.reports;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import nl.jorncruijsen.ingress.lampje.DBRepository;
import nl.jorncruijsen.ingress.lampje.commands.SimpleBaseCommand;
import nl.jorncruijsen.ingress.lampje.util.ReportService;

public class DBInfoCommand extends SimpleBaseCommand {
  @Override
  protected String doCommand(final String[] splitBody, final List<String> flags) {
    try {
      final int playerNum = DBRepository.getPlayerNum();
      final int reportNum = DBRepository.getReportNum();
      final int contributorNum = DBRepository.getContributorNum();
      final int cityNum = DBRepository.getCityNum();
      final int portalNum = DBRepository.getPortalNum();
      final Date firstReportDate = DBRepository.getFirstReportDate();

      return ReportService.reportDBInfo(playerNum, reportNum, contributorNum, cityNum, portalNum, firstReportDate);
    } catch (final SQLException e) {
      e.printStackTrace();
      return "Could not find database.";
    }
  }
}
