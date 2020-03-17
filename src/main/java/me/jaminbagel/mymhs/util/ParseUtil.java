package me.jaminbagel.mymhs.util;

import java.util.regex.Pattern;
import org.json.JSONObject;

/**
 * Created by Ben on 2/9/20 @ 9:32 PM
 */
public class ParseUtil {

  public static final Pattern GRADING_PERIOD_PATTERN = Pattern.compile("^[A-Z0-9]{1,5}$");

  public static final String COURSE_ID_AND_SECTION_REGEX = "^\\d+:\\d+$";
  public static final Pattern COURSE_ID_AND_SECTION_PATTERN = Pattern
      .compile(COURSE_ID_AND_SECTION_REGEX);

  /**
   * Separate a course/section string into its separate parts ie "11000:7"
   *
   * @param input Course/section combined string
   * @return
   */
  public static JSONObject courseAndSectionFromString(String input) {
    if (input.matches(COURSE_ID_AND_SECTION_REGEX)) {
      String[] courseIdSplit = input.split(":");
      JSONObject course = new JSONObject();

      course.put("id", Integer.parseInt(courseIdSplit[0]));
      course.put("section", Integer.parseInt(courseIdSplit[1]));
      return course;
    }
    return null;
  }
}
