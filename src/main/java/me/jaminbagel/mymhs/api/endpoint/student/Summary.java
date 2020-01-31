package me.jaminbagel.mymhs.api.endpoint.student;

import static me.jaminbagel.mymhs.api.APIUtil.respond;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import me.jaminbagel.mymhs.api.APIUtil.HttpMethod;
import me.jaminbagel.mymhs.api.APIUtil.ResponseType;
import me.jaminbagel.mymhs.api.Endpoint;
import me.jaminbagel.mymhs.exception.LoggedOutException;
import me.jaminbagel.mymhs.fetch.GenesisURL;
import me.jaminbagel.mymhs.fetch.GenesisURL.Path;
import me.jaminbagel.mymhs.fetch.Request.Builder;
import me.jaminbagel.mymhs.fetch.Response;
import me.jaminbagel.mymhs.parse.SummaryParser;
import org.json.JSONObject;

/**
 * Created by Ben on 1/14/20 @ 10:03 PM
 */
public class Summary extends Endpoint {

  @Override
  public HttpMethod getAllowedMethod() {
    return HttpMethod.GET;
  }

  @Override
  public boolean requiresSessionId() {
    return true;
  }

  @Override
  public boolean requiresStudentId() {
    return true;
  }

  @Override
  public void handleGet(HttpServletRequest req, HttpServletResponse resp)
      throws LoggedOutException, IOException {
    String sessionId = req.getParameter("sid");
    String studentId = req.getParameter("student");
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
}
