package pmb.sort.photos.model;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum Property {

    DEFAULT_WORKING_DIR("default_working_dir"),
    PICTURE_EXTENSION("picture_extension"),
    VIDEO_EXTENSION("video_extension"),
    DATE_FORMAT("date_format");

    private final String value;

    Property(String value) {
        this.value = value;
    }

    /**
     * Name of the enum instance.
     *
     * @return value given in constructor
     */
    public String getValue() {
        return value;
    }

    /**
     * Finds a Property by its value.
     *
     * @param value the value to find
     * @return a Cat or null if not found
     */
    public static Property getByValue(String value) {
        return Arrays.stream(values()).filter(prop -> StringUtils.equalsAnyIgnoreCase(prop.toString(), value))
                .findFirst().orElseGet(() -> Property.valueOf(value));
    }

    @Override
    public String toString() {
        return value;
    }
}
