package nl.jorncruijsen.ingress.lampje.commands.impl.db.players;

import nl.jorncruijsen.ingress.lampje.commands.BotCommand;
import nl.jorncruijsen.ingress.lampje.domain.game.Player;
import nl.jorncruijsen.messaging.providers.AbstractMessageChannel;

public abstract class EditPlayerInfoBaseCommand implements BotCommand {

  public EditPlayerInfoBaseCommand() {
  }

  abstract boolean validate(final AbstractMessageChannel chat, final String data);

  abstract String getOldData(final Player player);

}
