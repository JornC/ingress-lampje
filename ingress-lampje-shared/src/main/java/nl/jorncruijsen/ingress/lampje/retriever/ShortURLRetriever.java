package nl.jorncruijsen.ingress.lampje.retriever;

import java.io.InputStream;

import nl.jorncruijsen.ingress.lampje.parser.URLShortenerParser;

public class ShortURLRetriever {
  private static final String URL_SHORTENER_SERVICE = "https://www.googleapis.com/urlshortener/v1/url";

  private static final String SHORTENER_URL_FORMAT = "{\"longUrl\": \"%s\"}";

  public static ShortURLRetriever I;

  private final URLRetriever urlRetriever;

  public ShortURLRetriever(final URLRetriever urlRetriever) {
    this.urlRetriever = urlRetriever;
  }

  public static void create(final URLRetriever urlRetriever) {
    I = new ShortURLRetriever(urlRetriever);
  }

  public String getShortUrl(final String longUrl) {
    final InputStream postContent = urlRetriever.postContent(URL_SHORTENER_SERVICE, String.format(SHORTENER_URL_FORMAT, longUrl));

    return URLShortenerParser.getShortUrl(postContent);
  }

}
