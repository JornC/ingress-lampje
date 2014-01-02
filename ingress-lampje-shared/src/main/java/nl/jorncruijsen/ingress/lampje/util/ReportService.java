package nl.jorncruijsen.ingress.lampje.util;

import it.rambow.master.javautils.PolylineEncoder;
import it.rambow.master.javautils.Track;
import it.rambow.master.javautils.Trackpoint;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import nl.jorncruijsen.ingress.lampje.domain.game.CityInfo;
import nl.jorncruijsen.ingress.lampje.domain.game.Player;
import nl.jorncruijsen.ingress.lampje.domain.game.Portal;
import nl.jorncruijsen.ingress.lampje.domain.game.PortalReportInfo;
import nl.jorncruijsen.ingress.lampje.domain.game.TEAM;

import org.apache.commons.lang.StringUtils;
import org.opensphere.geometry.algorithm.ConcaveHull;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class ReportService {
  private static final String ACTIVITY_REPORT = "Active players in %s\n"
      + "Enlightened: %s\n"
      + "Resistance: %s";

  private static final String AREA_IMAGE_WITH_MARKERS_URL = "http://maps.googleapis.com/maps/api/staticmap?size=512x512&path=weight:3%%7Cfillcolor:0x00AA0066%%7Ccolor:0xFFFFFF00%%7Cenc:%s&%s&sensor=false";
  private static final String AREA_IMAGE_URL = "http://maps.googleapis.com/maps/api/staticmap?size=512x512&path=weight:3%%7Cfillcolor:0x00AA0066%%7Ccolor:0xFFFFFF00%%7Cenc:%s&sensor=false";

  private static final String SINGLE_PORTAL_MINIMAL = "#%s %s";

  private static final String SINGLE_PLAYER = "%s(L%s) (%sm ago)";



  private static String formatPlayer(final Player p) {
    return String.format(SINGLE_PLAYER, p.getName(), p.getLevel() == -1 ? "?" : p.getLevel(), ((System.currentTimeMillis() - p.getLastSeen().getTime()) / 60 / 1000));
  }

  public static String reportActivePlayers(final String query, final HashSet<Player> players) {
    final ArrayList<String> enl = findPlayers(players, TEAM.ENLIGHTENED);
    final ArrayList<String> res = findPlayers(players, TEAM.RESISTANCE);

    return String.format(ACTIVITY_REPORT, query, enl.isEmpty() ? "None." : StringUtils.join(enl, ", "), res.isEmpty() ? "None." : StringUtils.join(res, ", "));
  }

  private static ArrayList<String> findPlayers(final HashSet<Player> players, final TEAM team) {
    final ArrayList<String> result = new ArrayList<>();

    if(players == null) {
      return result;
    }

    for (final Player p : players) {
      if (p.getTeam() == team) {
        result.add(formatPlayer(p));
      }
    }

    return result;
  }

  private static final String DB_INFO_REPORT = "Database info:"
      + "\nPlayers in database: %s"
      + "\nPortals in database: %s"
      + "\nDamage reports in database: %s (%.0f/day, %.1f/hour)"
      + "\nCities covered with reports: %s"
      + "\nReport forwarding contributors: %s (Thanks!)"
      + "\nAvg. activity per contributor: %s (%.1f/day)"
      + "\nUptime report retriever: %.2f days";

  public static String reportDBInfo(final int playerNum, final int reportNum, final int contributorNum, final int cityNum, final int portalNum, final Date firstReportDate) {
    final long first = firstReportDate.getTime();
    final long now = new Date().getTime();

    final double dayDiff = (now - first) / (float)(1000 * 60 * 60 * 24);
    final double hrDiff = (now - first) / (float)(1000 * 60 * 60);
    return String.format(DB_INFO_REPORT, playerNum, portalNum, reportNum, reportNum / dayDiff, reportNum / hrDiff, cityNum, contributorNum, reportNum / contributorNum, reportNum / contributorNum / dayDiff, dayDiff);
  }

  private static final String PORTAL_INFO_REPORT = "Name: %s"
      + "\nAddress: %s, %s"
      + "\nRecorded attackers: %s";

  private static final String PORTAL_PLAYER_INFO = "%s (%sx)";

  public static String reportPortalInfo(final PortalReportInfo portalInfo) {
    if(portalInfo == null) {
      return "Portal not found.";
    }

    final String[] ls = new String[portalInfo.size()];

    for(int i = 0; i < portalInfo.size(); i++) {
      ls[i] = String.format(PORTAL_PLAYER_INFO, portalInfo.get(i).getAttackerName(), portalInfo.get(i).getCount());
    }

    return String.format(PORTAL_INFO_REPORT, portalInfo.getPortalName(), portalInfo.getPortalAddress(), portalInfo.getPortalCity(), StringUtils.join(ls, ", "));
  }

  private static final String CITY_REPORT = "City info for the past %s days:"
      + "\nQuery: %s"
      + "\nDistinct attackers: %s"
      + "\nRecorded attacks: %s (%.1f/day)"
      + "\nContributors in this area: %s"
      + "\n5 most active enemies: %s"
      + "\n5 most active portals: %s";

  private static final String CITY_SINGLE_ACTIVITY_REPORT = "%s (%sx)";

  private static final String NAVIGATION_FORMAT = "Portal: %s\nNav: https://maps.google.com/maps?daddr=%s&hl=nl";

  private static final String MAP_IMAGE = "http://maps.googleapis.com/maps/api/staticmap?size=512x512&maptype=roadmap&%s&sensor=false";
  private static final String MAP_IMAGE_FINAL = "Top %s portals: %s\n%s";

  private static final String MAP_IMAGE_MARKER = "markers=color:green|%s";
  private static final String MAP_IMAGE_MARKER_LOCATION = "%s,%s";

  private static final String AREA_ACTIVITY_CONCISE = "%s portals tracked in this area.\n%s";

  public static String reportCityInfo(final CityInfo cityInfo) {
    return String.format(CITY_REPORT, cityInfo.getDayDiff(), cityInfo.getCityName(), cityInfo.getAttackers(), cityInfo.getAttacks(), cityInfo.getAttacks() / (float)cityInfo.getDayDiff(), cityInfo.getContributors(), getMostActiveAttackers(cityInfo), getMostActivePortals(cityInfo));
  }

  private static String getMostActivePortals(final CityInfo cityInfo) {
    final String[] ls = new String[cityInfo.getMostActivePortals().size()];

    for(int i = 0; i < cityInfo.getMostActivePortals().size(); i++) {
      ls[i] = String.format(CITY_SINGLE_ACTIVITY_REPORT, cityInfo.getMostActivePortals().get(i).getAttackerName(), cityInfo.getMostActivePortals().get(i).getCount());
    }

    return StringUtils.join(ls, ", ");
  }

  private static String getMostActiveAttackers(final CityInfo cityInfo) {
    final String[] ls = new String[cityInfo.getMostActiveAttackers().size()];

    for(int i = 0; i < cityInfo.getMostActiveAttackers().size(); i++) {
      ls[i] = String.format(CITY_SINGLE_ACTIVITY_REPORT, cityInfo.getMostActiveAttackers().get(i).getAttackerName(), cityInfo.getMostActiveAttackers().get(i).getCount());
    }

    return StringUtils.join(ls, ", ");
  }

  public static String reportNavigationInfo(final PortalReportInfo portalInfo) {
    if(portalInfo == null) {
      return "Portal not found.";
    }

    return String.format(NAVIGATION_FORMAT, portalInfo.getPortalName(), FormatUtil.formatGoogleQuery(portalInfo.getPortalAddress() + " " + portalInfo.getPortalCity()));
  }

  public static String reportPortalImage(final ArrayList<Portal> portalInfo, final String shortUrl) {
    return String.format(MAP_IMAGE_FINAL, portalInfo.size(), composeTopPortals(portalInfo), shortUrl);
  }

  public static String reportPortalImageUrl(final ArrayList<Portal> portalInfo) {
    return String.format(MAP_IMAGE, composeMarkers(portalInfo));
  }

  private static Object composeTopPortals(final ArrayList<Portal> portalInfo) {
    final String[] str = new String[portalInfo.size()];
    for(int i = 0; i < portalInfo.size();i++) {
      str[i] = String.format(SINGLE_PORTAL_MINIMAL, i + 1, portalInfo.get(i).getName());
    }

    return StringUtils.join(str, ", ");
  }

  private static String composeMarkers(final List<Portal> portalInfo) {
    final int size = Math.min(6, portalInfo.size() - 1);

    final String[] str = new String[size];
    for(int i = size - 1; i > 0;i--) {
      final Portal portal = portalInfo.get(i);
      str[i] = String.format(MAP_IMAGE_MARKER_LOCATION, portal.getLat(), portal.getLon());
    }

    return String.format(MAP_IMAGE_MARKER, StringUtils.join(str, "|"));
  }

  public static String createAreaCoverageUrl(final List<Portal> portals, final boolean showMarkers) throws ParseException, UnsupportedEncodingException {
    final Geometry concaveHull = getConcaveHull(portals);

    if (concaveHull == null) {
      return "";
    }

    final Track track = getTrackPoints(concaveHull);

    final HashMap<String, String> dpEncode = new PolylineEncoder().dpEncode(track);

    final String encodedPolygon = URLEncoder.encode(dpEncode.get("encodedPoints"), "UTF-8").replace("%5C%5C", "%5C");

    final String markers = showMarkers ? composeMarkers(portals) : "";

    return getAreaCoverageUrl(encodedPolygon, markers);
  }

  public static String getAreaCoverageUrl(final String encodedPortals, final String markers) {
    final String large = String.format(AREA_IMAGE_WITH_MARKERS_URL, encodedPortals, markers);

    if(markers.isEmpty() || large.length() > 2048) {
      return String.format(AREA_IMAGE_URL, encodedPortals);
    } else {
      return large;
    }
  }

  private static Track getTrackPoints(final Geometry concaveHull) {
    final Track track = new Track();

    for (final Coordinate c : concaveHull.getCoordinates()) {
      track.addTrackpoint(new Trackpoint(c.x, c.y));
    }

    return track;
  }

  private static Geometry getConcaveHull(final Collection<Portal> portals) throws ParseException {
    int total = 0;
    for (final Portal p : portals) {
      // Temp skip
      if (p.getLon() == 0d) {
        continue;
      }

      total++;
    }

    if (total == 0) {
      return null;
    }

    final Geometry[] geoms = new Geometry[total];

    final WKTReader reader = new WKTReader();

    int i = 0;
    for (final Portal p : portals) {
      // Temp skip
      if (p.getLon() == 0d) {
        continue;
      }

      // Point, stored in lat/lon (for reasons that frustrate me beyond
      // reasonable measures)
      geoms[i] = reader.read(String.format("POINT (%s %s)", p.getLat(), p.getLon()));
      i++;
    }

    final GeometryFactory fact = new GeometryFactory(new PrecisionModel(), 4326);
    final GeometryCollection col = new GeometryCollection(geoms, fact);

    final ConcaveHull hull = new ConcaveHull(col, 0.01);
    try {
      return hull.getConcaveHull();
    } catch (final IndexOutOfBoundsException e) {
      return null;
    }
  }

  public static String reportAreaActivityConcise(final Collection<Portal> portals, final String shortUrl) {
    return String.format(AREA_ACTIVITY_CONCISE, portals.size(), shortUrl);
  }
}
