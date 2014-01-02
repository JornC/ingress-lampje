package nl.jorncruijsen.ingress.lampje.util;


public final class FormatUtil {
  public static String formatGoogleQuery(final String query) {
    return query.trim().replace(" ", "+").replace("\"", "");
  }
}
