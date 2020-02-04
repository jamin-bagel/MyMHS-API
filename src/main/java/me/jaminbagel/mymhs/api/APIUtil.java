package me.jaminbagel.mymhs.api;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import me.jaminbagel.mymhs.Main;
import org.json.JSONObject;

/**
 * Created by Ben on 1/13/20 @ 5:52 PM
 */
public class APIUtil {

  public static void respond(ResponseType responseType, HttpServletResponse resp) {
    respond(responseType, resp, responseType.defaultPayload);
  }

  public static void respond(ResponseType responseType, HttpServletResponse resp, Object record) {
    resp.setStatus(responseType.httpCode);
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF8");

    // Auto-stringify enums
    if (Enum.class.isAssignableFrom(record.getClass())) {
      record = record.toString();
    }

    try {
      resp.getWriter().print(
          new JSONObject()
              .put("success", responseType.successful)
              .put("payload", record)
              .toString()
      );
      resp.getWriter().close();
    } catch (IOException e) {
      Main.logger.error("Failed to respond to request", e);
    }
  }

  public enum HttpMethod {
    GET,
    POST
  }

  public enum ResponseType {
    // Generic
    SUCCESS(true, 200, "Request was successful"),
    ERROR(false, 500, "An unknown error occurred"),
    // TODO: 2/4/20 Add rate limiting w/ code 429

    // User input errors
    INVALID_SESSION(false, 403, "Invalid (or expired) session"),

    MISSING_PARAM(false, 422, "Missing parameter"),
    INVALID_PARAM(false, 422, "Invalid parameter"),

    INVALID_METHOD(false, 405, "Method not allowed here"),
    INVALID_REQ_BODY(false, 422, "Invalid/missing request body");

    private final boolean successful;
    private final int httpCode;
    private final String defaultPayload;

    ResponseType(boolean successful, int httpCode, String defaultPayload) {
      this.successful = successful;
      this.httpCode = httpCode;
      this.defaultPayload = defaultPayload;
    }
  }
}
