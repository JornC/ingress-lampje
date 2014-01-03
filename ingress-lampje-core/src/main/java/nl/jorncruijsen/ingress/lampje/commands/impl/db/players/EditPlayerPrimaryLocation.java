package nl.jorncruijsen.ingress.lampje.commands.impl.db.players;

import java.sql.SQLException;

import nl.jorncruijsen.ingress.lampje.DBRepository;
import nl.jorncruijsen.ingress.lampje.commands.BotCommand;
import nl.jorncruijsen.ingress.lampje.domain.game.Player;
import nl.jorncruijsen.messaging.domain.Message;
import nl.jorncruijsen.messaging.providers.AbstractMessageChannel;

public class EditPlayerPrimaryLocation implements BotCommand {
  @Override
  public void trigger(final AbstractMessageChannel chat, final Message message) {
    final String body = message.getText();
    final String[] splittedBody = body.split(" ", 3);
    String text = null;

    if (splittedBody.length == 3) {
      final String nickname = splittedBody[1];
      final String data = splittedBody[2];

      try {
        final Player originalPlayer = DBRepository.getPlayer(nickname);
        if (originalPlayer != null) {
          final String originalLocation = originalPlayer.getLocationPrimary();
          originalPlayer.setLocationPrimary(data);
          DBRepository.updatePlayer(originalPlayer);
          final Player updatedPlayer = DBRepository.getPlayer(nickname);
          text = "Updated agent " + nickname + ". Old: '" + originalLocation + "' - new: '" + updatedPlayer.getLocationPrimary() + "'.";
        } else {
          text = "player not found";
        }
      } catch (final SQLException e) {
        text = "Server made a database booboo while trying to edit the agent information. Try again later. Contact admin if the problem persists.";

        e.printStackTrace();
      }
    } else {
      text = "Command syntax: !setlocation [player] [new_location]";
    }

    if (text != null) {
      chat.sendMessage(text);
    }
  }
}
