package me.jaminbagel.mymhs.util;

import lombok.Getter;

/**
 * Created by Ben on 2/8/20 @ 8:36 PM
 */
public class GradeUtil {

  // Matches 2 int/floats with a slash in between (how grade points are formatted in the assignment list)
  public static final String GRADE_POINTS_REGEX = "^\\d+(\\.\\d+)? ?\\/ ?\\d+(\\.\\d+)?$";

  /**
   * Various states that an assignment can be in
   */
  public enum GradeStatus {
    /**
     * The assignment is technically "put in", but has a completely empty grade value (has no grade
     * impact)
     */
    NO_GRADE(0),

    /**
     * The grade has been put in and has either a number grade or a string that shows its
     * completion
     * <p>
     * ie "9/10" or "completed"
     */
    GRADED(1),

    /**
     * The grade has not been put in yet
     */
    UNGRADED(2, "Not Graded"),

    /**
     * The assignment was not turned in (counts as 0%)
     */
    MISSING(3, "Missing"),

    /**
     * The assignment was not completed (counts as 0%)
     * <p>
     * NOTE: Teachers often prefer to put in their own grades so that students can get partial
     * credit rather than a 0%
     */
    INCOMPLETE(4, "Incomplete"),

    /**
     * The assignment was missed due to an absent (has no grade impact)
     */
    ABSENT(5, "Absent"),

    /**
     * The student was not obligated to complete that assignment (has no grade impact)
     */
    EXEMPT(6, "Exempt");

    @Getter
    private final int id;
    @Getter
    private final String designator;

    GradeStatus(int id) {
      this.id = id;
      this.designator = null;
    }

    GradeStatus(int id, String designator) {
      this.id = id;
      this.designator = designator;
    }
  }

  /**
   * Days of the week for assignments, etc
   * <p>
   * NOTE: SAT and SUN are included because assignments CAN be put on weekends :(
   */
  @SuppressWarnings("unused")
  public enum WeekDay {
    SUN(0),
    MON(1),
    TUE(2),
    WED(3),
    THU(4),
    FRI(5),
    SAT(6);

    @Getter
    private final int value;

    WeekDay(int value) {
      this.value = value;
    }
  }
}
