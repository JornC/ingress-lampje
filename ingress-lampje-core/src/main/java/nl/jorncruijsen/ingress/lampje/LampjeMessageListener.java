package nl.jorncruijsen.ingress.lampje;

import nl.jorncruijsen.messaging.domain.Message;
import nl.jorncruijsen.messaging.listeners.MessageListener;
import nl.jorncruijsen.messaging.providers.MessageChannel;

public class LampjeMessageListener implements MessageListener {

  @Override
  public void handleMessage(final MessageChannel channel, final Message message) {
    System.out.println("Got message. - > " + message.getSender() + ": " + message.getText());

    channel.sendMessage("hello back");
  }

}
