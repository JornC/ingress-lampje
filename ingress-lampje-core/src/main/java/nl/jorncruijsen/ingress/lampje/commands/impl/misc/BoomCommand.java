package nl.jorncruijsen.ingress.lampje.commands.impl.misc;

import java.util.List;

import nl.jorncruijsen.ingress.lampje.commands.GlobalCommand;
import nl.jorncruijsen.ingress.lampje.commands.SimpleBaseCommand;

public class BoomCommand extends SimpleBaseCommand implements GlobalCommand {
  @Override
  protected String doCommand(final String[] splitBody, final List<String> flags) {
    return "HEADSHOT";
  }
}
