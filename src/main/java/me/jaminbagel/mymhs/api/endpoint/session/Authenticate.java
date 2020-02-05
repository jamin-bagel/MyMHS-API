package me.jaminbagel.mymhs.api.endpoint.session;

import static me.jaminbagel.mymhs.api.APIUtil.respond;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import me.jaminbagel.mymhs.api.APIUtil;
import me.jaminbagel.mymhs.api.APIUtil.HttpMethod;
import me.jaminbagel.mymhs.api.APIUtil.ResponseType;
import me.jaminbagel.mymhs.api.Endpoint;
import me.jaminbagel.mymhs.api.GenesisUtil;
import me.jaminbagel.mymhs.api.Message;
import me.jaminbagel.mymhs.exception.InvalidServerResponseException;
import org.json.JSONObject;

/**
 * Created by Ben on 1/13/20 @ 9:21 PM
 */
public class Authenticate extends Endpoint {

  private static final ConcurrentHashMap<String, Pattern> requiredParams = new ConcurrentHashMap<String, Pattern>() {{
    put(SESSION_ID_PARAM, GenesisUtil.SESSION_ID_PATTERN);
  }};

  @Override
  public HttpMethod getAllowedMethod() {
    return HttpMethod.POST;
  }

  @Override
  public boolean requiresBody() {
    return true;
  }

  @Override
  public ConcurrentHashMap<String, Pattern> getRequiredParameters() {
    return requiredParams;
  }

  @Override
  public void handlePost(HttpServletRequest req, HttpServletResponse resp, JSONObject body)
      throws IOException {
    try {
      boolean successfulAuth = GenesisUtil
          .authenticateSession(body.getString("u"), body.getString("p"),
              req.getParameter("sid"));
      if (successfulAuth) {
        APIUtil.respond(ResponseType.SUCCESS, resp, Message.LOGIN_SUCCESS);
        return;
      }
      // Invalid username/password
      respond(ResponseType.ERROR, resp, Message.LOGIN_INVALID_CREDENTIALS);
    } catch (InvalidServerResponseException e) {
      // If server offers a new session ID cookie, the provided one is invalid
      respond(ResponseType.INVALID_SESSION, resp, Message.LOGIN_BAD_SESSION_ID);
    }
  }
}
