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
    /** Nothing yet **/

    // Add message services to the client
    messageClient.addMessagingService(xmppService);

    // Initialize the database
    DBManager.init(args[2], args[3]);

    Thread.sleep(2000);

    // Run the listeners
    new Thread() {
      @Override
      public void run() {
        // Create a global bot command message listener which will receive all messages
        final MessageListener globalCommandListener = new BotCommandListener();

        // Loop over all channels and add the global message listener
        final ChannelManager channelManager = messageClient.getChannelManager();
        for (final MessageChannel channel : channelManager) {
          channel.addMessageListener(globalCommandListener);
        }

        System.out.println("Fully initialized.");
      }
    }.start();

    /* Run indefinitely. */
    while (true) {
      try {
        Thread.sleep(60 * 1000 * 60);
      } catch (final InterruptedException e1) {
        e1.printStackTrace();
      }
    }
  }
}
