package nl.jorncruijsen.ingress.lampje.commands.impl.misc;

import nl.jorncruijsen.ingress.lampje.commands.GlobalCommand;
import nl.jorncruijsen.messaging.domain.Message;
import nl.jorncruijsen.messaging.providers.MessageChannel;

public class WhackCommand implements GlobalCommand {

  @Override
  public void trigger(final MessageChannel chat, final Message message) {
    final String username = message.getSender();
    final String target = message.getText().contains(" ") ? message.getText().split(" ", 2)[1] : "nobody in particular";

    chat.sendMessage(String.format("%s slaps %s around a bit with a large trout", username, target));
  }
}
