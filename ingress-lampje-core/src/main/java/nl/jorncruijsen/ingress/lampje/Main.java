package nl.jorncruijsen.ingress.lampje;

import java.util.Properties;

import nl.jorncruijsen.messaging.MultiPlexedMessageService;
import nl.jorncruijsen.messaging.XMPPMessageService;
import nl.jorncruijsen.messaging.listeners.MessageListener;
import nl.jorncruijsen.messaging.providers.ChannelManager;
import nl.jorncruijsen.messaging.providers.MessageChannel;
import nl.jorncruijsen.messaging.providers.MessageService;

public class Main {
  public static void main(final String... args) throws InterruptedException {
    // Initialize the message service
    final MultiPlexedMessageService messageClient = new MultiPlexedMessageService();

    // Initialize the XMPP message service
    final Properties xmppProperties = new Properties();
    xmppProperties.put("xmpp.user", args[0]);
    xmppProperties.put("xmpp.pass", args[1]);
    final MessageService xmppService = new XMPPMessageService();
    xmppService.init(xmppProperties);

    // Initialize the GroupMe message service

    // Add message services to the client
    messageClient.addMessagingService(xmppService);

    // Create a global message listener which will receive all messages
    final MessageListener globalMessageListener = new LampjeMessageListener();

    Thread.sleep(2000);

    // Loop over all channels and add the global message listener
    final ChannelManager channelManager = messageClient.getChannelManager();
    for (final MessageChannel channel : channelManager) {
      channel.addMessageListener(globalMessageListener);
    }

    System.out.println("Fully initialized.");

    // Run indefinitely.
    final Thread t = new Thread() {
      @Override
      public void run() {
        synchronized (this) {
          try {
            this.wait();
          } catch (final InterruptedException e) {
            e.printStackTrace();
          }
        }
      };
    };
    t.start();
    t.join();
  }
}
