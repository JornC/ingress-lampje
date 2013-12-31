package nl.jorncruijsen.ingress.lampje;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBManager {
  private static final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/lampje";

  private static Properties props = new Properties();

  public static void init(final String user, final String pass) {
    props.put("user", user);
    props.put("password", pass);
  }

  public static Connection createConnection() throws SQLException {
    return DriverManager.getConnection(CONNECTION_STRING, props);
  }
}
