package nl.jorncruijsen.ingress.lampje.commands.impl.db.players;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import nl.jorncruijsen.ingress.lampje.DBRepository;
import nl.jorncruijsen.ingress.lampje.commands.SimpleBaseCommand;
import nl.jorncruijsen.ingress.lampje.domain.game.ActivityReport;
import nl.jorncruijsen.ingress.lampje.domain.game.Player;
import nl.jorncruijsen.ingress.lampje.domain.game.TEAM;

import org.apache.commons.lang.StringUtils;

public class PlayerInfoCommand extends SimpleBaseCommand {
  private static final int LOWER_LIMIT = 10;

  public PlayerInfoCommand() {
    super(2);
  }

  @Override
  protected String doCommand(final String[] splitBody, final List<String> flags) {
    final String name = splitBody[1];

    try {
      final Player player = DBRepository.getPlayer(name, true);

      if (player == null) {
        return name + " not found.";
      }

      final StringBuilder builder = new StringBuilder();
      builder.append(TEAM.ENLIGHTENED.equals(player.getTeam()) ? "ENL " : "RES ");
      builder.append(player.getName());
      builder.append(String.format(" (L%s)", player.getLevel() == 0 ? "?" : player.getLevel()));

      if (player.getLocationPrimary() != null) {
        builder.append(String.format(" from %s.", player.getLocationPrimary()));
      }

      // Cover thy eyes
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
        if (report.getPortalCity() == null) {
          continue;
        }

        map.put(report.getPortalCity(), map.get(report.getPortalCity()) + 1);
      }

      for (final ActivityReport report : player.getOwnedReports()) {
        if (report.getPortalCity() == null) {
          continue;
        }

        map.put(report.getPortalCity(), map.get(report.getPortalCity()) + 1);
      }

      final Set<String> filter = new HashSet<String>();
      for (final Entry<String, Integer> entry : map.entrySet()) {
        if (entry.getValue() < LOWER_LIMIT) {
          filter.add(entry.getKey());
        }
      }
      for (final String key : filter) {
        map.remove(key);
      }

      final String[] locations = new String[map.size()];
      int i = 0;
      for (final Entry<String, Integer> entry : map.entrySet()) {
        locations[i] = String.format("%s (%sx)", entry.getKey(), entry.getValue());
        i++;
      }

      if (locations.length > 0) {
        builder.append("\r\nReports (min. 10) in: " + StringUtils.join(locations, ", "));
      } else {
        builder.append("\r\nNo reports (<10 reports are not shown).");
      }

      return builder.toString();
    } catch (final SQLException e) {
      return "I lost the database! :(";
    }
  }

  @Override
  protected String getInvalidCommandSyntaxError() {
    return "Please provide a username to lookup. Example: !pi / !playerinfo [agentname]";
  }
}
