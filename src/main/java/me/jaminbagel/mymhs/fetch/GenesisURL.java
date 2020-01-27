package me.jaminbagel.mymhs.fetch;

import java.net.URL;
import lombok.SneakyThrows;

/**
 * Created by Ben on 1/11/20 @ 3:20 PM
 */
public class GenesisURL {

  public static String BASE = "https://parents.mtsd.k12.nj.us/genesis/";

  @SneakyThrows
  public static URL get(Path path, String... options) {
    return new URL(String.format(BASE + path.getValue(), options));
  }

  public enum Path {
    AUTH("sis/j_security_check"),
    GENERIC_PAGE("parents?tab1=studentdata&tab2=gradebook&tab3=weeklysummary"),

    STUDENT_SUMMARY("parents?tab1=studentdata&tab2=studentsummary&action=form&studentid=%s");

    private final String value;

    Path(String value) {
      this.value = value;
    }

    private String getValue() {
      return this.value;
    }
  }
}
