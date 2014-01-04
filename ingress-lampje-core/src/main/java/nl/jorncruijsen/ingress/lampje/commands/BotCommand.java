package nl.jorncruijsen.ingress.lampje.commands;

import nl.jorncruijsen.messaging.domain.Message;
import nl.jorncruijsen.messaging.providers.AbstractMessageChannel;

public interface BotCommand {
  abstract void trigger(final AbstractMessageChannel chat, final Message message) throws Exception;
}
