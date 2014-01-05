package nl.jorncruijsen.ingress.lampje;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import nl.jorncruijsen.ingress.lampje.collections.ValueComparator;
import nl.jorncruijsen.ingress.lampje.domain.game.Portal;
import nl.jorncruijsen.ingress.lampje.domain.report.Contributor;
import nl.jorncruijsen.ingress.lampje.domain.report.GlobalStatistics;
import nl.jorncruijsen.ingress.lampje.domain.report.PlayerReport;
import nl.jorncruijsen.ingress.lampje.util.ReportService;

import org.apache.commons.lang.StringUtils;

import com.sun.mail.smtp.SMTPMessage;
import com.vividsolutions.jts.io.ParseException;

/**
 * FIXME Divide this up. FIXME Kill main() with fire. FIXME Remove all static
 * (except constant) bodies. FIXME Centralize mail transport somewhere. FIXME
 * Centralize mail formatter somewhere. FIXME Refactor {@link PlayerReport} to
 * the bone.
 * 
 * TODO Switch to J8 lambda's. Will decrease loop-related lines of code by maybe
 * 60% in this extreme case.
 */
@SuppressWarnings("unchecked")
public class MailReporter {
  private static final String MESSAGE_SUBJECT = "[Lampje] %s, your personalized monthly ingress activity report. (report #2, December '13)";

  private static final String smtpHost = "smtp.gmail.com";

  private static final String TRANSMISSION_BEGIN_IMAGE = "<img src=\"http://commondatastorage.googleapis.com/ingressemail/common/transmission-begin.gif\">";
  private static final String TRANSMISSION_END_IMAGE = "<img src=\"http://commondatastorage.googleapis.com/ingressemail/common/transmission-end.gif\">";

  private static final String MESSAGE_SALUTATION_FORMAT = "<p style=\"font-size:17px;\">Greetings %s!</h2>" + "<p>This is your monthly personalized ingress activity report. Brought to you by Lampje.</p><p>This report contains all information retrieved in <b>December '13</b>.</p>";
  private static final String MESSAGE_GREETING_FORMAT = "<p>Well. That's all we got so far. Consider this a test-run.</p><p>Be sure to pass along any suggestions you may have.</p><p>See you next month,<br />Lampje</p>";
  private static final String MESSAGE_DISCLAIMER_FORMAT = "<p><i>Replies to this email can and will not be read.</i></p>";

  private static final String SECTION_WIDTH = "620px";
  private static final String BARCHART_WIDTH = "300px";

  private static final int IMAGE_WIDTH = 620;
  private static final int IMAGE_HEIGHT = 300;
  private static final int BARCHART_HEIGHT = 10;
  private static final int BARCHART_NUM = 6;

  private static final String BARCHART_GRAPH_FORMAT = String.format("<div style=\"border:1px solid black;\">%%s</div>", BARCHART_WIDTH);
  private static final String BARCHART_BAR_FORMAT = String.format("<div style=\"padding:10px 0px 0px 0px;width:%s;height:%2$spx;\"><div style=\"width:%%s%%%%;background-color:%%s;height:%2$spx;border:1px solid grey;border-left:none;\" title=\"%%s (%%sx)\"></div><div style=\"margin:5px;\">%%3$s (%%4$sx)</div></div>", BARCHART_WIDTH, BARCHART_HEIGHT);

  private static final String REPORT_BREAK = "<hr />";

  private static final String MESSAGE_BEGIN_FORMAT = String.format("<html><body style=\"display:inline-block;padding:0px 25px;font-family: arial;background-color:rgb(246,246,246);\"><div>%s%s%s", TRANSMISSION_BEGIN_IMAGE, MESSAGE_SALUTATION_FORMAT, REPORT_BREAK);
  private static final String MESSAGE_END_FORMAT = String.format("%s%s%s</div></body></html>", MESSAGE_GREETING_FORMAT, MESSAGE_DISCLAIMER_FORMAT, TRANSMISSION_END_IMAGE);

  private static final String SUMMARY_FORMAT = "<h3>Personal statistics</h3><p>You've been active in %s cities and have encountered %s enemies, those enemies attacked %s unique portals. Lampje received %s reports from you.</p>";
  private static final String NATIONAL_STATS_MESSAGE_FORMAT = "<h3>National statistics</h3><p>This month, Lampje received %s reports from %s different contributors. Those contributors have been active in %s cities and encountered %s different enemies on %s different portals.</p>";

