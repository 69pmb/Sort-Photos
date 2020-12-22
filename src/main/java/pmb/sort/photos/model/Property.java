package pmb.sort.photos.model;

public enum Property {

    DEFAULT_WORKING_DIR("default_working_dir"),
    PICTURE_EXTENSION("picture_extension"),
    VIDEO_EXTENSION("video_extension"),
    DATE_FORMAT("date_format"),
    ENABLE_FOLDERS_ORGANIZATION("enable_folders_organization"),
    OVERWRITE_IDENTICAL("overwrite_identical"),
    FALL_BACK_CHOICE("fall_back_choice"),
    FALL_BACK_PATTERN("fall_back_pattern");

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

    @Override
    public String toString() {
        return value;
    }

}
