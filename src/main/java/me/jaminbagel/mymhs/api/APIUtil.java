package me.jaminbagel.mymhs.api;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
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
      System.out.println("Failed to respond to request");
      e.printStackTrace();
    }
  }

  public enum HttpMethod {
    GET,
    POST
  }

  public enum ResponseType {
    // Generic
    SUCCESS(true, 200, "Request was successful"),
    INVALID_METHOD(false, 405, "Method not allowed here"),
    // --Commented out by Inspection (1/31/20, 12:23 PM):RATE_LIMIT(false, 429, "Rate limit exceeded. Slow down!"),
    BAD_INPUT(false, 422, "Invalid input"),
    ERROR(false, 500, "An unknown error occurred"),

    // More specific
    INVALID_SESSION(false, 403, "Invalid (or expired) session"),
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
