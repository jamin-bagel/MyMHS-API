package me.jaminbagel.mymhs.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import me.jaminbagel.mymhs.Main;
import me.jaminbagel.mymhs.exception.InvalidServerResponseException;
import me.jaminbagel.mymhs.fetch.GenesisURL.Path;
import me.jaminbagel.mymhs.fetch.Request.Builder;
import me.jaminbagel.mymhs.fetch.RequestMethod;
import me.jaminbagel.mymhs.fetch.Response;

/**
 * Created by Ben on 1/12/20 @ 9:42 PM
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class AuthUtil {

  // For validating session IDs sent to our server as parameters
  public static final Pattern SESSION_ID_PATTERN = Pattern.compile("^[A-Z0-9]{32}$");
  // For validating student IDs sent to our server as parameters
  public static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^\\d{1,15}$");
  // Gets only the value of the session ID from a Set-Cookie header
  private static final Pattern SESSION_ID_COOKIE_PATTERN = Pattern
      .compile("JSESSIONID=([A-Z0-9]{32})($|;)");

  /**
   * Generate a new session ID that can be used to interface with Genesis
   * <p>
   * New requests sent to the auth path respond with a new Genesis session ID in the set-cookie
   * header This page is also fairly quick to load compared to others, making it ideal for
   * generating sessions
   *
   * @return A new random session ID generated by Genesis
   * @throws InvalidServerResponseException If a session ID is not returned or it could not be
   *                                        parsed
   * @throws IOException                    If the request fails
   */
  public static String getNewSessionId() throws InvalidServerResponseException, IOException {
    try {
      String responseCookie = new
          Builder(Path.AUTH.format())
          .method(RequestMethod.GET)
          .construct()
          .execute()
          .getHeader("set-cookie");

      if (responseCookie != null) {
        Matcher cookieMatcher = SESSION_ID_COOKIE_PATTERN.matcher(responseCookie);
        if (cookieMatcher.find()) {
          return cookieMatcher.group(1);
        }
      }
    } catch (MalformedURLException e) {
      Main.logger.error("Bad URL", e);
    }

    throw new InvalidServerResponseException();
  }

  /**
   * Take an existing session ID and authenticate it with Genesis
   *
   * @param user      Username of the user to attach to this session
   * @param password  User's password
   * @param sessionId Session ID to attach the user to
   * @return Whether or not the authentication was successful
   * @throws IOException If the request fails
   */
  public static boolean authenticateSession(String user, String password, String sessionId)
      throws IOException, InvalidServerResponseException {
    // Verify that Session ID, Username and Password are all present and valid
    if (!validateSessionId(sessionId)
        || !validateUsernameOrPassword(user)
        || !validateUsernameOrPassword(password)) {
      return false;
    }

    Response authResponse = new
        Builder(Path.AUTH.format())
        .method(RequestMethod.POST)
        .setHeader("Cookie", "JSESSIONID=" + sessionId)
        .body(getURLEncodedAuthBody(user, password))
        .construct()
        .execute();

    // If a set-cookie is returned as well, then the authentication failed
    if (authResponse.getHeader("location") == null
        || authResponse.getHeader("set-cookie") != null) {
      throw new InvalidServerResponseException();
    }
    return authResponse.getHeader("location").contains("/genesis/parents?");
  }

  /**
   * Convert a username and password into a URL encoded string that can be safely sent through
   * Genesis' security check
   *
   * @param user     Username to encode
   * @param password Password to encode
   * @return URL encoded string to be sent in j_security_check body
   */
  @SneakyThrows
  private static String getURLEncodedAuthBody(String user, String password) {
    return "j_username=" + URLEncoder.encode(user, StandardCharsets.UTF_8.name()) +
        "&j_password=" + URLEncoder.encode(password, StandardCharsets.UTF_8.name());
  }

  /**
   * Ensure that an input field is realistic and not intended to overload our servers or Genesis'
   *
   * @param authInput Auth-related input to check against
   * @return Whether or not the username/password is safe to send to Genesis
   */
  public static boolean validateUsernameOrPassword(String authInput) {
    return authInput != null && authInput.length() <= 320 && authInput.length() > 1;
  }

  /**
   * Ensure that a Genesis session ID (usually passed through URL parameter) is valid
   *
   * @param sessionId Session ID to check
   * @return Whether or not the session ID is properly formatted
   */
  public static boolean validateSessionId(String sessionId) {
    return sessionId != null && SESSION_ID_PATTERN.matcher(sessionId).find();
  }
}