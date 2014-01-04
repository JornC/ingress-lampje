package nl.jorncruijsen.ingress.lampje;

import java.util.HashMap;
import java.util.Map;

import nl.jorncruijsen.ingress.lampje.commands.BotCommand;
import nl.jorncruijsen.ingress.lampje.commands.impl.db.players.AddPlayerCommand;
import nl.jorncruijsen.ingress.lampje.commands.impl.db.players.EditPlayerLevel;
import nl.jorncruijsen.ingress.lampje.commands.impl.db.players.EditPlayerPrimaryLocation;
import nl.jorncruijsen.ingress.lampje.commands.impl.db.players.PlayerInfoCommand;
import nl.jorncruijsen.ingress.lampje.commands.impl.db.reports.CityInfoCommand;
import nl.jorncruijsen.ingress.lampje.commands.impl.db.reports.DBInfoCommand;
import nl.jorncruijsen.ingress.lampje.commands.impl.db.reports.NavigationCommand;
import nl.jorncruijsen.ingress.lampje.commands.impl.db.reports.PlayerAreaCommand;
import nl.jorncruijsen.ingress.lampje.commands.impl.db.reports.PortalImageCommand;
import nl.jorncruijsen.ingress.lampje.commands.impl.db.reports.PortalInfoCommand;
import nl.jorncruijsen.ingress.lampje.commands.impl.misc.BeerCommand;
import nl.jorncruijsen.ingress.lampje.commands.impl.misc.BoomCommand;
import nl.jorncruijsen.ingress.lampje.commands.impl.misc.HelpCommand;
import nl.jorncruijsen.ingress.lampje.commands.impl.misc.PingCommand;
import nl.jorncruijsen.ingress.lampje.commands.impl.misc.WhackCommand;
import nl.jorncruijsen.messaging.domain.Message;
import nl.jorncruijsen.messaging.listeners.MessageListener;
import nl.jorncruijsen.messaging.providers.AbstractMessageChannel;
import nl.jorncruijsen.messaging.providers.MessageChannel;

public class BotCommandListener implements MessageListener {
  private final Map<String, BotCommand> commands = new HashMap<>();

  private final MessageChannel ownerChannelFinal;

  public BotCommandListener(final MessageChannel ownerChannelFinal) {
    this.ownerChannelFinal = ownerChannelFinal;

    // Spam commands
    commands.put("!ping", new PingCommand());
    commands.put("!beer", new BeerCommand());

    // Player info commands
    commands.put("!playerinfo", new PlayerInfoCommand());
    commands.put("!pi", new PlayerInfoCommand());
    commands.put("!activity", new PlayerAreaCommand());

    // Portal info commands
    commands.put("!portalinfo", new PortalInfoCommand());
    commands.put("!portal", new PortalInfoCommand());
    commands.put("!nav", new NavigationCommand());
    commands.put("!navigate", new NavigationCommand());
    commands.put("!navigation", new NavigationCommand());

    // City info commands
    commands.put("!cityinfo", new CityInfoCommand());
    commands.put("!city", new CityInfoCommand());
    commands.put("!image", new PortalImageCommand());
    commands.put("!top5", new PortalImageCommand());

    // Add player commands
    commands.put("!add", new AddPlayerCommand());
    commands.put("!addplayer", new AddPlayerCommand());
    commands.put("!addagent", new AddPlayerCommand());

    // Edit level commands
    commands.put("!editlevel", new EditPlayerLevel());

    // Add primary location commands
    commands.put("!loc", new EditPlayerPrimaryLocation());
    commands.put("!setlocation", new EditPlayerPrimaryLocation());

    // Misc
    commands.put("!slap", new WhackCommand());
    commands.put("!whack", new WhackCommand());
    commands.put("!help", new HelpCommand());
    commands.put("!dbinfo", new DBInfoCommand());
    commands.put("!boom", new BoomCommand());
    commands.put("boom", new BoomCommand());
  }

  @Override
  public void handleMessage(final AbstractMessageChannel chat, final Message message) {
    // TODO Logging shouldn't be here.
    System.out.println(String.format("<-- %s - %s: %s", chat.getChannelId(), message.getSender(), message.getText()));

    final String body = message.getText();
    final String firstWord = body.split(" ", 2)[0].toLowerCase();

    BotCommand cmd = commands.get(firstWord);

    if (cmd != null) {
      System.out.println(String.format("- Triggering command %s", cmd.getClass().getSimpleName()));

      try {
        cmd.trigger(chat, message);
      } catch(Exception e) {
        e.printStackTrace();
        chat.sendMessage("Error occurred while trying to execute the command - %s [error reported to admin]", e.getMessage());
        ownerChannelFinal.sendMessage("Exception while executing a command! %s", e.getMessage());
      }

    }
  }
}
