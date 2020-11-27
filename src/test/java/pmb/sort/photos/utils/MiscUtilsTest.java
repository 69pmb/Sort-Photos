package pmb.sort.photos.utils;

import static com.sun.javafx.application.PlatformImpl.startup;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;

import javafx.scene.control.TextField;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyProperties;
import pmb.sort.photos.RunMyTests;
import pmb.sort.photos.TestUtils;
import pmb.sort.photos.model.Property;

@RunMyTests
class MiscUtilsTest {

    @BeforeAll
    static void setup() {
        startup(() -> {});
    }

    @Test
    void is_blank() {
        TextField t = new TextField();
        assertTrue(MiscUtils.isBlank.test(t));
        t.setText("test");
        assertFalse(MiscUtils.isBlank.test(t));
    }

    @Test
    void is_invalid_characters() {
        assertFalse(MiscUtils.isInvalidCharacters.test(new TextField("correct")));
        assertPredicates(Arrays.asList(MyConstant.getForbiddenCharactersFilename()), MiscUtils.isInvalidCharacters, true);
    }

    @Test
    void is_valid_extention() {
        assertPredicates(List.of("mp3,mp4", "mp3", "avi,mkv,wmv"), MiscUtils.isValidExtension, true);
        assertPredicates(List.of("mp3,", ",", "avi,mkv!"), MiscUtils.isValidExtension, false);
    }

    @Test
    void validate_date_format() {
        assertTrue(MiscUtils.validateDateFormat("dd/MM/yyyy"));
        assertFalse(MiscUtils.validateDateFormat("tt/MM\\;;"));
    }

    @Test
    void get_default_value() {
        try (MockedStatic<MyProperties> properties = mockStatic(MyProperties.class)) {
            properties.when(() -> MyProperties.getOrDefault("video_extension", "")).thenReturn("TEST");
            assertEquals("TEST", MiscUtils.getDefaultValue(Property.VIDEO_EXTENSION));
            properties.verify(() -> MyProperties.getOrDefault("video_extension", ""));
        }
    }

    @Nested
    class get_taken_time {

        @Test
        void with_datas() {
            Optional<Date> takenTime = MiscUtils.getTakenTime(TestUtils.WITH_EXIF);
            assertTrue(takenTime.isPresent());
            assertEquals("2008-02-07T11:33", new SimpleDateFormat("yyyy-MM-dd'T'hh:mm").format(takenTime.orElseThrow()));
        }

        @Test
        void without_data() {
            assertTrue(MiscUtils.getTakenTime(TestUtils.WITHOUT_EXIF).isEmpty());
        }

        @ParameterizedTest(name = "fails when reader throws {0}")
        @ValueSource(classes = { ImageProcessingException.class, IOException.class })
        void fails(Class<? extends Exception> exception) {
            try (MockedStatic<ImageMetadataReader> reader = mockStatic(ImageMetadataReader.class)) {
                reader.when(() -> ImageMetadataReader.readMetadata(TestUtils.WITH_EXIF)).thenThrow(exception);

                assertTrue(MiscUtils.getTakenTime(TestUtils.WITH_EXIF).isEmpty());

                reader.verify(() -> ImageMetadataReader.readMetadata(TestUtils.WITH_EXIF));
            }
        }

    }

    @Nested
    class get_model {

        @Test
        void with_datas() {
            Optional<String> model = MiscUtils.getModel(TestUtils.WITH_EXIF);

            assertAll(() -> assertTrue(model.isPresent()), () -> assertEquals("FinePixS1Pro", model.orElseThrow()));
        }

        @Test
        void without_data() {
            assertTrue(MiscUtils.getModel(TestUtils.WITHOUT_EXIF).isEmpty());
        }

        @ParameterizedTest(name = "fails when reader throws {0}")
        @ValueSource(classes = { ImageProcessingException.class, IOException.class })
        void fails(Class<? extends Exception> exception) {
            try (MockedStatic<ImageMetadataReader> reader = mockStatic(ImageMetadataReader.class)) {
                reader.when(() -> ImageMetadataReader.readMetadata(TestUtils.WITH_EXIF)).thenThrow(exception);

                assertTrue(MiscUtils.getModel(TestUtils.WITH_EXIF).isEmpty());

                reader.verify(() -> ImageMetadataReader.readMetadata(TestUtils.WITH_EXIF));
            }
        }

    }

    private void assertPredicates(List<String> inputs, Predicate<TextField> predicate, boolean expected) {
        TextField t = new TextField();
        inputs.forEach(s -> {
            t.setText(s);
            assertEquals(expected, predicate.test(t), "Failed with input: " + s);
        });
    }

}
