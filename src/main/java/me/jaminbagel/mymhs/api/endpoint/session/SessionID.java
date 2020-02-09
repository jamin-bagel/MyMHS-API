package me.jaminbagel.mymhs.api.endpoint.session;

import static me.jaminbagel.mymhs.api.APIUtil.respond;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import me.jaminbagel.mymhs.Main;
import me.jaminbagel.mymhs.api.APIUtil.HttpMethod;
import me.jaminbagel.mymhs.api.APIUtil.ResponseType;
import me.jaminbagel.mymhs.api.Endpoint;
import me.jaminbagel.mymhs.exception.InvalidServerResponseException;
import me.jaminbagel.mymhs.util.AuthUtil;
import org.json.JSONObject;

/**
 * Created by Ben on 12/26/19 @ 11:37 PM
 */
public class SessionID extends Endpoint {

  @Override
  public HttpMethod getAllowedMethod() {
    return HttpMethod.POST;
  }

  @Override
  public void handlePost(HttpServletRequest req, HttpServletResponse resp, JSONObject body)
      throws IOException {
    try {
      respond(ResponseType.SUCCESS, resp, AuthUtil.getNewSessionId());
    } catch (InvalidServerResponseException e) {
      // Genesis didn't send us a SID, or we couldn't parse it
      Main.logger.error("Failed to generate SID", e);
      respond(ResponseType.ERROR, resp, "Unable to generate SID");
    }
  }
}
