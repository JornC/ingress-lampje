package nl.jorncruijsen.ingress.lampje;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.jorncruijsen.ingress.lampje.domain.game.ActivityReport;
import nl.jorncruijsen.ingress.lampje.domain.game.CityInfo;
import nl.jorncruijsen.ingress.lampje.domain.game.NameCountInfo;
import nl.jorncruijsen.ingress.lampje.domain.game.Player;
import nl.jorncruijsen.ingress.lampje.domain.game.Portal;
import nl.jorncruijsen.ingress.lampje.domain.game.PortalReportInfo;
import nl.jorncruijsen.ingress.lampje.domain.game.TEAM;
import nl.jorncruijsen.ingress.lampje.domain.geo.GeoInformation;
import nl.jorncruijsen.ingress.lampje.domain.report.Contributor;
import nl.jorncruijsen.ingress.lampje.domain.report.GlobalStatistics;
import nl.jorncruijsen.ingress.lampje.domain.report.PlayerReport;

/**
 * TODO Refactor to get DB connection in methods instead of creating one. (no
 * DBManager reference here)
 */
public class DBRepository {
  // Contributors
  private static final String INSERT_CONTRIBUTOR = "INSERT IGNORE INTO lampje.contributors (agent_name, email) VALUES (?, ?)";
  private static final String SELECT_CONTRIBUTORS = "SELECT * FROM lampje.contributors";

  // Player
  private static final String SELECT_PLAYER_QUERY = "SELECT * FROM lampje.players WHERE name=? LIMIT 1;";
  private static final String UPDATE_PLAYER_QUERY = "UPDATE lampje.players SET level=?, location_primary=?, location_secondary=?, ap=?, last_modified=?, last_seen=? WHERE name=?";
  private static final String INSERT_PLAYER_BARE_QUERY = "INSERT INTO lampje.players (level, name, faction, last_modified, last_seen) VALUES(?, ?, ?, ?, ?)";
  private static final String GET_PLAYERS_FROM_QUERY = "SELECT * FROM lampje.players WHERE faction=? AND level BETWEEN ? AND ? AND (location_primary LIKE ? OR location_secondary LIKE ?) ORDER BY level DESC";
  // private static final String INSERT_OR_UPDATE_PLAYER_QUERY =
  // "INSERT INTO players (name, faction, location_primary, location_secondary) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE location_primary=VALUES(location_primary), location_secondary=VALUES(location_secondary);";

  // Stats
  private static final String SELECT_REPORT_NUM = "SELECT count(*) as count from lampje.reports WHERE date > DATE(NOW() - INTERVAL 1 MONTH)";
  private static final String SELECT_CONTRIBUTOR_NUM = "SELECT count(*) as count from lampje.contributors";
  private static final String SELECT_CITY_NUM = "SELECT count(DISTINCT portal_city) as count FROM lampje.reports, lampje.portals WHERE portal_id=portals.id AND date > DATE(NOW() - INTERVAL 1 MONTH)";
  private static final String SELECT_ATTACKER_NUM = "SELECT count(DISTINCT portal_attacker_name) as count FROM lampje.reports, lampje.portals WHERE portal_id=portals.id AND date > DATE(NOW() - INTERVAL 1 MONTH)";
  private static final String SELECT_PORTAL_NUM = "SELECT count(DISTINCT portal_id) as count FROM lampje.reports, lampje.portals WHERE portal_id=portals.id AND date > DATE(NOW() - INTERVAL 1 MONTH)";

