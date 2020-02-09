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
        "parents?tab1=studentdata&tab2=studentsummary&action=form&studentid=%s&busview=week"),

    // Student ID & Grading Section & Date
    WEEKLY_SUMMARY(
        "parents?tab1=studentdata&tab2=gradebook&tab3=weeklysummary&action=form&studentid=%s&mpToView=%s"),

    // Student ID
    ALL_ASSIGNMENTS(
        "parents?tab1=studentdata&tab2=gradebook&tab3=listassignments&action=form&studentid=%s&dateRange=allMP&courseAndSection=&status=");

    private final String value;

    Path(String value) {
      this.value = value;
    }

    @SneakyThrows
    public URL format(String... options) {
      //noinspection RedundantCast (IntelliJ conflicting "redudant cast"" and "confusing arg to varargs" warnings)
      return new URL(String.format(BASE + getValue(), (Object[]) options));
    }

    private String getValue() {
      return this.value;
    }
  }
}
