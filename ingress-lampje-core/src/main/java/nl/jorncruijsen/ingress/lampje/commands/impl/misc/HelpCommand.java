package nl.jorncruijsen.ingress.lampje.commands.impl.misc;

import nl.jorncruijsen.ingress.lampje.commands.BotCommand;
import nl.jorncruijsen.ingress.lampje.commands.impl.db.players.PlayersFromBotCommand;
import nl.jorncruijsen.messaging.domain.Message;
import nl.jorncruijsen.messaging.providers.MessageChannel;

public class HelpCommand implements BotCommand {

  @Override
  public void trigger(final MessageChannel chat, final Message message) {
    final StringBuilder builder = new StringBuilder();

    final String playersFromArgs = "(" + PlayersFromBotCommand.PARAM_MINLEVEL + "[level]) " + "(" + PlayersFromBotCommand.PARAM_MAXLEVEL + "[level]) " + "(" + PlayersFromBotCommand.PARAM_PRIMARYLOCATION + ") " + "(" + PlayersFromBotCommand.PARAM_SECONDARYLOCATION + ")";

    builder.append("## Player information:\r\n" + "!playerinfo [agentname] (alias: !pi)\r\n" + "!enemiesfrom OR !alliesfrom " + playersFromArgs + " [city]\r\n" + "!activity [player] [area] (optional: -markers, displays top 5 active portals)"

    + "\r\n## Portal information:\r\n" + "!portalinfo [portalname] (alias: !portal)\r\n" + "!navigate [portalname] (alias: !nav)\r\n"

    + "\r\n## City information:\r\n" + "!cityinfo [city]\r\n"

    + "\r\n## Edit players:\r\n" + "!add(player) [ENL/RES] [agentname] [level]\r\n" + "!editlevel [agentname] [level]\r\n" + "!editlocationprimary [agentname] [location] (alias: !loc1, !editlocation1)\r\n" + "!addlocationsecondary [agentname] [location] (alias: !loc2, !addlocation2)\r\n" + "\r\n## Lampje report forward (We need more contributors!):" + "\r\nhttp://yogh.nl/lampje/\r\n");

    // + "## Area analytics:\r\n"
    // + "!scan [query]\r\n"
    // + "!track [30m->2h15m->3h] [area]\r\n"
    // + "!activenow [area] (-addlocation)\r\n"
    // +
    // "-- Scans the area for active players, guesses the level by checking which resonators are deployed. If the player database has a more accurate level available, shows that. If the player database contains a previous level, automatically updates it. Also adds the user location in the player database if the -addlocation flag is passed.\r\n"
    // +
    // "!findmods [-linkamp|-multihack|-forceamp|-heatsink|-turret|-shield] [area_query] (-all|-enlightened|-resistance|-elaborate)\r\n"
    // +
    // "-- Finds portal mods in the given area, you can look for all possible mods using their respective flags. Use -all to list all portals matching the filter, use -elaborate to show the portal address (TomTom!). This command is ideal for multi-hack hunting; home portal destruction or finding out which bar you wanna spend your afternoon at."
    // + "\r\n## Game analytics\r\n"
    // + "!score\r\n");

    builder.append("\r\n## Miscellaneous:\r\n" + "!dbinfo\r\n" + "!beer [nick]\r\n" + "!slap [nick] (alias: !whack)\r\n" + "!ping [optional echo text]\r\n");

    chat.sendMessage(builder.toString());
  }
}