  // Reports
  private static final String SELECT_REPORTS_QUERY = "SELECT * FROM lampje.reports, lampje.portals WHERE portal_attacker_name=? AND reports.portal_id = portals.id AND date > DATE(NOW() - INTERVAL 1 MONTH) ORDER BY date DESC;";
  private static final String SELECT_OWNED_REPORTS_QUERY = "SELECT * FROM lampje.reports, lampje.portals WHERE report_origin_name=? AND reports.portal_id = portals.id AND date > DATE(NOW() - INTERVAL 1 MONTH) ORDER BY date DESC;";
  private static final String INSERT_DAMAGE_REPORT_QUERY = "INSERT INTO lampje.reports (portal_attacker_name, portal_raw_address, date, report_origin_name, portal_id) VALUES(?, ?, ?, ?, ?)";
  private static final String INERT_GEO_INFO = "INSERT INTO lampje.portals (portal_name, portal_city, portal_address, portal_lon, portal_lat) VALUES(?, ?, ?, ?, ?)";
  private static final String SELECT_PLAYER_REPORTS = "SELECT COUNT(*) as count, portal_city, portal_attacker_name FROM lampje.reports, lampje.portals WHERE report_origin_name=? AND date > DATE(NOW() - INTERVAL 1 MONTH) AND reports.portal_id = portals.id GROUP BY portal_city, portal_attacker_name";
  private static final String SELECT_PLAYER_PORTALS = "SELECT COUNT(*) as count, portal_name, portal_city, portal_lon, portal_lat FROM lampje.reports, lampje.portals WHERE report_origin_name=? AND reports.date > DATE(NOW() - INTERVAL 1 MONTH) AND reports.portal_id = portals.id GROUP BY portal_name, portal_city, portal_address";
  private static final String SELECT_PLAYER_CITY_PORTALS = "SELECT count(*) as count, portal_name, portal_lon, portal_lat FROM lampje.reports, lampje.portals WHERE (report_origin_name=? OR portal_attacker_name=?) AND date > DATE(NOW() - INTERVAL 1 MONTH) AND portal_city=? AND reports.portal_id = portals.id GROUP BY portal_name, portal_address ORDER BY count DESC";
  private static final String SELECT_CITY_PORTALS = "SELECT count(*) as count, portal_name, portal_lon, portal_lat FROM lampje.reports, lampje.portals WHERE portal_city=? AND reports.portal_id = portals.id GROUP BY portal_name, portal_address ORDER BY count DESC";

  // Portal info
  private static final String SELECT_PORTAL_INFO = "SELECT * FROM lampje.reports, lampje.portals WHERE portal_name LIKE ? AND reports.portal_id = portals.id LIMIT 1";
  private static final String SELECT_PORTAL_ATTACKERS = "SELECT portal_attacker_name, portal_name, count(portal_attacker_name) as count FROM lampje.reports, lampje.portals WHERE portal_name = ? AND portal_id = portals.id GROUP BY portal_attacker_name ORDER BY count DESC";

  // City info
  private static final String SELECT_CITY_INFO = "SELECT count(DISTINCT report_origin_name) as contributors, count(DISTINCT portal_attacker_name) as attackers, count(*) as attacks FROM lampje.reports, lampje.portals WHERE portals.portal_city like ? AND reports.portal_id = portals.id AND date >= ?";
  private static final String SELECT_ACTIVE_ATTACKERS_INFO = "SELECT count(*) as count, portal_attacker_name FROM lampje.reports, lampje.portals WHERE portals.portal_city like ? AND reports.portal_id = portals.id AND date >= ? GROUP BY portal_attacker_name ORDER BY count DESC LIMIT ?";
  private static final String SELECT_ACTIVE_PORTALS_INFO = "SELECT count(*) as count, portal_name FROM lampje.reports, lampje.portals WHERE portal_city like ? AND reports.portal_id = portals.id AND date >= ? GROUP BY portal_name ORDER BY count DESC LIMIT ?";
  private static final String SELECT_TOP_CITY_PORTALS = "SELECT count(*) as count, portal_name, portal_lon, portal_lat FROM lampje.portals, lampje.reports WHERE portals.id = portal_id AND portal_city like ? AND date > DATE(NOW() - INTERVAL 1 MONTH) GROUP BY portal_name, portal_city, portal_address ORDER BY count DESC LIMIT ?";
  private static final int CITY_INFO_ACTIVITY_LIMIT = 5;

