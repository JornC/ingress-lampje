package nl.jorncruijsen.ingress.lampje.commands.impl.misc;

import nl.jorncruijsen.ingress.lampje.commands.GlobalCommand;
import nl.jorncruijsen.messaging.domain.Message;
import nl.jorncruijsen.messaging.providers.MessageChannel;

public class PingCommand implements GlobalCommand {
  @Override
  public void trigger(final MessageChannel chat, final Message message) {
    chat.sendMessage(message.getText().replaceFirst("[pP][iI][nN][gG]", "pong"));
  }
}
