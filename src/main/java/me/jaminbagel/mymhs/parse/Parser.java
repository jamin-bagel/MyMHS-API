package me.jaminbagel.mymhs.parse;

import me.jaminbagel.mymhs.exception.LoggedOutException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by Ben on 1/15/20 @ 12:16 PM
 */
public abstract class Parser {

  private final Document dom;

  public Parser(String html) throws LoggedOutException {
    this.dom = Jsoup.parse(html.replaceAll("\\s{2,}", ""));
    if (dom.getElementsByClass("logonHeader").size() > 0) {
      throw new LoggedOutException();
    }
  }

  Document getDom() {
    return dom;
  }

  public abstract JSONObject parse();
}
