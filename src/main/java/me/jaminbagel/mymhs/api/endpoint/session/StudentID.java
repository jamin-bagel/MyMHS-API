package me.jaminbagel.mymhs.api.endpoint.session;

import static me.jaminbagel.mymhs.api.APIUtil.respond;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import me.jaminbagel.mymhs.api.APIUtil.ResponseType;
import me.jaminbagel.mymhs.api.GenesisUtil;
import org.json.JSONObject;

/**
 * Created by Ben on 1/14/20 @ 9:38 PM
 */
public class StudentID extends HttpServlet {

  // TODO: 1/31/20 Extent endpoint
  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) {
    if (req.getMethod().equals("GET")) {
      this.doGet(req, resp);
    } else {
      respond(ResponseType.INVALID_METHOD, resp);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    try {
      String sessionId = req.getParameter("sid");

      String studentId = GenesisUtil.getSessionStudentId(sessionId);
      if (studentId != null) {
        respond(ResponseType.SUCCESS, resp, new JSONObject().put("id", studentId));
      } else {
        respond(ResponseType.INVALID_SESSION, resp);
      }
    } catch (IOException e) {
      e.printStackTrace();
      respond(ResponseType.ERROR, resp,
          "Encountered error while fetching student ID: " + e.getClass().getName());
    }
  }
}
