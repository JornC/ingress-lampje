package nl.jorncruijsen.ingress.lampje.commands;

import nl.jorncruijsen.messaging.domain.Message;
import nl.jorncruijsen.messaging.providers.MessageChannel;

public interface BotCommand {
  abstract void trigger(final MessageChannel chat, final Message message);
}
