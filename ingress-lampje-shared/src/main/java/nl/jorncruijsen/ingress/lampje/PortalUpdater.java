package nl.jorncruijsen.ingress.lampje;

import java.sql.SQLException;
import java.util.ArrayList;

import nl.jorncruijsen.ingress.lampje.domain.game.Portal;
import nl.jorncruijsen.ingress.lampje.domain.geo.GeoInformation;
import nl.jorncruijsen.ingress.lampje.retriever.GeoRetriever;
import nl.jorncruijsen.ingress.lampje.retriever.URLRetriever;

public class PortalUpdater {
  private static URLRetriever urlRetriever;

  public static void main(final String[] args) throws SQLException {
    // Database credentials
    DBManager.init(args[0], args[1]);

    urlRetriever = new URLRetriever();
    GeoRetriever.create(urlRetriever);

    final int limit = 500;

    final ArrayList<Portal> locationLessPortals = DBRepository.getLocationlessPortals(limit);

    for(final Portal p : locationLessPortals) {
      System.out.println();
      System.out.println(p);
      final GeoInformation geoInfo = GeoRetriever.I.getGeoInfoForQuery(p.getAddress() + ", " + p.getCity());
      System.out.println(geoInfo);
      if(geoInfo == null) {
        continue;
      }

      DBRepository.updateGeoInfo(p, geoInfo);
    }
  }
}
