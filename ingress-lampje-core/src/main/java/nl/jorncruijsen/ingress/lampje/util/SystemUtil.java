package nl.jorncruijsen.ingress.lampje.util;

/**
 * Determines the OS this VM is running on.
 */
public class SystemUtil {
  private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

  // Set on class initialisation
  public static final SYSTEM CURRENT;
  static {
    if (OS_NAME.contains("win")) {
      CURRENT = SYSTEM.WINDOWS;
    } else if (OS_NAME.contains("mac")) {
      CURRENT = SYSTEM.MAC;
    } else {
      // We're just gonna assume anything that's not mac or windows, is linux
      CURRENT = SYSTEM.LINUX;
    }
  }

  public enum SYSTEM {
    LINUX("lin"), WINDOWS("win"), MAC("mac");

    private final String quickName;

    private SYSTEM(final String quickName) {
      this.quickName = quickName;

    }

    public String getIdentifier() {
      return quickName;
    }
  };
}
