package pmb.sort.photos.model;

import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

/** Fallback choices when there is no taken date for a picture. */
public enum Fallback {
  CREATE,
  EDIT,
  PATTERN;

  /**
   * Checks if given value exist in the enum.
   *
   * @param value to check
   * @return true if a match is found in {@link Fallback#values()}
   */
  public static boolean exist(String value) {
    return Arrays.stream(Fallback.values())
        .map(Fallback::toString)
        .anyMatch(s -> StringUtils.equalsIgnoreCase(s, value));
  }
}
