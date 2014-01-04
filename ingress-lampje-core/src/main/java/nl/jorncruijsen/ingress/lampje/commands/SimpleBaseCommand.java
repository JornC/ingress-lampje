package nl.jorncruijsen.ingress.lampje.commands;

import java.util.ArrayList;
import java.util.List;

import nl.jorncruijsen.messaging.domain.Message;
import nl.jorncruijsen.messaging.providers.AbstractMessageChannel;

public abstract class SimpleBaseCommand implements BotCommand {
  private static final String SEPERATION = " ";
  private static final String DEFAULT_SYNTAX_ERROR = "Invalid command syntax.";

  private final int expected;

  public SimpleBaseCommand() {
    this(-1);
  }

  public SimpleBaseCommand(final int expected) {
    this.expected = expected;
  }

  @Override
  public void trigger(final AbstractMessageChannel chat, final Message message) throws Exception {
    final ArrayList<String> flags = new ArrayList<>();

    String body = message.getText();

    // Extract the flags
    while(body.contains(" -")) {
      final int startIdx = body.indexOf(" -");
      int endIdx = body.indexOf(" ", startIdx + 2);
      endIdx = endIdx == -1 ? body.length() : endIdx;

      final String flag = body.substring(startIdx + 2, endIdx);
      flags.add(flag);

      body = body.replaceFirst(" -" + flag, "");
    }

    final String[] splitBody = body.split(SEPERATION, expected);

    if (splitBody.length < expected) {
      chat.sendMessage(getInvalidCommandSyntaxError());
    }

    final String result = doCommand(splitBody, flags);
    chat.sendMessage(result);
  }

  /**
   * Do the command, given the split body.
   * 
   * @param splitBody
   * 
   * @return Status string.
   * @throws Exception
   */
  protected abstract String doCommand(String[] splitBody, List<String> flags) throws Exception;

  /**
   * Return a String that indicates the command syntax is wrong.
   * 
   * @return See above.
   */
  protected String getInvalidCommandSyntaxError() {
    return DEFAULT_SYNTAX_ERROR;
  }
}
