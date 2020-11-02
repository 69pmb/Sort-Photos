package pmb.sort.photos.utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sun.javafx.application.PlatformImpl;

import javafx.scene.control.TextField;
import pmb.my.starter.utils.MyConstant;
import pmb.sort.photos.TestUtils;

class MiscUtilsTest {

    @BeforeAll
    static void setup() {
        PlatformImpl.startup(() -> {});
    }

    @Test
    void testPredicateIsBlank() {
        TextField t = new TextField();
        Assertions.assertTrue(MiscUtils.isBlank.test(t));
        t.setText("test");
        Assertions.assertFalse(MiscUtils.isBlank.test(t));
    }

    @Test
    void testPredicateInvalidCharacters() {
        Assertions.assertFalse(MiscUtils.isInvalidCharacters.test(new TextField("correct")));
        assertPredicates(Arrays.asList(MyConstant.getForbiddenCharactersFilename()), MiscUtils.isInvalidCharacters, true);
    }

    @Test
    void testPredicateValidExtention() {
        assertPredicates(List.of("mp3,mp4", "mp3", "avi,mkv,wmv"), MiscUtils.isValidExtension, true);
        assertPredicates(List.of("mp3,", ",", "avi,mkv!"), MiscUtils.isValidExtension, false);
    }

    private void assertPredicates(List<String> inputs, Predicate<TextField> predicate, boolean expected) {
        TextField t = new TextField();
        inputs.forEach(s -> {
            t.setText(s);
            Assertions.assertEquals(expected, predicate.test(t), "Failed with input: " + s);
        });
    }

    @Test
    void validateDateFormat() {
        Assertions.assertTrue(MiscUtils.validateDateFormat("dd/MM/yyyy"));
        Assertions.assertFalse(MiscUtils.validateDateFormat("tt/MM\\;;"));
    }

    @Test
    void getTakenTimeOk() {
        Optional<Date> takenTime = MiscUtils.getTakenTime(TestUtils.WITH_EXIF);
        Assertions.assertTrue(takenTime.isPresent());
        Assertions.assertEquals("2008-02-07T11:33", new SimpleDateFormat("yyyy-MM-dd'T'hh:mm").format(takenTime.orElseThrow()));
    }

    @Test
    void getTakenTimeKo() {
        Assertions.assertTrue(MiscUtils.getTakenTime(TestUtils.WITHOUT_EXIF).isEmpty());
    }

    @Test
    void getModelOk() {
        Optional<String> model = MiscUtils.getModel(TestUtils.WITH_EXIF);
        Assertions.assertTrue(model.isPresent());
        Assertions.assertEquals("FinePixS1Pro", model.orElseThrow());
    }

    @Test
    void getModelKo() {
        Assertions.assertTrue(MiscUtils.getModel(TestUtils.WITHOUT_EXIF).isEmpty());
    }

}
