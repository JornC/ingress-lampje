package nl.jorncruijsen.ingress.lampje.commands.impl.db.players;

import java.sql.SQLException;

import nl.jorncruijsen.ingress.lampje.DBRepository;
import nl.jorncruijsen.ingress.lampje.domain.game.Player;
import nl.jorncruijsen.messaging.domain.Message;
import nl.jorncruijsen.messaging.providers.MessageChannel;

public class EditPlayerLevel extends EditPlayerInfoBaseCommand {
  @Override
  boolean validate(final MessageChannel chat, final String data) {
    String text = null;
    int level = 0;
    try {
      level = Integer.parseInt(data);
    } catch (final NumberFormatException nfe) {
      text = "'" + data + "' is not a valid number, it should be between 1-8";
    }

    if (text == null && (level < 1 || level > 8)) {
      text = "Level argument should be between 1-8";
    }

    if (text != null) {
      chat.sendMessage(text);
    }

    return text == null;
  }

  @Override
  String getOldData(final Player player) {
    return String.valueOf(player.getLevel());
  }

  @Override
  public void trigger(final MessageChannel chat, final Message message) {
    final String body = message.getText();
    final String[] splittedBody = body.split(" ", 3);
    String text = null;

    if (splittedBody.length == 3) {
      final String nickname = splittedBody[1];
      final String data = splittedBody[2];
      Player originalPlayer = null;
      if (validate(chat, data)) {
        try {
          originalPlayer = DBRepository.getPlayer(nickname);

          if (originalPlayer != null) {
            final int level = originalPlayer.getLevel();
            originalPlayer.setLevel(Integer.parseInt(data));
            DBRepository.updatePlayer(originalPlayer);
            // catch (SQLException e) {
            // // TODO Auto-generated catch block
            // text =
            // "Server made a database booboo while trying to edit the agent information. Try again later. Contact admin if the problem persists.";
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            Player updatedPlayer = null;
            updatedPlayer = DBRepository.getPlayer(nickname);

            text = "Updated agent " + nickname + ". Old: '" + level + "' - new: '" + updatedPlayer.getLevel() + "'.";

          } else {
            text = "player not found";
          }

        } catch (final SQLException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    } else {
      text = "wrong arguments, rtfm";
    }
    if (text != null) {
      chat.sendMessage(text);
    }
  }
}
