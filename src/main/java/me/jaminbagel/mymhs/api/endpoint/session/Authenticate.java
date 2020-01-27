package me.jaminbagel.mymhs.api.endpoint.session;

import static me.jaminbagel.mymhs.api.APIUtil.respond;

import java.io.IOException;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import me.jaminbagel.mymhs.api.APIUtil;
import me.jaminbagel.mymhs.api.APIUtil.ResponseType;
import me.jaminbagel.mymhs.api.Message;
import me.jaminbagel.mymhs.exception.InvalidServerResponseException;
import me.jaminbagel.mymhs.fetch.GenesisUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ben on 1/13/20 @ 9:21 PM
 */
public class Authenticate extends HttpServlet {

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) {
    if (req.getMethod().equals("POST")) {
      this.doPost(req, resp);
    } else {
      respond(ResponseType.INVALID_METHOD, resp);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    try {
      // Parse and validate body
      String body = req.getReader().lines().collect(Collectors.joining());
      if (body.length() > 1) {
        JSONObject bodyJson = new JSONObject(body);

        // Attempt login and check result
        boolean successfulAuth = GenesisUtil
            .authenticateSession(bodyJson.getString("u"), bodyJson.getString("p"),
                req.getParameter("sid"));
        if (successfulAuth) {
          APIUtil.respond(ResponseType.SUCCESS, resp, Message.LOGIN_SUCCESS);
          return;
        }
        respond(ResponseType.ERROR, resp, Message.LOGIN_INVALID_CREDENTIALS);
      }

      // Bad request body
      respond(ResponseType.BAD_INPUT, resp);
    } catch (IOException e) {
      e.printStackTrace();
      respond(ResponseType.ERROR, resp,
          "Encountered error while fetching new session ID: " + e.getClass().getName());
    } catch (JSONException e) {
      respond(ResponseType.BAD_INPUT, resp);
    } catch (InvalidServerResponseException e) {
      e.printStackTrace();
      respond(ResponseType.INVALID_SESSION, resp);
    }
  }
}
