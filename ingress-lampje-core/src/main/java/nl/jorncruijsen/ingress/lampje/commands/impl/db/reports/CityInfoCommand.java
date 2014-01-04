package nl.jorncruijsen.ingress.lampje.commands.impl.db.reports;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import nl.jorncruijsen.ingress.lampje.DBRepository;
import nl.jorncruijsen.ingress.lampje.commands.SimpleBaseCommand;
import nl.jorncruijsen.ingress.lampje.domain.game.CityInfo;
import nl.jorncruijsen.ingress.lampje.domain.game.Portal;
import nl.jorncruijsen.ingress.lampje.retriever.ShortURLRetriever;
import nl.jorncruijsen.ingress.lampje.util.ReportService;

import com.vividsolutions.jts.io.ParseException;

public class CityInfoCommand extends SimpleBaseCommand {
  private static final int EXPECTED_PARTS = 2;
  private static final int MINIMAL_PORTALS_REQUIRED = 5;

  public CityInfoCommand() {
    super(EXPECTED_PARTS);
  }

  @Override
  protected String doCommand(final String[] splitBody, final List<String> flags) throws Exception {
    String commandString = splitBody[1];

    final Calendar instance = Calendar.getInstance();
    instance.add(Calendar.MONTH, -1);

    final int dayDiff = Math.round((Calendar.getInstance().getTime().getTime() - instance.getTime().getTime()) / 1000 / 60 / 60 / 24f);

    try {
      final CityInfo cityInfo = DBRepository.getCityInfo(commandString, new Date(new Date().getTime() - instance.getTime().getTime()));
      cityInfo.setDayDiff(dayDiff);
      cityInfo.setCityName(commandString);

      List<Portal> portals = DBRepository.getPortalsInCity(commandString);

      if (portals.size() < MINIMAL_PORTALS_REQUIRED) {
        return "Fewer than " + MINIMAL_PORTALS_REQUIRED + " portals found. Not doing this command for this area. (privacy)";
      }

      final String longUrl = ReportService.createAreaCoverageUrl(portals, flags.contains("markers"));
      String shortUrl = ShortURLRetriever.I.getShortUrl(longUrl);

      return ReportService.reportCityInfo(cityInfo, shortUrl, flags.contains("image"));
    } catch (final SQLException e) {
      e.printStackTrace();
      return "City could not be found";
    } catch (UnsupportedEncodingException e) {
      throw new Exception("Could not encode image");
    } catch (ParseException e) {
      throw new Exception("Could not parse image");
    }
  }
}
