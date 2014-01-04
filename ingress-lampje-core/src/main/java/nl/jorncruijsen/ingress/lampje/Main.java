package nl.jorncruijsen.ingress.lampje;

import java.util.Properties;

import javax.mail.MessagingException;

import nl.jorncruijsen.ingress.lampje.retriever.GeoRetriever;
import nl.jorncruijsen.ingress.lampje.retriever.ShortURLRetriever;
import nl.jorncruijsen.ingress.lampje.retriever.URLRetriever;
import nl.jorncruijsen.messaging.GroupMeMessageService;
import nl.jorncruijsen.messaging.MultiPlexedMessageService;
import nl.jorncruijsen.messaging.XMPPMessageService;
import nl.jorncruijsen.messaging.listeners.MessageListener;
import nl.jorncruijsen.messaging.providers.AbstractMessageChannel;
import nl.jorncruijsen.messaging.providers.ChannelManager;
import nl.jorncruijsen.messaging.providers.MessageChannel;
import nl.jorncruijsen.messaging.providers.MessageService;

public class Main {
  public static void main(final String... args) throws InterruptedException {
    // Initialize the message service
    final MultiPlexedMessageService messageClient = new MultiPlexedMessageService();

    URLRetriever urlRetriever = new URLRetriever();
    ShortURLRetriever.create(urlRetriever);

    // Initialize the XMPP message service
    final Properties xmppProperties = new Properties();
    xmppProperties.put("xmpp.user", args[0]);
    xmppProperties.put("xmpp.pass", args[1]);
    final MessageService xmppService = new XMPPMessageService();
    xmppService.init(xmppProperties);

    // Initialize the GroupMe message service
    Properties groupMeProperties = new Properties();
    groupMeProperties.put("groupme.botnames", args[2]);
    groupMeProperties.put("groupme.groups", args[3]);
    groupMeProperties.put("groupme.keys", args[4]);
    MessageService groupMeService = new GroupMeMessageService();
    groupMeService.init(groupMeProperties);

    // Add message services to the client
    messageClient.addMessagingService(xmppService);
    messageClient.addMessagingService(groupMeService);

    // Initialize the database
    GeoRetriever.create();
    DBManager.init(args[5], args[6]);

    MessageChannel ownerChannel = null;
    for (MessageChannel channel : xmppService.getChannelManager()) {
      if (channel.getChannelId().equals(args[7])) {
        ownerChannel = channel;
        break;
      }
    }

    final MessageChannel ownerChannelFinal = ownerChannel;

    // Run the listeners
    new Thread() {
      @Override
      public void run() {
        // Create a global bot command message listener which will receive all
        // messages
        final MessageListener globalCommandListener = new BotCommandListener(ownerChannelFinal);

        // Loop over all channels and add the global message listener
        final ChannelManager channelManager = messageClient.getChannelManager();
        for (final AbstractMessageChannel channel : channelManager) {
          channel.addMessageListener(globalCommandListener);
        }
      }
    }.start();

    try {
      MailRetriever.init(args[0], args[1]);
    } catch (MessagingException e) {
      e.printStackTrace();
    }

    ownerChannel.sendMessage("Lampje has been initialized.");

    /* Run indefinitely. */
    while (true) {
      try {
        MailRetriever.goGoGadget();

        Thread.sleep(60 * 1000 * 2);
      } catch (final InterruptedException e1) {
        e1.printStackTrace();
      }
    }
  }
}
