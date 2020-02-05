package me.jaminbagel.mymhs.api.endpoint.student.gradebook;

import static me.jaminbagel.mymhs.api.APIUtil.respond;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import me.jaminbagel.mymhs.api.APIUtil.ResponseType;
import me.jaminbagel.mymhs.api.Endpoint;
import me.jaminbagel.mymhs.api.GenesisUtil;
import me.jaminbagel.mymhs.exception.LoggedOutException;
import me.jaminbagel.mymhs.fetch.GenesisURL;
import me.jaminbagel.mymhs.fetch.GenesisURL.Path;
import me.jaminbagel.mymhs.fetch.Request.Builder;
import me.jaminbagel.mymhs.fetch.Response;
import me.jaminbagel.mymhs.parse.gradebook.WeeklySummaryParser;
import org.json.JSONObject;

/**
 * Created by Ben on 1/27/20 @ 8:36 AM
 */
public class WeeklySummary extends Endpoint {

  private static final ConcurrentHashMap<String, Pattern> requiredParams = new ConcurrentHashMap<String, Pattern>() {{
    put(STUDENT_ID_PARAM, GenesisUtil.STUDENT_ID_PATTERN);
    put(SESSION_ID_PARAM, GenesisUtil.SESSION_ID_PATTERN);
    put("date", Pattern.compile("^\\d{2}/[0-3]?\\d/\\d{2,4}$"));
    put("mp", Pattern.compile("^[A-Z0-9]{1,5}$"));
  }};

  @Override
  public ConcurrentHashMap<String, Pattern> getRequiredParameters() {
    return requiredParams;
  }

  @Override
  public void handleGet(HttpServletRequest req, HttpServletResponse resp)
      throws LoggedOutException, IOException {
    String sessionId = req.getParameter("sid");
    String studentId = req.getParameter("student");
    Response response = new
        Builder(GenesisURL
        .get(Path.WEEKLY_SUMMARY, studentId, req.getParameter("mp"), req.getParameter("date")))
        .setHeader("Cookie", "JSESSIONID=" + sessionId)
        .construct()
        .execute();

    JSONObject parsedPage = new WeeklySummaryParser(response.getBody()).parse();
    if (parsedPage == null) {
      // All parsing failed
      respond(ResponseType.ERROR, resp);
      return;
    }
    respond(ResponseType.SUCCESS, resp, parsedPage);
  }
}
