package nl.jorncruijsen.ingress.lampje.commands.impl.db.players;

import java.sql.SQLException;
import java.util.List;

import nl.jorncruijsen.ingress.lampje.DBRepository;
import nl.jorncruijsen.ingress.lampje.commands.SimpleBaseCommand;
import nl.jorncruijsen.ingress.lampje.domain.game.Player;
import nl.jorncruijsen.ingress.lampje.domain.game.TEAM;

public class AddPlayerCommand extends SimpleBaseCommand {
  private static final int EXPECTED_PARTS = 4;

  public AddPlayerCommand() {
    super(EXPECTED_PARTS);
  }

  @Override
  protected String doCommand(final String[] splitBody, final List<String> flags) {
    final TEAM team = parseFactionFromAbbreviation(splitBody[1]);
    if (team == null) {
      return "invalid faction";
    }

    final Integer level = parseLevel(splitBody[3]);
    if (level == null) {
      return "Invalid level supplied. Should be a number in the range of 1-8";
    }

    final String name = splitBody[2];

    try {
      final Player player = new Player(name, team, level);
      DBRepository.insertPlayer(player);
    } catch (final SQLException e) {
      return name + " already exists in the database.";
    }

    return "New agent " + name + " (" + team.getFriendlyName() + ") added.";
  }

  private Integer parseLevel(final String levelString) {
    try {
      final int level = Integer.parseInt(levelString);

      if (level > 8 || level < 1) {
        return null;
      }

      return level;
    } catch (final NumberFormatException nfe) {
      return null;
    }
  }

  @Override
  protected String getInvalidCommandSyntaxError() {
    return "Invalid syntax. Usage: !add(player) [ENL/RES] [nick] [level]";
  }

  private TEAM parseFactionFromAbbreviation(final String factionName) {
    return "ENL".equals(factionName.toUpperCase()) ? TEAM.ENLIGHTENED : "RES".equals(factionName.toUpperCase()) ? TEAM.RESISTANCE : null;
  }
}
