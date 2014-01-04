package nl.jorncruijsen.ingress.lampje;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import nl.jorncruijsen.ingress.lampje.domain.game.Player;
import nl.jorncruijsen.ingress.lampje.domain.game.TEAM;
import nl.jorncruijsen.ingress.lampje.domain.geo.GeoInformation;
import nl.jorncruijsen.ingress.lampje.retriever.GeoRetriever;

import com.sun.mail.imap.IMAPBodyPart;
import com.sun.mail.smtp.SMTPMessage;

/**
 * This is basically a proof-of-concept that's gone live.
 * 
 * Don't mind the code.
 */
public class MailRetriever {
  private static final String smtpHost = "smtp.gmail.com";
  private static final String imapHost = "imap.gmail.com";

  private static Session session;
  private static Store store;
  private static String email;
  private static String pass;

  public static void init(final String... args) throws MessagingException {
    email = args[0];
    pass = args[1];

    final Properties props = System.getProperties();
    props.setProperty("mail.store.protocol", "imaps");
    props.setProperty("mail.smtp.starttls.enable", "true"); // added this line
    props.setProperty("mail.smtp.host", smtpHost);
    props.setProperty("mail.smtp.user", email);
    props.setProperty("mail.smtp.password", pass);
    props.setProperty("mail.smtp.port", "587");
    props.setProperty("mail.smtp.auth", "true");

    session = Session.getDefaultInstance(props, null);
    store = session.getStore("imaps");
    store.connect(imapHost, email, pass);

    System.out.println("Connected to IMAP store.");
  }

  public static boolean goGoGadget() {
    boolean hasErrors = false;

    try {
      // Get the inbox
      final Folder inbox = store.getFolder("Inbox");
      inbox.open(Folder.READ_WRITE);

      try (Connection con = DBManager.createConnection()) {
        // Iterate over unread messages
        for (final Message message : inbox.search(new FlagTerm(new Flags(Flag.SEEN), false))) {
          try {
            handleMessage(con, message);
          } catch (final SQLException e) {
            System.out.println("No db connection, skipping.");
            hasErrors = true;
            message.setFlag(Flag.SEEN, false);
            continue;
          } catch (final IllegalStateException e) {
            System.out.println("Skipping.");
            hasErrors = true;
            message.setFlag(Flag.SEEN, false);
            continue;
          } catch (final Exception e) {
            e.printStackTrace();
            System.out.println("Weird errors!");
            hasErrors = true;
            message.setFlag(Flag.SEEN, false);
            continue;
          }

          // Mark it as read.
          message.setFlag(Flag.SEEN, true);
        }
      } catch (final SQLException e1) {
        e1.printStackTrace();
      }

      inbox.close(true);
    } catch (final NoSuchProviderException e) {
      e.printStackTrace();
    } catch (final MessagingException e) {
      e.printStackTrace();
    }

    return hasErrors;
  }

  private static void handleMessage(final Connection con, final Message message) throws MessagingException, IOException, SQLException {
    final String subject = message.getSubject();

    if(subject == null) {
      return;
    }

    if (subject.contains("Forwarding Confirmation") || subject.contains("Bevestiging doorsturen")) {
      parseForwardingConfirmation(message);
    } else if (subject.contains("Ingress Damage Report")) {
      parseEntityDestruction(con, message);
    }
  }

