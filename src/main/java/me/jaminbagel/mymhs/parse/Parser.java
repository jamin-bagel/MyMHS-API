package me.jaminbagel.mymhs.parse;

import me.jaminbagel.mymhs.exception.LoggedOutException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by Ben on 1/15/20 @ 12:16 PM
 */
public abstract class Parser {

  private final Document dom;

  public Parser(String html) throws LoggedOutException {
    this.dom = Jsoup.parse(html.replaceAll("\\s{6,}", ""));
    if (dom.getElementsByClass("logonHeader").size() > 0) {
      throw new LoggedOutException();
    }
  }

  protected Document getDom() {
    return dom;
  }

  @SuppressWarnings("unused")
  public abstract JSONObject parse();

  /**
   * Parse the student selection dropdown
   *
   * @return A JSONObject containing available students with the current account as well as the
   * current student in use
   */
  protected JSONObject parseStudentSelector() {
    try {
      Element studentSelector = getDom().getElementById("fldStudent");
      if (studentSelector != null) {
        JSONObject studentSelectorData = new JSONObject();
        studentSelectorData.put("options", new JSONArray());
        // Loop through dropdown options
        for (Element studentOption : getDom().getElementById("fldStudent").children()) {
          studentSelectorData.getJSONArray("options").put(new JSONObject()
              .put("id", studentOption.attr("value"))
              .put("fullName", studentOption.text())
          );
          // Get current student
          if (studentOption.hasAttr("selected")) {
            studentSelectorData.put("selected", studentOption.attr("value"));
          }
        }
        return studentSelectorData;
      }
    } catch (NullPointerException | IndexOutOfBoundsException e) {
      // Do nothing
    }
    return null;
  }

  /**
   * Parse the date field above the content of some pages
   *
   * @return The default value of the date field as a String
   */
  protected String parseDateSelector() {
    try {
      Element dateField = getDom().getElementById("fldDate");
      if (dateField != null) {
        return dateField.attr("value");
      }
    } catch (NullPointerException | IndexOutOfBoundsException e) {
      // Do nothing
    }
    return null;
  }
}
