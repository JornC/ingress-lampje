package nl.jorncruijsen.ingress.lampje.commands.impl.db.reports;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import nl.jorncruijsen.ingress.lampje.DBRepository;
import nl.jorncruijsen.ingress.lampje.commands.SimpleBaseCommand;
import nl.jorncruijsen.ingress.lampje.domain.game.CityInfo;
import nl.jorncruijsen.ingress.lampje.util.ReportService;

public class CityInfoCommand extends SimpleBaseCommand {
  private static final int EXPECTED_PARTS = 2;

  private static final HashMap<String, Integer> PERIOD_MAP = new HashMap<>();
  static {
    PERIOD_MAP.put("-day", Calendar.DAY_OF_YEAR);
    PERIOD_MAP.put("-week", Calendar.WEEK_OF_YEAR);
    PERIOD_MAP.put("-month", Calendar.MONTH);
    PERIOD_MAP.put("-year", Calendar.YEAR);
  }

  public CityInfoCommand() {
    super(EXPECTED_PARTS);
  }

  @Override
  protected String doCommand(final String[] splitBody, final List<String> flags) {
    String commandString = splitBody[1];

    int periodField = Calendar.MONTH;
    for (final Entry<String, Integer> entry : PERIOD_MAP.entrySet()) {
      if (commandString.contains(entry.getKey())) {
        periodField = entry.getValue();
        commandString = commandString.replace(entry.getKey(), "");
        break;
      }
    }

    final Calendar instance = Calendar.getInstance();
    instance.add(periodField, -1);

    final int dayDiff = Math.round((Calendar.getInstance().getTime().getTime() - instance.getTime().getTime()) / 1000 / 60 / 60 / 24f);

    try {
      final CityInfo cityInfo = DBRepository.getCityInfo(commandString, new Date(new Date().getTime() - instance.getTime().getTime()));
      cityInfo.setDayDiff(dayDiff);
      cityInfo.setCityName(commandString);

      return ReportService.reportCityInfo(cityInfo);
    } catch (final SQLException e) {
      e.printStackTrace();
      return "City could not be found";
    }
  }
}
