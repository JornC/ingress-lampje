package nl.jorncruijsen.ingress.lampje.commands.impl.misc;

import nl.jorncruijsen.ingress.lampje.commands.GlobalCommand;
import nl.jorncruijsen.messaging.domain.Message;
import nl.jorncruijsen.messaging.providers.AbstractMessageChannel;

public class BeerCommand implements GlobalCommand {
  private final String[] NON_ALCOHOLIC_DRINKS = new String[] { "a cola", "a fanta", "a cassis", "an iced tea", "a lemon lime" };
  private final String PEOPLE_NOT_DRINKING_ALCOHOL = ",Serrie,Serrie_L8,";

  /**
   * TODO Fix for non-partychat
   */
  @Override
  public void trigger(final AbstractMessageChannel chat, final Message message) {
    final String[] parts = message.getText().split(" ", 2);
    final String username = parts.length > 1 ? parts[1] : message.getSender();

    final String text;
    if (PEOPLE_NOT_DRINKING_ALCOHOL.toLowerCase().contains("," + username.toLowerCase() + ",")) {
      final String randomDrink = NON_ALCOHOLIC_DRINKS[(int) Math.round(Math.random() * (NON_ALCOHOLIC_DRINKS.length - 1))];
      text = "Uhh.. does not compute.. He doesn't drink beer. Getting " + username + " " + randomDrink + " instead";
    } else {
      text = "Getting a beer for " + username + "!";
    }

    chat.sendMessage(text);
  }

}
