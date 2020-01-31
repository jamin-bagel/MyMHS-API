package me.jaminbagel.mymhs.api;

import static me.jaminbagel.mymhs.api.APIUtil.respond;

import java.io.IOException;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

  public HttpMethod getAllowedMethod() {
    return HttpMethod.GET;
  }

  public boolean requiresSessionId() {
    return false;
  }

  public boolean requiresStudentId() {
    return false;
  }

  public boolean requiresBody() {
    return false;
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
      if (requiresSessionId()) {
        if (!GenesisUtil.validateSessionId(req.getParameter("sid"))) {
          respond(ResponseType.BAD_INPUT, resp, "Invalid/missing session ID");
          return;
        }
      }

      // If needed, validate student ID
      if (requiresStudentId()) {
        if (!GenesisUtil.validateStudentId(req.getParameter("student"))) {
          respond(ResponseType.BAD_INPUT, resp, "Invalid/missing student ID");
          return;
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
        e.printStackTrace();
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
