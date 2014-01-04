package nl.jorncruijsen.ingress.lampje.commands.impl.misc;

import nl.jorncruijsen.ingress.lampje.commands.BotCommand;
import nl.jorncruijsen.messaging.domain.Message;
import nl.jorncruijsen.messaging.providers.AbstractMessageChannel;

public class HelpCommand implements BotCommand {

  @Override
  public void trigger(final AbstractMessageChannel chat, final Message message) {
    final StringBuilder msg = new StringBuilder();
    msg.append(">> Player information:\r\n"
        + "!playerinfo [agentname] (alias: !pi)\r\n"
        + "!activity [player] [area] (flags: -markers)\r\n"

    + "\r\n>> Portal information:\r\n"
    + "!portalinfo [portalname] (alias: !portal)\r\n"
    + "!navigate [portalname] (alias: !nav)\r\n"

    + "\r\n>> City information:\r\n"
    + "!cityinfo [city] (flags: -image)\r\n"

    + "\r\n>> Edit players:\r\n"
    + "!add(player) [ENL/RES] [agentname] [level]\r\n"
    + "!editlevel [agentname] [level]\r\n"
    + "!setlocation [agentname] [location]\r\n"

    + "\r\n>> Lampje report forward (We need more contributors!):\r\n"
    + "http://yogh.nl/lampje/\r\n"

    + "\r\n>> Miscellaneous:\r\n"
    + "!dbinfo\r\n"
    + "!beer [nick]\r\n"
    + "!slap [nick] (alias: !whack)\r\n"
    + "!ping [optional echo text]\r\n");

    chat.sendMessage(msg.toString());
  }
}