  // DB Info (all-time)
  private static final String GET_PLAYER_COUNT = "SELECT COUNT(*) as count FROM lampje.players";
  private static final String GET_REPORT_COUNT = "SELECT COUNT(*) as count FROM lampje.reports";
  private static final String GET_CONTRIBUTOR_NUM = "SELECT COUNT(DISTINCT report_origin_name) as count FROM lampje.reports";
  private static final String GET_CITY_NUM = "SELECT count(DISTINCT portals.portal_city) as count FROM lampje.reports, lampje.portals WHERE reports.portal_id = portals.id ";
  private static final String GET_PORTAL_NUM = "SELECT count(*) as count FROM lampje.portals";
  private static final String GET_FIRST_REPORT_DATE = "SELECT date FROM lampje.reports ORDER BY date ASC LIMIT 1";

  public static List<Player> getPlayersFrom(final String faction, final String cityToLookup, final int minLevel, final int maxLevel, final boolean searchPrimary, final boolean searchSecondary) throws SQLException {
    final List<Player> players = new ArrayList<>();

    try (final Connection conn = DBManager.createConnection()) {
      try (final PreparedStatement ps = conn.prepareStatement(GET_PLAYERS_FROM_QUERY)) {
        ps.setString(1, faction);
        ps.setInt(2, minLevel);
        ps.setInt(3, maxLevel);

        if (searchPrimary) {
          ps.setString(4, cityToLookup);
        } else {
          ps.setString(4, "");
        }

        if (searchSecondary) {
          ps.setString(5, "%" + cityToLookup + "%");
        } else {
          ps.setString(5, "");
        }

        final ResultSet rs = ps.executeQuery();
        while (rs.next())
          players.add(getPlayer(rs));
      }
    }

    return players;
  }

  public static Player getPlayer(final String name, final boolean full) throws SQLException {
    // Retrieve player from local db
    try (Connection con = DBManager.createConnection()) {
      try (PreparedStatement ps = con.prepareStatement(SELECT_PLAYER_QUERY)) {
        ps.setString(1, name);

        final ResultSet results = ps.executeQuery();

        if (results.next()) {
          final Player player = getPlayer(results);

          if (full) {
            try {
              fetchReports(con, player);
            } catch (final SQLException e) {
              e.printStackTrace();
              // Report, but eat
            }
          }

          return player;
        }
      } catch (final Exception e) {
        // Could not find player.
        return null;
      }
    } catch (final SQLException e) {
      // Connection is bogus.
      throw e;
    }

    return null;
  }

