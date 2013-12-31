package nl.jorncruijsen.ingress.lampje.parser;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.jackson.JsonNode;

public class URLShortenerParser {

  public static String getShortUrl(final InputStream postContent) {
    try {
      final JsonNode tree = JsonParser.mapper.readTree(postContent);

      return tree.get("id").getTextValue();
    } catch (final IOException e) {
      return "I mucked up terribly!";
    }
  }
}
