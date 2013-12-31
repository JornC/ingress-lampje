package nl.jorncruijsen.ingress.lampje.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

public class StreamUtil {
  public static String getString(final InputStream stream) {
    final StringWriter writer = new StringWriter();

    try {
      IOUtils.copy(stream, writer);
    } catch (final IOException e) {
      e.printStackTrace();
    }


    return writer.toString();
  }
}
