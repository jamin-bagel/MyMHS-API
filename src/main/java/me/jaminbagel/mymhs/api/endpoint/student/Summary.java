package me.jaminbagel.mymhs.api.endpoint.student;

import static me.jaminbagel.mymhs.api.APIUtil.respond;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import me.jaminbagel.mymhs.api.APIUtil.ResponseType;
import me.jaminbagel.mymhs.exception.LoggedOutException;
import me.jaminbagel.mymhs.fetch.GenesisURL;
import me.jaminbagel.mymhs.fetch.GenesisURL.Path;
import me.jaminbagel.mymhs.fetch.GenesisUtil;
import me.jaminbagel.mymhs.fetch.Request.Builder;
import me.jaminbagel.mymhs.fetch.Response;
import me.jaminbagel.mymhs.parse.SummaryParser;
import org.json.JSONObject;

/**
 * Created by Ben on 1/14/20 @ 10:03 PM
 */
public class Summary extends HttpServlet {

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
      String studentId = req.getParameter("student");
      if (GenesisUtil.validateSessionId(sessionId) && studentId != null) {
        Response response = new
            Builder(GenesisURL.get(Path.STUDENT_SUMMARY, studentId))
            .setHeader("Cookie", "JSESSIONID=" + sessionId)
            .construct()
            .execute();

        JSONObject parsedPage = new SummaryParser(response.getBody()).parse();
        if (parsedPage == null) {
          // Parser failed completely
          respond(ResponseType.ERROR, resp);
          return;
        }
        respond(ResponseType.SUCCESS, resp, parsedPage);
      }
      respond(ResponseType.BAD_INPUT, resp, "Invalid session ID or student ID");
    } catch (IOException e) {
      e.printStackTrace();
      respond(ResponseType.ERROR, resp,
          "Encountered error while fetching student ID: " + e.getClass().getName());
    } catch (LoggedOutException e) {
      respond(ResponseType.INVALID_SESSION, resp);
    }
  }
}