  private static final String CITY_REPORT_FORMAT_START = "<h3>City report for <span style=\"font-size:19px;\">[%s]</span></h3>";
  private static final String CITY_REPORT_SECTION_START = String.format("<div width=\"%s\">", SECTION_WIDTH);
  private static final String CITY_REPORT_SECTION_END = "</div>";
  private static final String CITY_REPORT_FORMAT_END = "<br />";
  private static final String PLAYER_ENCOUNTER_FORMAT = String.format("<div style=\"vertical-align:top;display:inline-block;width:%s;margin-right:20px;\"><p>Enemies encountered: %%s</p>%%s<br/></div>", BARCHART_WIDTH);
  private static final String PORTAL_ENCOUNTER_FORMAT = String.format("<div style=\"vertical-align:top;display:inline-block;width:%s;margin-right:20px;\"><p>Portals encountered: %%s (showing top %s)</p>%%s<br/></div>", BARCHART_WIDTH, BARCHART_NUM + 1);

  private static final String[] COLORS = new String[] { "#8C2D04", "#CC4C02", "#EC7014", "#FE9929", "#FEC44F", "#FEE391", "#FFFFD4", };

  private static final String AREA_COVERAGE_FORMAT = "<div style=\"vertical-align:top;display:inline-block;margin-right:20px;\"><p>Area coverage:</p><img src=\"%s\" alt=\"Area coverage\" /></div>";
  private static final String AREA_IMAGE_URL = String.format("http://maps.googleapis.com/maps/api/staticmap?size=%sx%s&path=weight:3%%%%7Cfillcolor:0x00AA0066%%%%7Ccolor:0xFFFFFF00%%%%7Cenc:%%s&sensor=false", IMAGE_WIDTH, IMAGE_HEIGHT);

  private static final String INFORMATION_DISCLAIMER = "<h3>About the information / Information disclaimer</h3><p>The information we're displaying here is retrieved from the 'Damage Reports' you're forwarding to Lampje. Damage reports are sent to your address when an enemy destroys one or more of your resonators on a portal. Therefore we can only track your activity in areas in which portals have been attacked. 'Live' resonators are invisible to us, and will remain that way until those resonators are destroyed. We also cannot track resonator decay.</p><p>When we receive a damage report, we parse the following data out of it: attacker name, report owner name (you), portal address, portal name, date/time. We then send the address (which is not formatted in a straight-forward way) to a Google Geocoder service, which will give us a formatted result out of which we can parse the city, street address and lon/lat.</p><p>All this, and only this, results in this report.</p>";

  private static final String QUIRKS_DISCLAIMER = "<h3>Quirks / known bugs</h3><p>- The location is WRONG!<br /><i>We're sending the location described in the damage report to a geocoder service, which will give is a formatted address. This address is an approximation of the actual (exact) portal location, which is some cases may be way off (up to a few hundred metres, in farm-land areas and such.). Unfortunately there's nothing we can do about this without sniffing the ingress service, which is against the ToS)</i><p>- Some city reports are filed under '[cityName] railway station'!<br /><i>This is due to the google Geocoder returning a location categorized as Public Transport, which has a slightly different format. We're working on accounting for this in a future report.</i></p><p>- Some images don't appear to be loading!<br /><i>This is a weird Gmail quirk, just refresh the page.</i></p><p>- I can't see an area coverage thingy in some cities!<br /><i>We need at least 3 portals (with accurate locations) to draw a coloured polygon on the map. This is either a portal location issue, or the city report only contains 2 or less portals.</i></p>";

  private static final String DEVELOPMENT_NOTES = "<h3>Development notes / Change log</h3><p>Changes:</p><ul><li>Basic GroupMe support for chat bot</li><li>Maps contain accurate polygon for area coverage</li></ul><p>Future updates:</p><ul><li>Friendly/enemy player area overlap stats</li><li>Nothing much</li></ul>";

  private static final String ADDITIONAL_NOTES = "<h3>Additional notes</h3><p>The more players that are forwarding their ingress damage reports, the better we can correlate activity information and reflect that into reports such as this one. If you would, please pass along the forwarding instruction link to anyone interested: <a href=\"http://yogh.nl/lampje/\">http://yogh.nl/lampje/</a></p>";

