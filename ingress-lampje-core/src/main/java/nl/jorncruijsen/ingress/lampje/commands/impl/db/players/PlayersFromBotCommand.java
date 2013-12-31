package nl.jorncruijsen.ingress.lampje.commands.impl.db.players;

import java.sql.SQLException;
import java.util.List;

import nl.jorncruijsen.ingress.lampje.DBRepository;
import nl.jorncruijsen.ingress.lampje.commands.BotCommand;
import nl.jorncruijsen.ingress.lampje.domain.game.Player;
import nl.jorncruijsen.ingress.lampje.domain.game.TEAM;
import nl.jorncruijsen.messaging.domain.Message;
import nl.jorncruijsen.messaging.providers.MessageChannel;

import org.apache.commons.lang.StringUtils;

public class PlayersFromBotCommand implements BotCommand {
  public static final String PARAM_PRIMARYLOCATION = "-primary";
  public static final String PARAM_SECONDARYLOCATION = "-secondary";
  public static final String PARAM_MINLEVEL = "-minlevel";
  public static final String PARAM_MAXLEVEL = "-maxlevel";

  @Override
  public void trigger(final MessageChannel chat, final Message message) {
    final String body = message.getText();
    final String[] splittedBody = body.split(" ");
    String text = null;

    if (splittedBody.length >= 2) {
      // allies from AND enemiesfrom? no playersfrom? would be welcome?
      final String faction = splittedBody[0].toLowerCase().contains("allies") ? TEAM.ENLIGHTENED.name() : TEAM.RESISTANCE.name();
      String cityToLookup;
      boolean searchPrimary = false;
      boolean searchSecondary = false;
      int minLevel = 0;
      int maxLevel = 8;

      int counter = 1;
      for (; counter < splittedBody.length; ++counter) {
        final String str = splittedBody[counter].toLowerCase();

        if (PARAM_PRIMARYLOCATION.equalsIgnoreCase(str)) {
          searchPrimary = true;
        } else if (PARAM_SECONDARYLOCATION.equalsIgnoreCase(str)) {
          searchSecondary = true;
        } else if (str.startsWith(PARAM_MINLEVEL)) {
          try {
            minLevel = Integer.parseInt(str.substring(PARAM_MINLEVEL.length()));
          } catch (final NumberFormatException nfe) {
            minLevel = -1;
            break;
          }
        } else if (str.startsWith(PARAM_MAXLEVEL)) {
          try {
            maxLevel = Integer.parseInt(str.substring(PARAM_MAXLEVEL.length()));
          } catch (final NumberFormatException nfe) {
            maxLevel = -1;
            break;
          }
        } else {
          break;
        }
      }

      if (counter == splittedBody.length) {
        cityToLookup = null;
      } else {
        cityToLookup = StringUtils.join(splittedBody, ' ', counter, splittedBody.length);
      }

      if (!searchPrimary && !searchSecondary) {
        searchPrimary = searchSecondary = true;
      }

      if (cityToLookup == null) {
        text = "I need you to provide a city to lookup as argument. Example: Arnhem";
      } else if (cityToLookup.length() < 3) {
        text = "City should at least contain 3 characters.";
      } else if (minLevel == -1) {
        text = "Invalid minimum level specified. Example usage: " + PARAM_MINLEVEL + "5";
      } else if (maxLevel == -1) {
        text = "Invalid maximum level specified. Example usage: " + PARAM_MAXLEVEL + "8";
      } else if (minLevel > 0 && maxLevel > 0 && minLevel > maxLevel) {
        text = "Really? The minimum level cannot be higher than the maximum level. DUH.";
      } else {
        List<Player> players = null;
        try {
          players = DBRepository.getPlayersFrom(faction, cityToLookup, minLevel, maxLevel, searchPrimary, searchSecondary);
        } catch (final SQLException e) {
          e.printStackTrace();
          text = "something failed with execution of query";
        }

        if (text == null && players == null || players.size() == 0) {
          text = "No players from " + cityToLookup + " found with the parameters you provided.";
        } else {
          final StringBuilder builder = new StringBuilder("Found " + players.size() + " player(s) from " + cityToLookup + ": ");
          for (int i = 0; i < players.size(); ++i) {
            final Player player = players.get(i);

            builder.append(player.getName() + " (L" + player.getLevel() + ")");
            if (i < players.size() - 1) {
              builder.append(", ");
            }
          }

          text = builder.toString();
        }
      }
    } else {
      text = "I need you to provide a city to lookup as argument. Example: Nijmegen";
    }

    chat.sendMessage(text);
  }
}