  private static boolean parseEntityDestruction(final Connection con, final Message message) throws MessagingException, IOException, SQLException {
    final String report = findReport(message);

    if(report == null) {
      return false;
    }

    final String subject = message.getSubject();

    final int agentNameIdx = report.indexOf("Agent Name:") + 11;
    final String agentName = report.substring(agentNameIdx, report.indexOf("\r", agentNameIdx));

    final int factionIdx = report.indexOf("Faction:") + 8;
    final String faction = report.substring(factionIdx, report.indexOf("\r", factionIdx));

    final int damageReportIdx = report.indexOf("DAMAGE REPORT") + 13;

    final int portalNameIdx = damageReportIdx + (subject.contains("Fwd:") ? 2 : 8);
    final String portalName = report.substring(portalNameIdx, report.indexOf("\r", portalNameIdx));

    final int portalAddressIdx = report.indexOf("\r", portalNameIdx) + (subject.contains("Fwd:") ? 2 : 4);
    String portalAddress = report.substring(portalAddressIdx, report.indexOf("\r", portalAddressIdx));

    if(portalAddress.contains("<")) {
      // Take the next line (caused by people that are manually forwarding -_-)
      final int actualPortalAddressIdx = report.indexOf("\r", portalAddressIdx) + 2;
      portalAddress = report.substring(actualPortalAddressIdx, report.indexOf("\r", actualPortalAddressIdx));
    }
    if(portalAddress.contains(">")) {
      portalAddress = portalAddress.substring(portalAddress.indexOf(">") + 1);
    }

    portalAddress = portalAddress.trim();

    final int enemyAgentNameIdx = report.indexOf(" destroyed by ") + 14;
    final String enemyAgentName = report.substring(enemyAgentNameIdx, report.indexOf(" ", enemyAgentNameIdx));

    // Get portal id if known portal
    int portalId = DBRepository.tryGetPortalId(con, portalAddress, portalName);

    // If not known portal, try to geocode and insert it
    if(portalId == -1) {
      GeoInformation geoInfo = null;
      try {
        geoInfo = GeoRetriever.I.getGeoInfoForQuery(portalAddress);
      } catch(final IllegalStateException e) {
        System.out.println("GeoCoder over quota. [" + portalAddress + "]");
        throw e;
      }

      if (geoInfo == null) {
        System.out.println("No parsable location (" + portalAddress + ")");
      } else {
        portalId = DBRepository.insertGeoInfo(con, portalName, geoInfo);
        System.out.println("Queried insert for owner:[" + agentName + "] attacker:[" + enemyAgentName + "] portal:[" + portalName + "] address:[" + portalAddress + "] timestamp:[" + SimpleDateFormat.getDateTimeInstance().format( message.getReceivedDate()) + "]");
      }
    } else {
      System.out.println("Quick insert for owner:[" + agentName + "] attacker:[" + enemyAgentName + "] portal:[" + portalName + "]  address:[" + portalAddress + "] timestamp:[" + SimpleDateFormat.getDateTimeInstance().format( message.getReceivedDate()) + "]");
    }

    // Insert report, regardless of the id
    DBRepository.insertReport(con, portalId, portalAddress, enemyAgentName, message.getReceivedDate(), agentName);

    try {
      final Player newPlayer = new Player(enemyAgentName, TEAM.valueOf(faction.toUpperCase()) == TEAM.ENLIGHTENED ? TEAM.RESISTANCE : TEAM.ENLIGHTENED, 0);
      DBRepository.insertPlayer(newPlayer);
      System.out.println("New player inserted: " + newPlayer);
    } catch (final SQLException e) {
      return false;
    }

    return true;
  }

  private static String findReport(final Message message) throws IOException, MessagingException {
    final IMAPBodyPart content = (IMAPBodyPart) ((MimeMultipart) message.getContent()).getBodyPart(0);

    if (content.getContent() instanceof String) {
      return (String) content.getContent();
    } else if (content.getContent() instanceof MimeMultipart) {
      return (String) ((MimeMultipart) content.getContent()).getBodyPart(0).getContent();
    }

    return null;
  }

  private static void parseForwardingConfirmation(final Message message) throws MessagingException {
    System.out.println("Parsing forwarding confirmation.");
    if (message.getFlags().contains(Flags.Flag.ANSWERED)) {
      System.out.println("Already answered");
      return;
    }

    final String subject = message.getSubject();

    final int codeStart = subject.indexOf("(#");
    final int codeEnd = subject.indexOf(")");
    final String confirmationCode = subject.substring(codeStart + 2, codeEnd);

    final String replyEmail = subject.substring(subject.lastIndexOf(" "));

    message.setFlag(Flag.ANSWERED, true);

    System.out.println("Sending confirmation " + confirmationCode + " to " + replyEmail);

    final Message reply = new SMTPMessage(session);
    reply.setRecipient(Message.RecipientType.TO, new InternetAddress(replyEmail));
    reply.setSubject("Lampje forwarding confirmation code");
    reply.setText("Enter this confirmation code in the 'Forwarding and POP/IMAP' settings panel: " + confirmationCode);

    final Transport transport = session.getTransport("smtp");
    transport.connect(smtpHost, email, pass);
    transport.sendMessage(reply, reply.getAllRecipients());
    transport.close();
  }
}
