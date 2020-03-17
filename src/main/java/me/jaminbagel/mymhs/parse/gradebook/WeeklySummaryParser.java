package me.jaminbagel.mymhs.parse.gradebook;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.jaminbagel.mymhs.exception.LoggedOutException;
import me.jaminbagel.mymhs.parse.Parser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Element;

/**
 * Created by Ben on 2/1/20 @ 5:02 PM
 */
public class WeeklySummaryParser extends Parser {

  private static final Pattern COURSE_ID_PATTERN = Pattern
      .compile("goToCourseSummary\\('(\\d+)','(\\d+)");

  @SuppressWarnings("unused")
  public WeeklySummaryParser(String html) throws LoggedOutException {
    super(html);
  }

  @Override
  public JSONObject parse() {
    JSONObject result = new JSONObject();

    result.put("availableStudents", parseStudentSelector());
    result.put("weekOf", parseDateSelector());
    result.put("markingPeriod", parseMpSelector());
    result.put("courses", parseCourseTable());

    return result;
  }

  /**
   * Parse the grading-period dropdown inside of the course table
   *
   * @return A JSONObject containing all available grading periods as well as the currently selected
   * one
   */
  private JSONObject parseMpSelector() {
    try {
      Element mpField = getDom().getElementsByAttributeValue("name", "fldMarkingPeriod").first();
      if (mpField != null) {
        JSONObject result = new JSONObject();
        result.put("options", new JSONArray());
        for (Element option : mpField.children()) {
          if (option.hasAttr("selected")) {
            result.put("selected", option.text());
          }
          result.getJSONArray("options").put(option.text());
        }
        return result;
      }
    } catch (NullPointerException | IndexOutOfBoundsException e) {
      // Do nothing (explained in doParse())
    }
    return null;
  }

  /**
   * Parse the course table in the center of the page
   *
   * @return A JSONArray of courses and their name, ID/section, teacher, average grade, and # of
   * weekly assignments
   */
  private JSONArray parseCourseTable() {
    try {
      Element courseTable = getDom().selectFirst("table.list");
      if (courseTable != null) {

        courseTable = courseTable.child(0);
        JSONArray courseArray = new JSONArray();

        for (Element row : courseTable.children()) {
          // Ignore non-content rows
          if (!row.hasClass("listrowodd") && !row.hasClass("listroweven")) {
            continue;
          }
          JSONObject course = new JSONObject();
          JSONArray assignmentsPerDay = new JSONArray();
          for (int i = 0; i < row.childNodeSize(); i++) {
            Element cell = row.child(i);
            switch (i) {
              // Course name cell
              case 0:
                course.put("name", cell.text());
                break;

              // Teacher cell
              case 1:
                JSONObject teacher = new JSONObject();
                teacher.put("name", cell.ownText());
                teacher.put("email", cell.getElementsByTag("a").first()
                    .attr("href").replace("mailto:", ""));
                course.put("teacher", teacher);
                break;

              // Grade average cell
              case 2:
                // Average grade
                String averageStr = cell.text().replaceFirst("%.*", "");
                course.put("average", !averageStr.isEmpty() ? Float.parseFloat(averageStr)
                    : 0.0f); // TODO: 3/17/20 Check before parse (can say "No Grades")

                // Course ID & section
                Matcher onClickMatcher = COURSE_ID_PATTERN.matcher(cell.attr("onclick"));
                if (onClickMatcher.find()) {
                  course.put("id", onClickMatcher.group(1));
                  course.put("section", onClickMatcher.group(2));
                }
                break;

              // Assignments-by-day cells
              case 3:
              case 4:
              case 5:
              case 6:
              case 7:
                assignmentsPerDay.put(Integer.parseInt(cell.text()));
                break;
            }
          }
          course.put("assignmentsPerDay", assignmentsPerDay);
          courseArray.put(course);
        }
        return courseArray;
      }
    } catch (NullPointerException | IndexOutOfBoundsException e) {
      // Do nothing (explained in doParse())
    }
    return null;
  }
}
