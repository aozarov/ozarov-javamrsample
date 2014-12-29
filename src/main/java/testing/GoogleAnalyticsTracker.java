package testing;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class GoogleAnalyticsTracker {

  private static final URL GA_URL_ENDPOINT = getGoogleAnalyticsEndpoint();
  private static final HTTPHeader CONTENT_TYPE_HEADER =
      new HTTPHeader("Content-Type", "application/x-www-form-urlencoded");

  private static URL getGoogleAnalyticsEndpoint() {
    try {
      return new URL("http", "www.google-analytics.com", "/collect");
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean trackEventToGoogleAnalytics(
      String category, String action, String label, String value) throws IOException {
    Map<String, String> map = new HashMap<>();
    map.put("v", "1");                      // Version.
    map.put("tid", "UA-49801701-2");        // Tracking ID / Web property / Property ID
    map.put("cid", "555");                  // Anonymous Client ID.
    map.put("t", "event");                  // Event hit type.
    map.put("ec", encode(category, true)); // Event Category. Required.
    map.put("ea", encode(action, true));   // Event Action. Required.
    map.put("el", encode(label, false));   // Event label.
    map.put("ev", encode(value, false));   // Event value.

    String postData = getPostData(map);

    URLFetchService urlFetchService =
        URLFetchServiceFactory.getURLFetchService();
    HTTPRequest request = new HTTPRequest(GA_URL_ENDPOINT, HTTPMethod.POST);
    request.addHeader(CONTENT_TYPE_HEADER);
    request.setPayload(postData.getBytes());

    HTTPResponse httpResponse = urlFetchService.fetch(request);
    // Return True if the call was successful.
    return httpResponse.getResponseCode() == HttpURLConnection.HTTP_OK;
  }

  private static String getPostData(Map<String, String> map) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      sb.append(entry.getKey());
      sb.append('=');
      sb.append(entry.getValue());
      sb.append('&');
    }
    if (sb.length() > 0) {
      sb.setLength(sb.length() - 1); // Remove the trailing &
    }
    return sb.toString();
  }

  private static String encode(String value, boolean required)
      throws UnsupportedEncodingException {
    if (value == null) {
      if (required) {
        throw new IllegalArgumentException("Null value");
      }
      return "";
    }
    return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
  }
}
