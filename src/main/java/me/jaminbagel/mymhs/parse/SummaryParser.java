package me.jaminbagel.mymhs.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.jaminbagel.mymhs.exception.LoggedOutException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Element;

/**
 * Created by Ben on 1/15/20 @ 12:15 PM
 */
public class SummaryParser extends Parser {

  // Matches the title attribute of bus schedule elements when the schedule is set to "WEEKLY"
  private static final Pattern BUS_SCHEDULE_ITEM_PATTERN = Pattern
      .compile("^(\\d{1,2}:\\d{1,2}[A|P]M) - Route: ([^.]+)\\.\\s+ (.+)\\.\\s+$");

  public SummaryParser(String html) throws LoggedOutException {
    super(html);
  }

  @Override
  public JSONObject parse() {
    JSONObject result = new JSONObject();

    // These methods contain their own silent try/catch blocks
    // Due to the dynamic nature of Genesis, varying page layouts are to be expected and if the parser fails, it can do so silently
    result.put("availableStudents", parseStudentSelector());
    result.put("properties", parseStudentCard());
    result.put("important", parseStudentHeader());
    result.put("schedule", parseStudentClassSchedule());
    result.put("busSchedule", parseStudentBusSchedule());
    // NOTE: The weekly attendance / assignment table is not parsed due to more useful sources of the same info

    return result.isEmpty() ? null : result;
  }

  /**
   * Parse the student's info card on the left side of the student summary page
   *
   * @return A JSONObject containing raw key/value pairs from the card
   */
  private JSONObject parseStudentCard() {
    try {
      Element studentCard = getDom().selectFirst("table.list");
      if (studentCard != null) {

        studentCard = studentCard.child(0);
        JSONObject studentProperties = new JSONObject();

        for (Element item : studentCard.children()) {
          // Ensure key/value pair
          if (item.childNodeSize() == 2 || item.child(0).childNodeSize() == 2) {
            String[] pairParts = item.wholeText().split(":");
            studentProperties.put(
                pairParts[0].trim(),
                pairParts[1].trim()
            );
          }
        }
        return studentProperties;
      }
    } catch (NullPointerException | IndexOutOfBoundsException e) {
      // Do nothing
    }
    return null;
  }

  /**
   * Parse the header above the student's schedule for their name, school, grade, ID, and state ID
   *
   * @return A JSONObject containing the student's important (identifying) info
   */
  private JSONObject parseStudentHeader() {
    try {
      Element studentHeader = getDom().select("table.list").get(1);
      if (studentHeader != null) {

        studentHeader = studentHeader.child(0);
        JSONObject studentImportantInfo = new JSONObject();

        // Name / grade
        String firstName = studentHeader.child(0).child(0).child(0).text();
        studentImportantInfo.put(
            "name",
            studentHeader.child(0).child(0).text()
                .replace(firstName, firstName + " ")
        ).put(
            "grade",
            Integer.parseInt(studentHeader.child(0).child(1).children().last().text())
        );

        // School / District ID / State ID
        String[] headerTextSplit = studentHeader.child(1).wholeText().split("  \\|  ");
        studentImportantInfo
            .put("school", headerTextSplit[0])
            .put("id", Integer.parseInt(headerTextSplit[1].split(": ")[1]))
            .put("stateId", Long.parseLong(headerTextSplit[2].split(": ")[1]));

        return studentImportantInfo;
      }
    } catch (NullPointerException | IndexOutOfBoundsException e) {
      // Do nothing
    }
    return null;
  }

  /**
   * Parse the main feature of the summary page: the course list / schedule
   *
   * @return An ordered JSONArray of courses that will be taken by the student sometime during the
   * year
   */
  private JSONArray parseStudentClassSchedule() {
    try {
      Element studentClassSchedule = getDom().select("table.list").get(2);
      if (studentClassSchedule != null) {

        studentClassSchedule = studentClassSchedule.child(0);
        JSONArray scheduleItemArray = new JSONArray();

        // Loop through table rows & cells
        int i = 0;
        for (Element row : studentClassSchedule.children()) {
          if (row.hasClass("listheading")) {
            continue;
          }

          scheduleItemArray.put(new JSONObject());
          int j = 0;
          for (Element cell : row.children()) {
            switch (j) {
              case 0:
                scheduleItemArray.getJSONObject(i).put("period", cell.text());
                break;
              case 1:
                scheduleItemArray.getJSONObject(i).put("title", cell.text());
                break;
              case 2:
                scheduleItemArray.getJSONObject(i).put("when", cell.text());
                break;
              case 3:
                scheduleItemArray.getJSONObject(i).put("days", cell.text());
                break;
              case 4:
                scheduleItemArray.getJSONObject(i).put("room", cell.text());
                break;
              case 5:
                scheduleItemArray.getJSONObject(i).put("teacher", cell.text());
                break;
            }
            j++;
          }
          i++;
        }
        return scheduleItemArray;
      }
    } catch (NullPointerException | IndexOutOfBoundsException e) {
      // Do nothing (explained in doParse())
    }
    return null;
  }

  /**
   * Parse the student's bus schedule at the bottom of the page. NOTE: Only AM/PM pickup times are
   * expected, but this method allows for others as a failsafe
   *
   * @return A JSONArray of the student's bus pickup/dropoff times for today (or the nearest
   * weekday)
   */
  private JSONObject parseStudentBusSchedule() {
    try {
      Element studentBusSchedule = getDom().select("table.list").get(4);
      if (studentBusSchedule != null) {

        studentBusSchedule = studentBusSchedule.child(0);
        JSONObject busSchedule = new JSONObject();

        // Loop through schedules (usually just AM/PM, but accepts others as a fail-safe)
        for (int i = 2; i < studentBusSchedule.childNodeSize(); i++) {
          JSONArray scheduleArray = new JSONArray();

          // Loop through weekdays
          for (int j = 1; j < studentBusSchedule.child(i).childNodeSize(); j++) {

            String scheduleTitle = studentBusSchedule.child(i).child(j).attr("title");
            Matcher scheduleItemMatcher = BUS_SCHEDULE_ITEM_PATTERN.matcher(scheduleTitle);

            // Ensure that the cell's hover text (what we're parsing) is recognizable
            if (scheduleItemMatcher.find()) {
              scheduleArray.put(new JSONObject()
                  .put("time", scheduleItemMatcher.group(1))
                  .put("route", scheduleItemMatcher.group(2))
                  .put("location", scheduleItemMatcher.group(3))
              );
            } else {
              scheduleArray.put(new JSONObject());
            }
          }

          String scheduleName = studentBusSchedule.child(i).child(0).text();
          busSchedule.put(scheduleName, scheduleArray);
        }
        return busSchedule;
      }
    } catch (NullPointerException | IndexOutOfBoundsException e) {
      e.printStackTrace();
      // Do nothing
    }
    return null;
  }
}
