package pmb.sort.photos;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import javafx.scene.control.TextField;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyProperties;

public final class MiscUtils {

    private static final Logger LOG = LogManager.getLogger(MiscUtils.class);

    private static final String REGEX_EXTENSION = "^\\w+(,\\w+)*$";

    /**
     * Predicate using {@link StringUtils#isBlank(CharSequence)}
     */
    public static final Predicate<TextField> isBlank = f -> StringUtils.isBlank(f.getText());

    /**
     * Predicate to test if input contains invalid Windows filesystem's characters.
     */
    public static final Predicate<TextField> isInvalidCharacters = f -> Arrays.stream(MyConstant.getForbiddenCharactersFilename())
            .anyMatch(s -> f.getText().contains(s));

    /**
     * Predicate to test if the given date format is valid.
     */
    public static final Predicate<TextField> isValidDateFormat = f -> validateDateFormat(f.getText());

    /**
     * Predicate to test if given input matches the given regex.
     */
    public static final BiPredicate<String, String> isValidRegex = (input, regex) -> Pattern.compile(regex).asMatchPredicate().test(input);

    /**
     * Predicate to test if given input is a valid list of extension file.
     */
    public static final Predicate<TextField> isValidExtension = f -> isValidRegex.test(f.getText(), REGEX_EXTENSION);

    private MiscUtils() {
        throw new AssertionError("Must not be used");
    }

    /**
     * Checks if the given date format is valid.
     *
     * @param format
     *            the date format to validate
     * @return if true the format is valid, false otherwise
     * @see SimpleDateFormat
     */
    public static boolean validateDateFormat(String format) {
        try {
            new SimpleDateFormat(format);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String getDefaultValue(Property property) {
        return MyProperties.getOrDefault(property.getValue(), "");
    }

    public static Optional<Date> getTakenTime(File file) {
        Metadata metadata;
        try {
            metadata = ImageMetadataReader.readMetadata(file);
            return Optional.ofNullable(metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class))
                    .map(dir -> dir.getDateOriginal(TimeZone.getDefault()));
        } catch (ImageProcessingException | IOException e) {
            LOG.error("Error reading metadata of file: {}", file.getAbsolutePath(), e);
        }
        return Optional.empty();
    }

    public static Optional<String> getModel(File file) {
        Metadata metadata;
        try {
            metadata = ImageMetadataReader.readMetadata(file);
            return Optional.ofNullable(metadata.getFirstDirectoryOfType(ExifIFD0Directory.class))
                    .map(dir -> dir.getString(ExifDirectoryBase.TAG_MODEL));
        } catch (ImageProcessingException | IOException e) {
            LOG.error("Error reading metadata of file: {}", file.getAbsolutePath(), e);
        }
        return Optional.empty();
    }

}
