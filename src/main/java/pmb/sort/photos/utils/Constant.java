package pmb.sort.photos.utils;

public final class Constant {

    public static final String SUFFIX_SEPARATOR = "~";
    public static final String EXTENSION_SEPARATOR = ",";
    public static final String CSS_FILE = "application.css";
    public static final String CSS_CLASS_ERROR = "error";
    public static final String CSS_CLASS_BOX = "box";
    public static final String YEAR_FORMAT = "yyyy";
    public static final String MONTH_FORMAT = "MM.yyyy";
    public static final String MONTH_REGEX = "^((0\\d)|(1[0-2]))\\.(19|20)\\d{2}$";
    public static final String YEAR_REGEX = "^(19|20)\\d{2}$";
    public static final String REGEX_EXTENSION = "^\\w+(" + EXTENSION_SEPARATOR + "\\w+)*$";

    private Constant() {
        throw new AssertionError("Must not be used");
    }

}