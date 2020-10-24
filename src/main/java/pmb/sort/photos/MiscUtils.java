package pmb.sort.photos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import javafx.scene.control.TextField;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyProperties;

public final class MiscUtils {

    private static final Pattern REGEX_EXTENTION = Pattern.compile("^(\\w+,)+\\w+$");
    public static final Predicate<TextField> isBlank = f -> StringUtils.isBlank(f.getText());
    public static final Predicate<TextField> invalidCharacters = f -> Arrays
            .stream(MyConstant.getForbiddenCharactersFilename()).anyMatch(s -> f.getText().contains(s));
    public static final Predicate<TextField> validDateFormat = f -> validateDateFormat(f.getText());
    public static final Predicate<TextField> validExtention = f -> REGEX_EXTENTION.asMatchPredicate().test(f.getText());

    private MiscUtils() {
        throw new AssertionError("Must not be used");
    }

    public static boolean validateDateFormat(String format) {
        try {
            new SimpleDateFormat(format);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean validateDate(String format, String input) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setLenient(false);
            sdf.parse(input);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static String getDefaultValue(Property property) {
        return MyProperties.getOrDefault(property.getValue(), "");
    }
}
