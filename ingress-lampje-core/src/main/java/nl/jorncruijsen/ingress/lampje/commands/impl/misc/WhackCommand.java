package nl.jorncruijsen.ingress.lampje.commands.impl.misc;

import nl.jorncruijsen.ingress.lampje.commands.GlobalCommand;
import nl.jorncruijsen.messaging.domain.Message;
import nl.jorncruijsen.messaging.providers.MessageChannel;

public class WhackCommand implements GlobalCommand {

  @Override
  public void trigger(final MessageChannel chat, final Message message) {
    String username = message.getSender();

    final String target = message.getText().split(" ", 2)[1];

    chat.sendMessage(username + " slaps " + target + " around a bit with a large trout");
  }

}