  private String email;
  private String pass;

  private Session session;

  private Object nationalReportString;

  public void init(final String... args) throws MessagingException {
    email = args[0];
    pass = args[1];

    final Properties props = System.getProperties();
    props.setProperty("mail.smtp.starttls.enable", "true");
    props.setProperty("mail.smtp.host", smtpHost);
    props.setProperty("mail.smtp.user", email);
    props.setProperty("mail.smtp.password", pass);
    props.setProperty("mail.smtp.port", "587");
    props.setProperty("mail.smtp.auth", "true");

    session = Session.getDefaultInstance(props, null);

    System.out.println("Connected to IMAP store.");
  }

  public synchronized void fetchNationalStats() throws SQLException {
    final GlobalStatistics stats = DBRepository.getGlobalStats();
    nationalReportString = String.format(NATIONAL_STATS_MESSAGE_FORMAT, stats.getReportNum(), stats.getContributorNum(), stats.getCityNum(), stats.getAttackerNum(), stats.getPortalNum());
  }

  public static void main(final String[] args) throws MessagingException, ParseException, IOException, SQLException {
    final MailReporter reporter = new MailReporter();

    reporter.init(args[2], args[3]);

    // Database credentials
    DBManager.init(args[0], args[1]);

    reporter.fetchNationalStats();

    // final ArrayList<Contributor> contributors =
    // DBRepository.getContributors();

    // final Contributor c = DBRepository.getContributorByEmail("");
    // reporter.sendPlayerReport(c);

    // for (final Contributor c : contributors) {
    // if (c.getEmail() == null) {
    // System.out.println("No email for " + c.getName());
    // continue;
    // }
    //
    // reporter.sendPlayerReport(c);
    // }
  }

  public void sendPlayerReport(final Contributor c) throws ParseException, IOException {
    // Do test
    try {
      // Get the mail report data
      final PlayerReport report = DBRepository.getPlayerReports(c.getName());

      // Create the transport
      final Transport transport = session.getTransport("smtp");
      transport.connect(smtpHost, email, pass);

      // Get and configure the mail message
      final Message msg = getPlayerReportMessage(report);

      System.out.println("Mailing to: " + c.getName() + " > " + c.getEmail());

      msg.setRecipient(Message.RecipientType.TO, new InternetAddress(c.getEmail()));
      msg.setFrom(new InternetAddress("no-reply@yogh.nl", "Lampje"));
      msg.setReplyTo(new InternetAddress[] { new InternetAddress("no-reply@yogh.nl") });

      // Send the message
      transport.sendMessage(msg, msg.getAllRecipients());

      // Close the transport
      transport.close();
    } catch (final SQLException e) {
      e.printStackTrace();
    } catch (final MessagingException e) {
      e.printStackTrace();
    }
  }

  private Message getPlayerReportMessage(final PlayerReport report) throws MessagingException, IOException, ParseException {
    final Message msg = new SMTPMessage(session);
    msg.setSubject(String.format(MESSAGE_SUBJECT, report.getPlayerName()));

    final Map<String, Map<String, Integer>> playerEncounters = report.getPlayerEncounters();
    final Map<String, Map<Portal, Integer>> portalEncounters = report.getPortalEncounters();

    final StringBuilder builder = new StringBuilder();
    builder.append(String.format(MESSAGE_BEGIN_FORMAT, report.getPlayerName()));
    builder.append(String.format(SUMMARY_FORMAT, getReportSummary(playerEncounters, portalEncounters)));
    builder.append(nationalReportString);
    builder.append(REPORT_BREAK);

    final ArrayList<MimeBodyPart> images = new ArrayList<MimeBodyPart>();

    for (final String city : playerEncounters.keySet()) {
      final Map<String, Integer> players = playerEncounters.get(city);
      final Map<Portal, Integer> portals = portalEncounters.get(city);

      builder.append(String.format(CITY_REPORT_FORMAT_START, city));
      builder.append(CITY_REPORT_SECTION_START);

      // Barcharts
      builder.append(String.format(PLAYER_ENCOUNTER_FORMAT, players.size(), createDataChart(players, false)));
      builder.append(String.format(PORTAL_ENCOUNTER_FORMAT, portals.size(), createDataChart(portals, true)));

      // Map coverage
      String pathString;
      pathString = ReportService.createAreaCoverageUrl(new ArrayList<Portal>(portals.keySet()), false);
      if (!portals.isEmpty()) {
        builder.append(CITY_REPORT_SECTION_END);
        builder.append(CITY_REPORT_SECTION_START);

        final String imageUrl = String.format(AREA_IMAGE_URL, pathString);

        builder.append(String.format(AREA_COVERAGE_FORMAT, imageUrl));
      }

      // End the city report
      builder.append(CITY_REPORT_SECTION_END);
      builder.append(CITY_REPORT_FORMAT_END);
      builder.append(REPORT_BREAK);
    }

    builder.append(INFORMATION_DISCLAIMER);
    builder.append(REPORT_BREAK);

    builder.append(QUIRKS_DISCLAIMER);
    builder.append(REPORT_BREAK);

    builder.append(DEVELOPMENT_NOTES);
    builder.append(REPORT_BREAK);

    builder.append(ADDITIONAL_NOTES);
    builder.append(REPORT_BREAK);

    builder.append(String.format(MESSAGE_END_FORMAT));

    final MimeMultipart content = new MimeMultipart("related");

    final MimeBodyPart htmlPart = new MimeBodyPart();
    htmlPart.setContent(builder.toString(), "text/html;");

    content.addBodyPart(htmlPart);

    for (final MimeBodyPart image : images) {
      content.addBodyPart(image);
    }

    msg.setContent(content);
    msg.setHeader("MIME-Version", "1.0");
    msg.setHeader("Content-Type", content.getContentType());

    return msg;
  }

