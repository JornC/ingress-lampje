package nl.jorncruijsen.ingress.lampje.parser;

import java.io.IOException;
import java.io.InputStream;

import nl.jorncruijsen.ingress.lampje.domain.geo.Bounds;
import nl.jorncruijsen.ingress.lampje.domain.geo.GeoInformation;
import nl.jorncruijsen.ingress.lampje.util.StreamUtil;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;

public class GeoParser {
  public static GeoInformation parseGeoInfo(final InputStream is) {
    try {
      final JsonNode tree = JsonParser.mapper.readTree(is);

      if("ZERO_RESULTS".equals(tree.get("status").getTextValue())) {
        System.out.println("No-result location.");
        return null;
      }

      final JsonNode resultNode = tree.get("results");

      // TODO Let the exception handler take care of this?
      // If for whatever reason the results node doesn't exist, return null and log it.
      if (resultNode == null) {
        System.out.println("'results' node doesn't exist: " + StreamUtil.getString(is));
        return null;
      }

      // We're only gonna care about the first result
      final JsonNode firstResult = resultNode.get(0);

      double lon = -1;
      double lat = -1;

      JsonNode jsonLocation = null;
      try {
        jsonLocation = firstResult.get("geometry").get("location");
      } catch(final NullPointerException e) {
        throw new IllegalStateException(e);
      }

      if(jsonLocation != null) {
        lon = jsonLocation.get("lng").getDoubleValue();
        lat = jsonLocation.get("lat").getDoubleValue();
      }

      // Get the bounds, it'll be either in the bounds or the viewport element
      JsonNode jsonBounds = firstResult.get("geometry").get("bounds");
      if (jsonBounds == null) {
        jsonBounds = firstResult.get("geometry").get("viewport");
      }

      final JsonNode addressComponents = firstResult.get("address_components");
      final String city = findComponent(addressComponents, "locality");
      final String streetName = findComponent(addressComponents, "route");
      final String streetNumber = findComponent(addressComponents, "street_number");

      // Get the NE and SW properties
      final JsonNode northEast = jsonBounds.get("northeast");
      final JsonNode southWest = jsonBounds.get("southwest");

      // Get GeoInformation properties
      final Bounds bounds = new Bounds(southWest.get("lng").getDoubleValue(), northEast.get("lng").getDoubleValue(), northEast.get("lat").getDoubleValue(), southWest.get("lat").getDoubleValue());
      final String name = firstResult.get("formatted_address").getTextValue();

      // Return the result
      return new GeoInformation(bounds, name, city, streetName + (streetNumber == null ? "" : " " + streetNumber), lon, lat);
    } catch (final JsonProcessingException e) {
      System.out.println("Error parsing.. " + StreamUtil.getString(is));
    } catch (final IOException e) {
      e.printStackTrace();
    } catch (final IllegalStateException e) {
      throw e;
    } catch (final Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  private static String findComponent(final JsonNode addressComponents, final String type) {
    for(int i = 0; i < addressComponents.size();i++) {
      final JsonNode jsonNode = addressComponents.get(i);
      if(nodeContains(jsonNode.get("types"), type)) {
        return jsonNode.get("short_name").asText();
      }
    }

    return null;
  }

  private static boolean nodeContains(final JsonNode jsonNode, final String type) {
    for(int i = 0; i < jsonNode.size();i++) {
      if(jsonNode.get(i).asText().equals(type)) {
        return true;
      }
    }

    return false;
  }
}
