package me.jaminbagel.mymhs.api;

import static me.jaminbagel.mymhs.api.APIUtil.respond;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import me.jaminbagel.mymhs.Main;
import me.jaminbagel.mymhs.api.APIUtil.HttpMethod;
import me.jaminbagel.mymhs.api.APIUtil.ResponseType;
import me.jaminbagel.mymhs.exception.LoggedOutException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ben on 1/27/20 @ 8:38 AM
 */
public abstract class Endpoint extends HttpServlet {

  /*
  Endpoint config
   */

  public static final String STUDENT_ID_PARAM = "student";
  public static final String SESSION_ID_PARAM = "sid";

  public HttpMethod getAllowedMethod() {
    return HttpMethod.GET;
  }

  public boolean requiresBody() {
    return false;
  }

  public ConcurrentHashMap<String, Pattern> getRequiredParameters() {
    return null;
  }

  /*
  Method handlers
   */

  public void handleGet(HttpServletRequest req, HttpServletResponse resp)
      throws LoggedOutException, IOException {

  }

  public void handlePost(HttpServletRequest req, HttpServletResponse resp, JSONObject body)
      throws IOException {

  }

  /*
  Wrapper
   */

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) {
    if (req.getMethod().equals(getAllowedMethod().name())) {
      // If needed, validate session ID
      ConcurrentHashMap<String, Pattern> requiredParams = getRequiredParameters();
      if (requiredParams != null) {
        for (Entry<String, Pattern> param : requiredParams.entrySet()) {
          String providedValue = req.getParameter(param.getKey());
          if (providedValue == null || providedValue.isEmpty()) {
            respond(ResponseType.MISSING_PARAM, resp,
                "Missing parameter, '" + param.getKey() + "'");
            return;
          } else if (!param.getValue().matcher(providedValue).find()) {
            respond(ResponseType.INVALID_PARAM, resp,
                "Invalid value for parameter, '" + param.getKey() + "'");
          }
        }
      }

      // Call handler
      try {
        if (getAllowedMethod() == HttpMethod.GET) {
          this.handleGet(req, resp);
        } else if (getAllowedMethod() == HttpMethod.POST) {
          String body = req.getReader().lines().collect(Collectors.joining());
          req.getReader().close();
          // If JSON body is required but not provided
          if (requiresBody()) {
            if (body.isEmpty()) {
              respond(ResponseType.INVALID_REQ_BODY, resp);
              return;
            }
            try {
              JSONObject jsonBody = new JSONObject(body);
              handlePost(req, resp, jsonBody);
            } catch (JSONException e) {
              respond(ResponseType.INVALID_REQ_BODY, resp);
              return;
            }
          }
          // Don't parse body if not needed
          handlePost(req, resp, null);
        }
      } catch (IOException e) {
        Main.logger.error("IOException on body reader or handler method", e);
        respond(ResponseType.ERROR, resp);
      } catch (LoggedOutException e) {
        // In case Genesis returns a log-in page unexpectedly
        respond(ResponseType.INVALID_SESSION, resp);
      }
    } else {
      respond(ResponseType.INVALID_METHOD, resp);
    }
  }
}
