package nl.jorncruijsen.ingress.lampje.retriever;

import java.io.IOException;
import java.io.InputStream;

import nl.jorncruijsen.ingress.lampje.domain.geo.GeoInformation;
import nl.jorncruijsen.ingress.lampje.parser.GeoParser;
import nl.jorncruijsen.ingress.lampje.util.FormatUtil;

public class GeoRetriever {
  private static final String GEO_CODER_URL_FORMAT = "http://maps.googleapis.com/maps/api/geocode/json?address=%s&sensor=false&region=nl";

  public static GeoRetriever I;

  private final URLRetriever urlRetriever;

  public GeoRetriever(final URLRetriever urlRetriever) {
    this.urlRetriever = urlRetriever;
  }

  public GeoInformation getGeoInfoForQuery(final String query) {
    try(final InputStream is = urlRetriever.getContent(String.format(GEO_CODER_URL_FORMAT, FormatUtil.formatGoogleQuery(query)))) {
      return GeoParser.parseGeoInfo(is);
    } catch (final IOException e) {
      e.printStackTrace();
      System.out.println("Could not close inputstream");
    } catch(final IllegalStateException e) {
      // Soft error if query to begin with is 'weird'.
      if(!query.contains(",")) {
        return null;
      } else {
        throw e;
      }
    }

    return null;
  }

  public static void create(final URLRetriever urlRetriever) {
    I = new GeoRetriever(urlRetriever);
  }

  public static void create() {
    create(new URLRetriever());
  }
}