  @SuppressWarnings("rawtypes")
  @Deprecated
  private static Object[] getReportSummary(final Map playerEncounters, final Map portalEncounters) {
    return new Object[] { playerEncounters.size(), sumCounter(playerEncounters), sumCounter(portalEncounters), sumInnerCounter(portalEncounters) };
  }

  private static int sumCounter(final Map<String, Map<?, Integer>> map) {
    int sum = 0;
    for (final Map<?, Integer> value : map.values()) {
      sum += value.size();
    }

    return sum;
  }

  private static int sumInnerCounter(final Map<String, Map<?, Integer>> map) {
    int sum = 0;
    for (final Map<?, Integer> value : map.values()) {
      for (final Integer count : value.values()) {
        sum += count;
      }
    }

    return sum;
  }

  @SuppressWarnings("rawtypes")
  private static String createDataChart(final Map data, final boolean limit) {
    final String[] bars = new String[data.size()];

    // Sort the map on its values
    final ValueComparator bvc = new ValueComparator(data);
    final TreeMap sortedData = new TreeMap(bvc);
    sortedData.putAll(data);

    // Store total and max
    int total = 0;
    int max = 0;
    for (final Object entry : data.entrySet()) {
      max = Math.max((Integer) ((Entry) entry).getValue(), max);
      total += (Integer) ((Entry) entry).getValue();
    }

    // Draw the bars (until max or size is reached)
    int i = 0;
    int counter = 0;
    for (final Object entry : sortedData.entrySet()) {
      final int leftOverCount = total - counter;
      // If over barchart bar limit && final bar fits in chart && remainder is
      // more than 1
      if (i >= BARCHART_NUM + (limit ? 1 : 0) && (leftOverCount < max || limit) && sortedData.size() - 1 != i) {
        // Compile the remainder in one last bar
        if (total - counter > 0 && !limit) {
          bars[i] = String.format(BARCHART_BAR_FORMAT, ((double) leftOverCount / total) * ((double) total / max) * 90, getColor(i), "... " + (data.size() - i) + " others", leftOverCount);
        }
        break;
      }

      counter += (Integer) ((Entry) entry).getValue();
      bars[i] = String.format(BARCHART_BAR_FORMAT, ((double) (Integer) ((Entry) entry).getValue() / total) * ((double) total / max) * 90, getColor(i), getNameForKey(((Entry) entry).getKey()), ((Entry) entry).getValue());
      i++;
    }

    return String.format(BARCHART_GRAPH_FORMAT, StringUtils.join(bars));
  }

  private static String getNameForKey(final Object key) {
    if (key instanceof String) {
      return (String) key;
    }
    if (key instanceof Portal) {
      return ((Portal) key).getName();
    }

    return "N/A";
  }

  private static String getColor(final int idx) {
    return COLORS[Math.min(idx, COLORS.length - 1)];
  }
}
