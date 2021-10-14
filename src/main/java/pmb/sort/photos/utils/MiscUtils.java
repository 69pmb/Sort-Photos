package pmb.sort.photos.utils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.bmp.BmpHeaderDirectory;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import com.drew.metadata.webp.WebpDirectory;

import javafx.scene.control.TextField;
import pmb.my.starter.exception.MajorException;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyProperties;
import pmb.sort.photos.model.Property;

/**
 * Various methods.
 */
public final class MiscUtils {

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
    public static final Predicate<TextField> isValidExtension = f -> isValidRegex.test(f.getText(), Constant.REGEX_EXTENSION);

    private MiscUtils() {
        throw new AssertionError("Must not be used");
    }

    /**
     * Checks if the given date format is valid.
     *
     * @param format the date format to validate
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

    /**
     * Gets a property value with default empty string value.
     *
     * @param property to recover
     * @return value stored in {@code config.properties} file
     */
    public static String getDefaultValue(Property property) {
        return MyProperties.getOrDefault(property.getValue(), "");
    }

    /**
     * Checks if given date matches given format.
     *
     * @param date to check
     * @param format the format to match against
     * @return true if a date could be parsed, false otherwise
     */
    public static boolean isStringMatchDateFormat(String date, SimpleDateFormat format) {
        try {
            return format.parse(date) != null;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Finds {@link Metadata} of given file representing a picture.
     *
     * @param file to process
     * @return an Optional of Metadata, empty if the metadata can't be read
     * @throws MajorException thrown when file or metadata can't be read (not found or corrupt for instance)
     * @see ImageMetadataReader#readMetadata(File)
     */
    public static Optional<Metadata> getMetadata(File file) throws MajorException {
        try {
            return Optional.ofNullable(ImageMetadataReader.readMetadata(file));
        } catch (ImageProcessingException | IOException e) {
            throw new MajorException("Error reading metadata of file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Recovers taken date time from {@link Metadata}.
     *
     * @param metadata of a picture file
     * @return an optional date if no taken date is found
     */
    public static Optional<Date> getTakenTime(Metadata metadata) {
        return Optional.ofNullable(metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class)).map(dir -> dir.getDateOriginal(TimeZone.getDefault()));
    }

    /**
     * Recovers dimension of a picture from {@link Metadata}.
     *
     * @param metadata of a picture file
     * @param extension of the picture file
     * @return an optional of String, with pattern "width x height"
     */
    public static Optional<String> getDimension(Metadata metadata, Object extension) {
        Map<String, Triple<Class<? extends Directory>, Integer, Integer>> infoByExtension = Map.of("png",
            Triple.of(PngDirectory.class, PngDirectory.TAG_IMAGE_WIDTH, PngDirectory.TAG_IMAGE_HEIGHT), "jpg",
            Triple.of(JpegDirectory.class, JpegDirectory.TAG_IMAGE_WIDTH, JpegDirectory.TAG_IMAGE_HEIGHT), "webp",
            Triple.of(WebpDirectory.class, WebpDirectory.TAG_IMAGE_WIDTH, WebpDirectory.TAG_IMAGE_HEIGHT), "GIF",
            Triple.of(BmpHeaderDirectory.class, BmpHeaderDirectory.TAG_IMAGE_WIDTH, BmpHeaderDirectory.TAG_IMAGE_HEIGHT));
        return Optional.ofNullable(infoByExtension.get(extension)).flatMap(tuple -> Optional.ofNullable(metadata.getFirstDirectoryOfType(tuple.getLeft()))
            .map(d -> d.getString(tuple.getMiddle()) + " x " + d.getString(tuple.getRight())));
    }

    /**
     * Recovers device model that took from {@link Metadata}.
     *
     * @param metadata of a picture file
     * @return an optional string if no model is found
     */
    public static Optional<String> getModel(Metadata metadata) {
        return Optional.ofNullable(metadata.getFirstDirectoryOfType(ExifIFD0Directory.class)).map(dir -> dir.getString(ExifDirectoryBase.TAG_MODEL));
    }

}
