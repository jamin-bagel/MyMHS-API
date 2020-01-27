package me.jaminbagel.mymhs.fetch;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Ben on 1/11/20 @ 12:34 PM
 */
public class Response {

  private static Logger logger = LogManager.getLogger("GLOBAL");

  @Getter
  int code;
  @Getter
  String body;
  private ConcurrentHashMap<String, String> headers;

  Response(HttpURLConnection conn, String body) throws IOException {
    this.code = conn.getResponseCode();
    this.body = body;
    if (conn.getHeaderFields() != null && conn.getHeaderFields().size() > 0) {
      this.headers = new ConcurrentHashMap<>();
      for (Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
        if (entry.getKey() == null) {
          continue;
        }
        this.headers.put(
            entry.getKey().toLowerCase(),
            String.join(", ", entry.getValue())
        );
      }
    }
    logger.debug("Received HTTP response: " + this);
  }

  public String getHeader(String header) {
    return headers.get(header.toLowerCase());
  }

  @Override
  public String toString() {
    return "Response {" +
        code +
        " | headers=" + headers +
        " | body='" + body.substring(0, Math.min(body.length(), 20)) + '\'' +
        '}';
  }
}
