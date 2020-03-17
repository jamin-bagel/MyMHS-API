package me.jaminbagel.mymhs.fetch;

import java.net.URL;
import lombok.SneakyThrows;

/**
 * Created by Ben on 1/11/20 @ 3:20 PM
 */
public class GenesisURL {

  public static final String BASE = "https://parents.mtsd.k12.nj.us/genesis/";

  public enum Path {
    AUTH("sis/j_security_check"),

    // Student ID
    STUDENT_SUMMARY(
        "parents?tab1=studentdata&tab2=studentsummary&action=form&studentid=%1$s&busview=week"),

    // Student ID & Grading Period
    WEEKLY_SUMMARY(
        "parents?tab1=studentdata&tab2=gradebook&tab3=weeklysummary&action=form&studentid=%1$s&mpToView=%2$s"),

    // Student ID
    ALL_ASSIGNMENTS(
        "parents?tab1=studentdata&tab2=gradebook&tab3=listassignments&action=form&studentid=%1$s&dateRange=allMP&courseAndSection=&status="),

    // Student ID & Course ID & Course Section & Grading Period
    COURSE_SUMMARY(
        "parents?tab1=studentdata&tab2=gradebook&tab3=coursesummary&action=form&studentid=%1$s&courseCode=%2$s&courseSection=%3$s&mp=%4$s"
    );

    private final String value;

    Path(String value) {
      this.value = value;
    }

    @SneakyThrows
    public URL format(Object... options) {
      //noinspection RedundantCast (IntelliJ conflicting "redudant cast"" and "confusing arg to varargs" warnings)
      return new URL(String.format(BASE + getValue(), (Object[]) options));
    }

    private String getValue() {
      return this.value;
    }
  }
}
