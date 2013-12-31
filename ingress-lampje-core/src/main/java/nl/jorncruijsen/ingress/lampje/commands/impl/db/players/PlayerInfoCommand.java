package nl.jorncruijsen.ingress.lampje.commands.impl.db.players;

import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import nl.jorncruijsen.ingress.lampje.DBRepository;
import nl.jorncruijsen.ingress.lampje.commands.SimpleBaseCommand;
import nl.jorncruijsen.ingress.lampje.domain.game.ActivityReport;
import nl.jorncruijsen.ingress.lampje.domain.game.Player;
import nl.jorncruijsen.ingress.lampje.domain.game.TEAM;

import org.apache.commons.lang.StringUtils;

public class PlayerInfoCommand extends SimpleBaseCommand {
  private static DateFormat formatter = DateFormat.getDateTimeInstance();

  public PlayerInfoCommand() {
    super(2);
  }

  @Override
  protected String doCommand(final String[] splitBody, final List<String> flags) {
    final String name = splitBody[1];

    try {
      final Player player = DBRepository.getPlayer(name, true);

      if(player == null) {
        return name + " not found.";
      }

      final StringBuilder builder = new StringBuilder();
      builder.append(TEAM.ENLIGHTENED.equals(player.getTeam()) ? "Ally " : "Enemy ");
      builder.append(player.getName());
      builder.append(String.format(" (L%s%s)", player.getLevel() == 0 ? "?" : player.getLevel(), player.getAp() != 0 ? String.format(", %.1fmil+ AP", player.getAp() / 1000000d) : ""));

      if (player.getLocationPrimary() != null) {
        builder.append(String.format(" from %s.", player.getLocationPrimary()));
      }

      if (player.getLocationSecondary() != null) {
        builder.append(String.format(" Locations active: %s.", player.getLocationSecondary()));
      }

      if (player.getLastSeen() != null) {
        builder.append(String.format(" Last update: %s.", DateFormat.getDateInstance().format(player.getLastSeen())));
      }

      final ArrayList<ActivityReport> reports = player.getReports();
      @SuppressWarnings("unused")
      final ArrayList<ActivityReport> ownedReports = player.getOwnedReports();
      if(reports.size() > 0) {
        final ActivityReport ar = reports.get(0);
        builder.append(String.format("\r\nLast known location: %s, %s, %s, (%s)", ar.getPortalName(), ar.getPortalAddress(), ar.getPortalCity(), formatter.format(reports.get(0).getDate())));
      } else if(player.getTeam() == TEAM.RESISTANCE) {
        builder.append("\nNo known recent location. See http://yogh.nl/lampje/");
      }

      final Map<String, Integer> map = new TreeMap<String, Integer>() {
        private static final long serialVersionUID = 2088279151147577353L;

        @Override
        public Integer get(final Object key) {
          if (!containsKey(key)) {
            put((String) key, 0);
          }

          return super.get(key);
        }
      };

      for (final ActivityReport report : player.getReports()) {
        if(report.getPortalCity() == null) {
          continue;
        }

        map.put(report.getPortalCity(), map.get(report.getPortalCity()) + 1);
      }

      for (final ActivityReport report : player.getOwnedReports()) {
        if(report.getPortalCity() == null) {
          continue;
        }

        map.put(report.getPortalCity(), map.get(report.getPortalCity()) + 1);
      }

      final String[] locations = new String[map.size()];
      int i = 0;
      for (final Entry<String, Integer> entry : map.entrySet()) {
        locations[i] = String.format("%s (%sx)", entry.getKey(), entry.getValue());
        i++;
      }

      builder.append("\r\nReports in: " + StringUtils.join(locations, ", "));

      return builder.toString();
    } catch (final SQLException e) {
      //      ChatUtil.sendMessageToMaster("DB gone..");
      return "I lost the database! :(";
    }
  }

  @Override
  protected String getInvalidCommandSyntaxError() {
    return "Please provide a username to lookup. Example: !pi / !playerinfo [agentname]";
  }
}
