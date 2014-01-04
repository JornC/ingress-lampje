package nl.jorncruijsen.ingress.lampje;

import java.util.HashMap;
import java.util.Map;

public class LampjeException extends Exception {
  private static final long serialVersionUID = 1578597024286291313L;

  public static int INTERNAL_ERROR = 500;
  public static int DATABASE_ERROR = 522;

  private static final Map<Integer, String> localizations = new HashMap<>();
  static {
    localizations.put(INTERNAL_ERROR, "Internal error occurred.");
    localizations.put(DATABASE_ERROR, "Database failure.");
  }

  public LampjeException(int error) {
    super(getErrorString(error));
  }

  private static String getErrorString(int error) {
    return localizations.get(error);
  }
}
