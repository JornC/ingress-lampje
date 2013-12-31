package nl.jorncruijsen.ingress.lampje.commands.impl.misc;

import nl.jorncruijsen.ingress.lampje.commands.BotCommand;
import nl.jorncruijsen.messaging.domain.Message;
import nl.jorncruijsen.messaging.providers.MessageChannel;

public class MaintenanceCommand implements BotCommand {

  @Override
  public void trigger(final MessageChannel chat, final Message message) {
    chat.sendMessage("This command is in maintenance and is not available for the time being.");
  }
}