  private static void fetchReports(final Connection con, final Player player) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(SELECT_REPORTS_QUERY)) {
      ps.setString(1, player.getName());

      final ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        final String portalName = rs.getString("portal_name");
        final String portalAddress = rs.getString("portal_address");
        final String portalCity = rs.getString("portal_city");
        final Timestamp date = rs.getTimestamp("date");

        player.addReport(new ActivityReport(player.getName(), portalName, portalAddress, portalCity, new Date(date.getTime())));
      }
    }

    try (PreparedStatement ps = con.prepareStatement(SELECT_OWNED_REPORTS_QUERY)) {
      ps.setString(1, player.getName());

      final ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        final String portalName = rs.getString("portal_name");
        final String portalAddress = rs.getString("portal_address");
        final String portalCity = rs.getString("portal_city");
        final Timestamp date = rs.getTimestamp("date");

        player.addOwnedReport(new ActivityReport(player.getName(), portalName, portalAddress, portalCity, new Date(date.getTime())));
      }
    }
  }

  private static Player getPlayer(final ResultSet results) throws SQLException {
    final String guid = results.getString("guid");
    final String name = results.getString("name");
    final int level = results.getInt("level");
    final String locationPrimary = results.getString("location_primary");
    final String locationSecondary = results.getString("location_secondary");
    final TEAM team = results.getString("faction").equals(TEAM.ENLIGHTENED.name()) ? TEAM.ENLIGHTENED : TEAM.RESISTANCE;
    final int ap = results.getInt("ap");
    final Date sqlDate = results.getDate("last_modified");
    final Date date = sqlDate == null ? null : new Date(sqlDate.getTime());

    return new Player(guid, name, team, level, locationPrimary, locationSecondary, date, ap);
  }

  public static void updatePlayer(final Player player) throws SQLException {
    // TODO return geven als het goed/fout gaat.
    PreparedStatement statement = null;
    final Connection connection = DBManager.createConnection();
    try {
      statement = connection.prepareStatement(UPDATE_PLAYER_QUERY);
      statement.setInt(1, player.getLevel());
      statement.setString(2, player.getLocationPrimary());
      statement.setString(3, player.getLocationSecondary());
      statement.setInt(4, player.getAp());
      statement.setDate(5, new java.sql.Date(new Date().getTime()));
      statement.setDate(6, new java.sql.Date(new Date().getTime()));
      statement.setString(7, player.getName());
      statement.execute();

    } catch (final SQLException e) {
      e.printStackTrace();
    } finally {

      if (statement != null) {
        statement.close();
      }

      if (connection != null) {
        connection.close();
      }
    }
  }

  public static void insertPlayer(final Player player) throws SQLException {
    try (Connection conn = DBManager.createConnection()) {
      try (PreparedStatement ps = conn.prepareStatement(INSERT_PLAYER_BARE_QUERY)) {
        ps.setInt(1, player.getLevel());
        ps.setString(2, player.getName());
        ps.setString(3, player.getTeam().name());
        ps.setDate(4, new java.sql.Date(new Date().getTime()));
        ps.setDate(5, new java.sql.Date(new Date().getTime()));

        ps.execute();
      }
    }
  }

  public static void insertReport(final Connection con, final int portalId, final String fullAddress, final String enemyAgentName, final Date receivedDate, final String agentName) throws SQLException {
    // insertContributor(con, agentName);

    try (PreparedStatement ps = con.prepareStatement(INSERT_DAMAGE_REPORT_QUERY)) {
      ps.setString(1, enemyAgentName);
      ps.setString(2, fullAddress);
      ps.setTimestamp(3, new Timestamp(receivedDate.getTime()));
      ps.setString(4, agentName);
      ps.setObject(5, portalId);

      ps.execute();
    }
  }

  public static int tryGetPortalId(final Connection con, final String fullAddress, final String portalName) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement("SELECT portal_id as id from reports, portals WHERE portal_id = portals.id AND portal_raw_address = ? AND portal_name = ?")) {
      ps.setString(1, fullAddress);
      ps.setString(2, portalName);

      final ResultSet resultSet = ps.executeQuery();

      if (resultSet.next()) {
        return resultSet.getInt("id");
      }
    }

    return -1;
  }

  private static void insertContributor(final Connection con, final String agentName, String email) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(INSERT_CONTRIBUTOR)) {
      ps.setString(1, agentName);
      ps.setString(2, email);

      if (ps.execute()) {
        System.out.println("New contributor added! [" + agentName + "]");
      }
    }
  }

  public static int insertGeoInfo(final Connection con, final String portalName, final GeoInformation geoInfo) throws SQLException {
    if (portalName == null || geoInfo.getAddress() == null || geoInfo.getCity() == null) {
      System.out.println("Unparsable location! > " + portalName + " > " + geoInfo.getCity() + " > " + geoInfo.getAddress());
      return -1;
    }

    try (PreparedStatement selectPs = con.prepareStatement("SELECT * FROM lampje.portals WHERE portal_name = ? AND portal_address = ? AND portal_city = ?")) {
      selectPs.setString(1, portalName);
      selectPs.setString(2, geoInfo.getAddress());
      selectPs.setString(3, geoInfo.getCity());

      final ResultSet executeQuery = selectPs.executeQuery();

      if (!executeQuery.first()) {
        try (PreparedStatement insertPs = con.prepareStatement(INERT_GEO_INFO, Statement.RETURN_GENERATED_KEYS)) {
          insertPs.setString(1, portalName);
          insertPs.setString(2, geoInfo.getCity());
          insertPs.setString(3, geoInfo.getAddress());
          insertPs.setDouble(4, geoInfo.getLon());
          insertPs.setDouble(5, geoInfo.getLat());

          System.out.println("Inserting portal: " + portalName + " > " + geoInfo.getCity());
          insertPs.execute();

          final ResultSet rs = insertPs.getGeneratedKeys();
          if (rs.next()) {
            return rs.getInt(1);
          }
        }
      } else {
        return executeQuery.getInt("id");
      }
    }

    return -1;
  }

  public static PortalReportInfo getPortalInfo(final String portalName) throws SQLException {
    final PortalReportInfo ls = new PortalReportInfo();

    try (final Connection conn = DBManager.createConnection()) {
      try (PreparedStatement ps = conn.prepareStatement(SELECT_PORTAL_INFO)) {
        ps.setString(1, "%" + portalName + "%");

        final ResultSet rs = ps.executeQuery();

        if (rs.first()) {
          ls.setPortalName(rs.getString("portal_name"));
          ls.setPortalCity(rs.getString("portal_city"));
          ls.setPortalAddress(rs.getString("portal_address"));
        } else {
          return null;
        }
      }

      try (final PreparedStatement ps = conn.prepareStatement(SELECT_PORTAL_ATTACKERS)) {
        ps.setString(1, ls.getPortalName());
        final ResultSet rs = ps.executeQuery();

        while (rs.next()) {
          final String attackerName = rs.getString("portal_attacker_name");
          final int count = rs.getInt("count");

          final NameCountInfo info = new NameCountInfo(attackerName, count);

          ls.add(info);
        }
      }
    }

    return ls;
  }

  public static int getMontlyContributorNum() throws SQLException {
    return getCount(SELECT_CONTRIBUTOR_NUM);
  }

  public static int getMontlyCityNum() throws SQLException {
    return getCount(SELECT_CITY_NUM);
  }

  public static int getMontlyAttackerNum() throws SQLException {
    return getCount(SELECT_ATTACKER_NUM);
  }

  public static int getMontlyReportNum() throws SQLException {
    return getCount(SELECT_REPORT_NUM);
  }

  private static int getMontlyPortalNum() throws SQLException {
    return getCount(SELECT_PORTAL_NUM);
  }

  public static int getPlayerNum() throws SQLException {
    return getCount(GET_PLAYER_COUNT);
  }

  public static int getReportNum() throws SQLException {
    return getCount(GET_REPORT_COUNT);
  }

  public static int getContributorNum() throws SQLException {
    return getCount(GET_CONTRIBUTOR_NUM);
  }

  public static int getCityNum() throws SQLException {
    return getCount(GET_CITY_NUM);
  }

  public static int getPortalNum() throws SQLException {
    return getCount(GET_PORTAL_NUM);
  }

  public static Date getFirstReportDate() throws SQLException {
    try (Connection conn = DBManager.createConnection()) {
      try (PreparedStatement ps = conn.prepareStatement(GET_FIRST_REPORT_DATE)) {
        final ResultSet rs = ps.executeQuery();

        if (rs.first()) {
          return new Date(rs.getTimestamp("date").getTime());
        }
      }
    }

    return new Date();
  }

  public static CityInfo getCityInfo(final String city, final Date date) throws SQLException {
    try (Connection conn = DBManager.createConnection()) {
      final CityInfo cityInfo = new CityInfo();

      try (PreparedStatement ps = conn.prepareStatement(SELECT_CITY_INFO)) {
        ps.setString(1, "%" + city + "%");
        ps.setTimestamp(2, new Timestamp(date.getTime()));

        final ResultSet rs = ps.executeQuery();

        if (rs.first()) {
          cityInfo.setAttackers(rs.getInt("attackers"));
          cityInfo.setContributors(rs.getInt("contributors"));
          cityInfo.setAttacks(rs.getInt("attacks"));
        }
      }

      try (PreparedStatement ps = conn.prepareStatement(SELECT_ACTIVE_ATTACKERS_INFO)) {
        ps.setString(1, "%" + city + "%");
        ps.setTimestamp(2, new Timestamp(date.getTime()));
        ps.setInt(3, CITY_INFO_ACTIVITY_LIMIT);

        final ResultSet rs = ps.executeQuery();

        final ArrayList<NameCountInfo> ls = new ArrayList<>();
        while (rs.next()) {
          final String attackerName = rs.getString("portal_attacker_name");
          final int count = rs.getInt("count");

          ls.add(new NameCountInfo(attackerName, count));
        }

        cityInfo.setMostActiveAttackers(ls);
      }

      try (PreparedStatement ps = conn.prepareStatement(SELECT_ACTIVE_PORTALS_INFO)) {
        ps.setString(1, "%" + city + "%");
        ps.setTimestamp(2, new Timestamp(date.getTime()));
        ps.setInt(3, CITY_INFO_ACTIVITY_LIMIT);

        final ResultSet rs = ps.executeQuery();

        final ArrayList<NameCountInfo> ls = new ArrayList<>();
        while (rs.next()) {
          final String attackerName = rs.getString("portal_name");
          final int count = rs.getInt("count");

          ls.add(new NameCountInfo(attackerName, count));
        }

        cityInfo.setMostActivePortals(ls);
      }

      return cityInfo;
    }
  }

  private static int getCount(final String query) throws SQLException {
    try (Connection conn = DBManager.createConnection()) {
      try (PreparedStatement ps = conn.prepareStatement(query)) {
        final ResultSet rs = ps.executeQuery();

        if (rs.first()) {
          return rs.getInt("count");
        }
      }
    }

    return 0;
  }

  public static Player getPlayer(final String nickname) throws SQLException {
    return getPlayer(nickname, false);
  }

  public static PlayerReport getPlayerReports(final String playerName) throws SQLException {
    final PlayerReport playerReport = new PlayerReport(playerName);

    try (Connection con = DBManager.createConnection()) {
      try (PreparedStatement ps = con.prepareStatement(SELECT_PLAYER_REPORTS)) {
        ps.setString(1, playerName);
        final ResultSet rs = ps.executeQuery();

        while (rs.next()) {
          final int count = rs.getInt("count");
          final String city = rs.getString("portal_city");
          final String attacker = rs.getString("portal_attacker_name");

          playerReport.addPlayerEncounter(city, attacker, count);
        }
      }
    }

    try (Connection con = DBManager.createConnection()) {
      try (PreparedStatement ps = con.prepareStatement(SELECT_PLAYER_PORTALS)) {
        ps.setString(1, playerName);
        final ResultSet rs = ps.executeQuery();

        while (rs.next()) {
          final int count = rs.getInt("count");
          final String city = rs.getString("portal_city");
          final String portalName = rs.getString("portal_name");
          final Double lon = rs.getDouble("portal_lon");
          final Double lat = rs.getDouble("portal_lat");

          final Portal p = new Portal(portalName, city, lon, lat);

          playerReport.addPortalEncounter(city, p, count);
        }
      }
    }

    return playerReport;
  }

  public static ArrayList<Portal> getLocationlessPortals(final int limit) throws SQLException {
    final ArrayList<Portal> portals = new ArrayList<>();

    try (Connection con = DBManager.createConnection()) {
      try (PreparedStatement ps = con.prepareStatement("SELECT * FROM lampje.portals WHERE portal_lon IS NULL LIMIT ?")) {
        ps.setInt(1, limit);

        final ResultSet rs = ps.executeQuery();
        while (rs.next()) {
          final String portalName = rs.getString("portal_name");
          final String portalCity = rs.getString("portal_city");
          final String portalAddress = rs.getString("portal_address");

          final Portal p = new Portal(null, portalName, portalCity, portalAddress);
          portals.add(p);
        }
      }
    }

    return portals;
  }

  public static void updateGeoInfo(final Portal p, final GeoInformation geoInfo) throws SQLException {
    try (Connection con = DBManager.createConnection()) {
      try (PreparedStatement ps = con.prepareStatement("UPDATE lampje.portals SET portal_lon=?, portal_lat=? WHERE portal_name=? AND portal_city=? AND portal_address=?")) {
        ps.setDouble(1, geoInfo.getLon());
        ps.setDouble(2, geoInfo.getLat());
        ps.setString(3, p.getName());
        ps.setString(4, p.getCity());
        ps.setString(5, p.getAddress());

        ps.execute();
      }
    }
  }

  public static ArrayList<Contributor> getContributors() throws SQLException {
    final ArrayList<Contributor> ls = new ArrayList<>();

    try (Connection con = DBManager.createConnection()) {
      try (PreparedStatement ps = con.prepareStatement(SELECT_CONTRIBUTORS)) {
        final ResultSet rs = ps.executeQuery();

        while (rs.next()) {
          final String name = rs.getString("agent_name");
          final String email = rs.getString("email");

          ls.add(new Contributor(name, email));
        }
      }
    }

    return ls;
  }

  public static GlobalStatistics getGlobalStats() throws SQLException {
    final int attackerNum = DBRepository.getMontlyAttackerNum();
    final int reportNum = DBRepository.getMontlyReportNum();
    final int cityNum = DBRepository.getMontlyCityNum();
    final int contributorNum = DBRepository.getMontlyContributorNum();
    final int portalNum = DBRepository.getMontlyPortalNum();

    return new GlobalStatistics(attackerNum, reportNum, contributorNum, portalNum, cityNum);
  }

  public static ArrayList<Portal> getTopPortals(final String city, final int limit) throws SQLException {
    final ArrayList<Portal> lst = new ArrayList<>();

    try (Connection con = DBManager.createConnection()) {
      try (PreparedStatement ps = con.prepareStatement(SELECT_TOP_CITY_PORTALS)) {
        ps.setString(1, "%" + city + "%");
        ps.setInt(2, limit);

        final ResultSet resultSet = ps.executeQuery();
        while (resultSet.next()) {
          final Portal p = new Portal(resultSet.getString("portal_name"), resultSet.getDouble("portal_lon"), resultSet.getDouble("portal_lat"));
          lst.add(p);
        }
      }
    }

    return lst;
  }

  public static List<Portal> getPortalsInCity(final String city) throws SQLException {
    final List<Portal> portals = new ArrayList<>();

    try (Connection con = DBManager.createConnection()) {
      try (PreparedStatement ps = con.prepareStatement(SELECT_CITY_PORTALS)) {
        ps.setString(1, city);

        final ResultSet rs = ps.executeQuery();

        while (rs.next()) {
          portals.add(new Portal(rs.getString("portal_name"), rs.getDouble("portal_lon"), rs.getDouble("portal_lat")));
        }
      }
    }

    return portals;
  }

  public static List<Portal> getPortalsInCityForPlayer(final String playerName, final String city) throws SQLException {
    final List<Portal> portals = new ArrayList<>();

    try (Connection con = DBManager.createConnection()) {
      try (PreparedStatement ps = con.prepareStatement(SELECT_PLAYER_CITY_PORTALS)) {
        ps.setString(1, playerName);
        ps.setString(2, playerName);
        ps.setString(3, city);

        final ResultSet rs = ps.executeQuery();

        while (rs.next()) {
          portals.add(new Portal(rs.getString("portal_name"), rs.getDouble("portal_lon"), rs.getDouble("portal_lat")));
        }
      }
    }

    return portals;
  }

  public static Contributor getContributorByEmail(final String email) throws SQLException {
    try (Connection con = DBManager.createConnection()) {
      try (PreparedStatement ps = con.prepareStatement("SELECT agent_name FROM lampje.contributors WHERE email=?")) {
        ps.setString(1, email);

        final ResultSet rs = ps.executeQuery();

        if (rs.first()) {
          return new Contributor(rs.getString("agent_name"), email);
        }
      }
    }

    return null;
  }

  public static void insertContributorByName(String name) throws SQLException {
    try (final Connection conn = DBManager.createConnection()) {
      insertContributor(conn, name, null);
    }
  }

  public static void insertContributorByEmail(String email) throws SQLException {
    try (final Connection conn = DBManager.createConnection()) {
      insertContributor(conn, null, email);
    }
  }
}
