package nl.jorncruijsen.ingress.lampje.retriever;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

public class URLRetriever {

  // Context to store cookies
  private final HttpContext localContext;

  public URLRetriever() {
    localContext = new BasicHttpContext();
  }

  public InputStream postContent(final String uri, final String payloadString) {
    final HttpPost httpPost = new HttpPost(uri);

    try {
      httpPost.setHeader("Accept", "*/*");
      httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
      httpPost.setEntity(new StringEntity(payloadString));
    } catch (final UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    return getResponseBody(httpPost);
  }

  public InputStream getContent(final String uri) {
    return getResponseBody(new HttpGet(uri));
  }

  private HttpResponse executeRequest(final HttpUriRequest request) {
    try {
      final HttpResponse response = new DefaultHttpClient().execute(request, localContext);

      return response;
    } catch (final Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  private InputStream getResponseBody(final HttpUriRequest request) {
    try {
      final HttpResponse executeRequest = executeRequest(request);
      final InputStream content = executeRequest.getEntity().getContent();

      return content;
    } catch (final Exception e) {
      e.printStackTrace();
    }

    return null;
  }
}
