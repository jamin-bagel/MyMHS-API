package me.jaminbagel.mymhs.parse.gradebook;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.jaminbagel.mymhs.exception.LoggedOutException;
import me.jaminbagel.mymhs.parse.Parser;
import me.jaminbagel.mymhs.util.GradeUtil.GradingMethod;
import me.jaminbagel.mymhs.util.ParseUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by Ben on 2/9/20 @ 9:19 PM
 */
public class CourseSummaryParser extends Parser {

  private static final Pattern MP_INFO_PATTERN = Pattern
      .compile("^(.+) (\\d+/\\d+/\\d+) to (\\d+/\\d+/\\d+)$");
  private static final String LAST_GRADE_INDICATOR = "Last grade posted on ";
  private static final String GRADING_METHOD_INDICATOR = "Grading Information";

  private static final String GRADING_METHOD_SELECTOR =
      "b:contains(" + GRADING_METHOD_INDICATOR + ")";

  public CourseSummaryParser(String html) throws LoggedOutException {
    super(html);
  }

  @Override
  public JSONObject parse() {
    JSONObject result = new JSONObject();

    result.put("availableStudents", parseStudentSelector());
    result.put("courseInfo", parseCourseSelector());

    JSONObject gradingPeriodInfo = parseGradingPeriodInfo();
    if (gradingPeriodInfo != null) {
      result.put("lastGrade", gradingPeriodInfo.remove("lastGrade"));
    }
    result.put("gradingPeriodInfo", gradingPeriodInfo);

    GradingMethod gradingMethod = parseGradingMethod();
    result.put("gradingMethod", gradingMethod);
    if (gradingMethod == GradingMethod.CATEGORY_WEIGHTING) {
      result.put("categoryWeights", parseWeightingCategories());
    }

    // TODO: 2/10/20 Parse weighting categories (if using category weighting)
    return result;
  }

  /**
   * Parse the course selector at the top of the page
   *
   * @return A JSONObject containing an array of course options and the currently selected course
   */
  private JSONObject parseCourseSelector() {
    Element courseSelector = getDom().getElementById("fldCourse");
    if (courseSelector != null) {
      JSONObject courseInfo = new JSONObject();
      JSONArray courses = new JSONArray();

      for (Element option : courseSelector.children()) {
        JSONObject courseId = ParseUtil.courseAndSectionFromString(option.attr("value"));
        // Add the course's name to the array item
        if (courseId != null) {
          courseId.put("name", option.text());
        }
        // Get selected course
        if (option.hasAttr("selected")) {
          courseInfo.put("selected", courseId);
        }
        courses.put(courseId);
      }
      courseInfo.put("options", courses);
      return courseInfo;
    }
    return null;
  }

  /**
   * Parse the text above the assignment list, containing the proper marking period name, start date
   * and end date
   *
   * @return A JSONObject containing an array of course options and the currently selected course
   */
  private JSONObject parseGradingPeriodInfo() {
    Element mainContent = getDom().selectFirst("table.list");
    if (mainContent != null) {

      mainContent = mainContent.child(0);
      JSONObject mpInfo = new JSONObject();

      Element tableHeader = mainContent.child(0).child(0).child(0);
      Matcher gradingPeriodInfoMatcher = MP_INFO_PATTERN.matcher(
          tableHeader.child(1).text()
      );

      // Get the full name of the grading period (ie "Marking Period 2") and the date that it starts and ends
      if (gradingPeriodInfoMatcher.find()) {
        mpInfo.put("name", gradingPeriodInfoMatcher.group(1));
        mpInfo.put("start", gradingPeriodInfoMatcher.group(2));
        mpInfo.put("end", gradingPeriodInfoMatcher.group(3));
      }

      // Not always present, but add the date of the last grade if it's there
      if (tableHeader.childNodeSize() == 2) {
        mpInfo.put("lastGrade",
            tableHeader.child(1).text().replace(LAST_GRADE_INDICATOR, "")
        );
      }
      return mpInfo;
    }
    return null;
  }

  /**
   * Parse the grading method (total points / category weight) used to calculate grades for this
   * course
   *
   * @return The used grading method (UNKNOWN if unreadable or not present)
   */
  private GradingMethod parseGradingMethod() {
    Element gradingInformationTitle = getDom().selectFirst(GRADING_METHOD_SELECTOR);
    if (elementDoesExist(gradingInformationTitle)) {
      // Gets the bold text under the Grading Information section (if-statement because structure varies here for some reason)
      String gradingMethodText = "";
      if (gradingInformationTitle.parent().parent().children().size() == 2) {
        gradingMethodText = gradingInformationTitle.parent().parent().child(1).text();
      } else if (gradingInformationTitle.parent().parent().children().size() == 3) {
        gradingMethodText = gradingInformationTitle.parent().parent().child(1).child(0).text();
      }

      switch (gradingMethodText) {
        case "Total Points":
          return GradingMethod.TOTAL_POINTS;

        case "Category Weighting":
          return GradingMethod.CATEGORY_WEIGHTING;

        default:
          return GradingMethod.UNKNOWN;
      }
    }
    return GradingMethod.UNKNOWN;
  }

  private JSONArray parseWeightingCategories() {
    Elements listTables = getDom().select("table.list");
    Element weightingTable = listTables.get(listTables.size() - 2);
    if (weightingTable != null) {

      weightingTable = weightingTable.child(0);
      JSONArray categoryArray = new JSONArray();

      for (Element row : weightingTable.children()) {
        if (row.hasClass("listheading")) {
          continue;
        }

        categoryArray.put(
            new JSONObject()
                .put("title", row.child(0).text())
                .put("weight", Float.parseFloat(
                    row.child(1).text().replace(" %", "")
                ) / 100)
        );
      }
      return categoryArray;
    }
    return null;
  }
}
