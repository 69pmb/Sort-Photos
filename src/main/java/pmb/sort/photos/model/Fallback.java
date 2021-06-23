package pmb.sort.photos.model;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum Fallback {

    CREATE,
    EDIT,
    PATTERN;

    public static boolean exist(String value) {
        return Arrays.stream(Fallback.values()).map(Fallback::toString).anyMatch(s -> StringUtils.equalsIgnoreCase(s, value));
    }

}
