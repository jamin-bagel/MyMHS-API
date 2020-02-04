package me.jaminbagel.mymhs.fetch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import me.jaminbagel.mymhs.Main;
import org.perf4j.StopWatch;

/**
 * Created by Ben on 12/27/19 @ 12:39 PM
 */
public class Request {

  private final URL url;
  private final RequestMethod method;
  private final ConcurrentHashMap<String, String> headers;
  private final String body;

  private Request(Builder builder) {
    this.url = builder.url;
    this.method = builder.method;
    this.headers = builder.headers;
    this.body = builder.body;
  }

  public Response execute() throws IOException {

    // Prepare request
    HttpURLConnection conn = (HttpURLConnection) this.url.openConnection();
    conn.setRequestMethod(this.method.name());
    conn.setConnectTimeout(5000);
    conn.setUseCaches(true);
    conn.setInstanceFollowRedirects(false);
    if (headers != null) {
      headers.forEach(conn::setRequestProperty);
    }

    Main.logger.debug("Making HTTP request: " + this);
    StopWatch requestTimer = new StopWatch("httpRequest-" + this.url.getHost())
        .setMessage(this.url.getPath());
    requestTimer.start();

    // Write body
    if (this.body != null && !this.body.isEmpty()) {
      conn.setDoOutput(true);
      OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
      out.write(this.body);
      out.close();
    }

    // Read body
    StringBuilder responseBody = new StringBuilder();
    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    reader.lines().forEach(responseBody::append);

    Main.logger.debug(requestTimer.stop());

    conn.disconnect();
    return new Response(conn, responseBody.toString());
  }

  @Override
  public String toString() {
    return "Request {" +
        method + " " + url +
        " | headers=" + headers +
        " | body='" + body + '\'' +
        '}';
  }

  public static class Builder {

    private final URL url;
    private RequestMethod method;
    private ConcurrentHashMap<String, String> headers;
    private String body;

    public Builder(URL url) {
      this.url = url;
      this.method = RequestMethod.GET;
    }

    public Request construct() {
      return new Request(this);
    }

    public Builder method(RequestMethod method) {
      this.method = method;
      return this;
    }

    public Builder setHeader(String header, String value) {
      if (this.headers == null) {
        this.headers = new ConcurrentHashMap<>();
      }
      this.headers.put(header, value);
      return this;
    }

    public Builder body(String body) {
      this.body = body;
      return this;
    }
  }
}
