package nl.jorncruijsen.ingress.lampje.domain.report;

public class Contributor {
  private final String name;
  private final String email;

  public Contributor(final String name, final String email) {
    this.name = name;
    this.email = email;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }
}
