package me.jaminbagel.mymhs.api.endpoint.student.gradebook;

import static me.jaminbagel.mymhs.api.APIUtil.respond;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import me.jaminbagel.mymhs.api.APIUtil.ResponseType;
import me.jaminbagel.mymhs.api.Endpoint;
import me.jaminbagel.mymhs.exception.LoggedOutException;
import me.jaminbagel.mymhs.fetch.GenesisURL.Path;
import me.jaminbagel.mymhs.fetch.Request.Builder;
import me.jaminbagel.mymhs.fetch.Response;
import me.jaminbagel.mymhs.parse.gradebook.CourseSummaryParser;
import me.jaminbagel.mymhs.util.AuthUtil;
import me.jaminbagel.mymhs.util.ParseUtil;
import org.json.JSONObject;

/**
 * Created by Ben on 2/5/20 @ 9:59 AM
 */
public class CourseSummary extends Endpoint {

  private static final ConcurrentHashMap<String, Pattern> requiredParams = new ConcurrentHashMap<String, Pattern>() {{
    put(STUDENT_ID_PARAM, AuthUtil.STUDENT_ID_PATTERN);
    put(SESSION_ID_PARAM, AuthUtil.SESSION_ID_PATTERN);
    put("course", ParseUtil.COURSE_ID_AND_SECTION_PATTERN);
    put("mp", ParseUtil.GRADING_PERIOD_PATTERN);
  }};

  @Override
  public ConcurrentHashMap<String, Pattern> getRequiredParameters() {
    return requiredParams;
  }

  @SuppressWarnings("ConstantConditions") // courseIdInfo can't be null thanks to parameter checks
  @Override
  public void handleGet(HttpServletRequest req, HttpServletResponse resp)
      throws LoggedOutException, IOException {
    String sessionId = req.getParameter("sid");
    String studentId = req.getParameter("student");
    JSONObject courseIdInfo = ParseUtil.courseAndSectionFromString(req.getParameter("course"));
    Response response = new Builder(
        Path.COURSE_SUMMARY
            .format(
                studentId,
                courseIdInfo.getInt("id"),
                courseIdInfo.getInt("section"),
                req.getParameter("mp")
            ))
        .setHeader("Cookie", "JSESSIONID=" + sessionId)
        .construct()
        .execute();

    JSONObject parsedPage = new CourseSummaryParser(response.getBody()).parse();
    if (parsedPage == null) {
      // All parsing failed
      respond(ResponseType.ERROR, resp);
      return;
    }
    respond(ResponseType.SUCCESS, resp, parsedPage);
  }
}
