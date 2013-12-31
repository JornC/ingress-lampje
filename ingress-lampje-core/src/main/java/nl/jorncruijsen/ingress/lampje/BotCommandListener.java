package nl.jorncruijsen.ingress.lampje;

import java.util.HashMap;
import java.util.Map;

import nl.jorncruijsen.ingress.lampje.commands.BotCommand;
import nl.jorncruijsen.ingress.lampje.commands.impl.db.players.AddPlayerCommand;
import nl.jorncruijsen.ingress.lampje.commands.impl.db.players.EditPlayerLevel;
import nl.jorncruijsen.ingress.lampje.commands.impl.db.players.EditPlayerPrimaryLocation;
import nl.jorncruijsen.ingress.lampje.commands.impl.db.players.EditPlayerSecondaryLocation;
import nl.jorncruijsen.ingress.lampje.commands.impl.db.players.PlayerInfoCommand;
import nl.jorncruijsen.ingress.lampje.commands.impl.db.players.PlayerReportCommand;
import nl.jorncruijsen.ingress.lampje.commands.impl.db.players.PlayersFromBotCommand;
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
import nl.jorncruijsen.messaging.providers.MessageChannel;

public class BotCommandListener implements MessageListener {
  Map<String, BotCommand> commands = new HashMap<>();

  public BotCommandListener() {
    // Spam commands
    commands.put("!ping", new PingCommand());
    commands.put("!beer", new BeerCommand());

    // Player info commands
    commands.put("!playerinfo", new PlayerInfoCommand());
    commands.put("!pi", new PlayerInfoCommand());
    commands.put("!alliesfrom", new PlayersFromBotCommand());
    commands.put("!enemiesfrom", new PlayersFromBotCommand());
    commands.put("!pr", new PlayerReportCommand());
    commands.put("!playerreport", new PlayerReportCommand());
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
    commands.put("!update", new EditPlayerLevel());

    // Add primary location commands
    commands.put("!loc1", new EditPlayerPrimaryLocation());
    commands.put("!a1", new EditPlayerPrimaryLocation());
    commands.put("!addlocation1", new EditPlayerPrimaryLocation());
    commands.put("!addprimary", new EditPlayerPrimaryLocation());
    commands.put("!addprimarylocation", new EditPlayerPrimaryLocation());
    commands.put("!addlocationprimary", new EditPlayerPrimaryLocation());

    commands.put("!e1", new EditPlayerPrimaryLocation());
    commands.put("!editlocation1", new EditPlayerPrimaryLocation());
    commands.put("!editprimary", new EditPlayerPrimaryLocation());
    commands.put("!editprimarylocation", new EditPlayerPrimaryLocation());
    commands.put("!editlocationprimary", new EditPlayerPrimaryLocation());

    // Add secondary location commands
    commands.put("!loc2", new EditPlayerSecondaryLocation());
    commands.put("!addlocation2", new EditPlayerSecondaryLocation());
    commands.put("!addsecondary", new EditPlayerSecondaryLocation());
    commands.put("!addlocationsecondary", new EditPlayerSecondaryLocation());
    commands.put("!addsecondarylocation", new EditPlayerSecondaryLocation());

    // Misc
    commands.put("!slap", new WhackCommand());
    commands.put("!whack", new WhackCommand());
    commands.put("!help", new HelpCommand());
    commands.put("!dbinfo", new DBInfoCommand());
    commands.put("!boom", new BoomCommand());
    commands.put("boom", new BoomCommand());
  }

  @Override
  public void handleMessage(final MessageChannel chat, final Message message) {
    System.out.println(String.format("<-- %s: %s", message.getSender(), message.getText()));

    final String body = message.getText();
    final String firstWord = body.split(" ", 2)[0];
    final BotCommand cmd = commands.get(firstWord.toLowerCase());

    if (cmd != null) {
      System.out.println(String.format("- Triggering command %s"));
      new Thread(new Runnable() {

        @Override
        public void run() {
          cmd.trigger(chat, message);
        }
      }).start();
    } else {
      if (firstWord.substring(0, 1).equalsIgnoreCase("!")) {
        chat.sendMessage("command not found, type !help to get a list containing the available commands");
      }
    }
  }
}
