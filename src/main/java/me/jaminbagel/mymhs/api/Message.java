package me.jaminbagel.mymhs.api;

/**
 * Created by Ben on 1/23/20 @ 6:03 PM
 */
public enum Message {
  LOGIN_SUCCESS("Successfully logged into Genesis"),
  LOGIN_INVALID_CREDENTIALS("Incorrect username or password");

  private final String text;

  Message(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return this.text;
  }
}
