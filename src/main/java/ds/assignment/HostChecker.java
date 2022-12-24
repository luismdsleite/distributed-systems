package ds.assignment;

import java.util.regex.Pattern;

public class HostChecker {
  private static final Pattern PATTERN = Pattern.compile(
    "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
  );
  
  /**
   * Check if the the string {@code ipv4} is in a valid IPv4 format.
   * <p>
   * Note: Can be improved with the use of an external library like https://github.com/seancfoley/IPAddress.
   * https://stackoverflow.com/questions/61001216/how-can-i-check-if-a-string-is-ipv4-ipv6-or-domain-name-java
   * <p>
   * @param ipv4 String to validate.
   */
  public static boolean validate(final String ipv4) {
    return PATTERN.matcher(ipv4).matches();
  }
}
