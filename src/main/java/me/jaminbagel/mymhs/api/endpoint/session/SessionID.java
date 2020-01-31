package me.jaminbagel.mymhs.api.endpoint.session;

import static me.jaminbagel.mymhs.api.APIUtil.respond;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import me.jaminbagel.mymhs.api.APIUtil.ResponseType;
import me.jaminbagel.mymhs.api.GenesisUtil;
import me.jaminbagel.mymhs.exception.InvalidServerResponseException;

/**
 * Created by Ben on 12/26/19 @ 11:37 PM
 */
public class SessionID extends HttpServlet {

  // TODO: 1/31/20 Extent endpoint
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
      respond(ResponseType.SUCCESS, resp, GenesisUtil.getNewSessionId());
    } catch (InvalidServerResponseException | IOException e) {
      e.printStackTrace();
      respond(ResponseType.ERROR, resp,
          "Encountered error while fetching new session ID: " + e.getClass().getName());
    }
  }
}
