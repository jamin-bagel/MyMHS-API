package me.jaminbagel.mymhs.parse.gradebook;

import java.util.ArrayList;
import me.jaminbagel.mymhs.exception.LoggedOutException;
import me.jaminbagel.mymhs.parse.Parser;
import me.jaminbagel.mymhs.util.GradeUtil;
import me.jaminbagel.mymhs.util.GradeUtil.GradeStatus;
import me.jaminbagel.mymhs.util.GradeUtil.WeekDay;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.jsoup.nodes.Element;

/**
 * Created by Ben on 2/5/20 @ 2:20 PM
 */
public class AssignmentListParser extends Parser {

  @SuppressWarnings("unused")
  public AssignmentListParser(String html) throws LoggedOutException {
    super(html);
  }

  @Override
  public JSONObject parse() {
    JSONObject result = new JSONObject();

    result.put("availableStudents", parseStudentSelector());
    result.put("weekOf", parseDateSelector());
    result.put("courses", parseCourseSelector());
    result.put("assignments", parseAssignmentList());

    return result;
  }

  @Nullable
  private ArrayList<JSONObject> parseCourseSelector() {
    Element courseSelector = getDom().getElementById("fldCourse");
    if (courseSelector != null) {
      ArrayList<JSONObject> result = new ArrayList<>();
      for (Element option : courseSelector.children()) {
        // Ensure option isn't "All Coureses"
        if (!option.val().isEmpty()) {
          JSONObject course = new JSONObject();
          course.put("title", option.text());

          String[] courseIdSplit = option.val().split(":");
          course.put("id", Integer.parseInt(courseIdSplit[0]));
          course.put("section", Integer.parseInt(courseIdSplit[1]));

          result.add(course);
        }
      }
      return result;
    }
    return null;
  }

  private ArrayList<JSONObject> parseAssignmentList() {
    Element assignmentTable = getDom().selectFirst("table.list");
    if (assignmentTable != null) {

      ArrayList<JSONObject> result = new ArrayList<>();
      assignmentTable = assignmentTable.child(0);

      // Iterate through assignments
      for (int i = 0; i < assignmentTable.childNodeSize(); i++) {
        // Ignore table header
        if (assignmentTable.child(i).hasClass("listheading")) {
          continue;
        }
        JSONObject assignment = new JSONObject();

        // Iterate through cells in this row
        for (int j = 0; j < assignmentTable.child(i).childNodeSize(); j++) {
          Element cell = assignmentTable.child(i).child(j);
          switch (j) {
            // Grading period
            case 0:
              assignment.put("mp", cell.text());
              break;

            // Date due
            case 1:
              // It's possible for no due date to be set
              if (!cell.text().isEmpty()) {
                assignment.put("due", new JSONObject()
                    .put("weekday", WeekDay.valueOf(cell.child(0).text().toUpperCase()).getValue())
                    .put("date", cell.child(1).text()));
              }
              break;

            // Course & teacher
            case 2:
              assignment.put("course", new JSONObject()
                  .put("title", cell.child(0).text())
                  .put("teacher", cell.child(1).text()));
              break;

            // Grading category & description
            case 3:
              assignment.put("category", cell.ownText());
              String description = cell.child(0).child(0).child(0).child(2).text();
              if (!description.isEmpty()) {
                assignment.putOpt("description", description);
              }
              break;

            // Assignment title & comment
            case 4:
              assignment.put("title", cell.child(0).text());
              assignment.put("id",
                  Integer.parseInt(cell.children().last().id().replace("divComments", "")));

              String comment = cell.children().last().child(0).child(0).child(1).text();
              if (!comment.isEmpty()) {
                assignment.put("comment", comment.replaceAll("\"", ""));
              }
              break;

            // Points/grade/status
            case 5:
              assignment.put("grade", parseGradeCell(cell));
              break;
          }
        }
        result.add(assignment);
      }
      return result;
    }
    return null;
  }

  private JSONObject parseGradeCell(Element cell) {
    JSONObject grade = new JSONObject();
    GradeStatus status;

    /*
    Determine the status of the assignment's grade
     */

    if (cell.text().isEmpty()) {
      // Empty cell = an empty grade was posted
      status = GradeStatus.NO_GRADE;

    } else if (cell.text().contains(GradeStatus.UNGRADED.getDesignator())) {
      // Assignment wasn't graded yet
      status = GradeStatus.UNGRADED;

    } else if (cell.text().contains(GradeStatus.MISSING.getDesignator())) {
      // Student did not turn in assignment
      status = GradeStatus.MISSING;

    } else if (cell.text().contains(GradeStatus.INCOMPLETE.getDesignator())) {
      // Student did not complete assignment
      status = GradeStatus.INCOMPLETE;

    } else if (cell.text().contains(GradeStatus.ABSENT.getDesignator())) {
      // Student was not in class on the due date
      status = GradeStatus.ABSENT;

    } else if (cell.text().contains(GradeStatus.EXEMPT.getDesignator())) {
      // Student wasn't required to submit assignment
      status = GradeStatus.EXEMPT;

    } else {
      // Assignment must have a grade inserted
      status = GradeStatus.GRADED;
    }
    grade.put("status", status.getId());

    /*
    Determine other values based on grading status
     */
    if (status == GradeStatus.UNGRADED) {
      float possiblePoints = Float.parseFloat(
          cell.child(0).children().last().text().replace("Assignment Pts: ", "")
      );
      grade.put("possiblePoints", possiblePoints);

    } else if (status != GradeStatus.NO_GRADE) {
      if (cell.ownText().matches(GradeUtil.GRADE_POINTS_REGEX)) {

        // Grade is numerical
        String[] pointsSplit = cell.ownText().split(" ?/ ?");
        grade.put("possiblePoints", pointsSplit[1]);
        if (status == GradeStatus.GRADED) {
          grade.put("earnedPoints", pointsSplit[0]);
        }
      } else {
        // Grade field may contain something like "completed" or "AP" instead of numbers
        grade.put("text", cell.text());
      }
    }

    return grade;
  }
}
