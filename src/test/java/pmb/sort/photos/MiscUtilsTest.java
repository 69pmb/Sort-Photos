package pmb.sort.photos;

import java.io.File;
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

class MiscUtilsTest {

    private static final String TEST_RESOURCE_DIR = "src/test/resources/";

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
        Assertions.assertFalse(MiscUtils.invalidCharacters.test(new TextField("correct")));
        assertPredicates(Arrays.asList(MyConstant.getForbiddenCharactersFilename()), MiscUtils.invalidCharacters, true);
    }

    @Test
    void testPredicateValidExtention() {
        assertPredicates(List.of("mp3,mp4", "mp3", "avi,mkv,wmv"), MiscUtils.validExtention, true);
        assertPredicates(List.of("mp3,", ",", "avi,mkv!"), MiscUtils.validExtention, false);
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
    void validateDate() {
        Assertions.assertTrue(MiscUtils.validateDate("dd/MM/yyyy", "12/05/2020"));
        Assertions.assertTrue(MiscUtils.validateDate("MM.yyyy", "06.2020"));
        Assertions.assertTrue(MiscUtils.validateDate("yyyy", "2020"));
        Assertions.assertFalse(MiscUtils.validateDate("yyyy", "06.2020"));
        Assertions.assertFalse(MiscUtils.validateDate("MM.yyyy", "02.06.2020"));
        Assertions.assertFalse(MiscUtils.validateDate("MM.yyyy", "2020"));
    }

    @Test
    void getTakenTimeOk() {
        Optional<Date> takenTime = MiscUtils.getTakenTime(new File(TEST_RESOURCE_DIR + "test1.jpg"));
        Assertions.assertTrue(takenTime.isPresent());
        Assertions.assertEquals("2008-02-07T11:33", new SimpleDateFormat("yyyy-MM-dd'T'hh:mm").format(takenTime.orElseThrow()));
    }

    @Test
    void getTakenTimeKo() {
        Assertions.assertTrue(MiscUtils.getTakenTime(new File(TEST_RESOURCE_DIR + "test2.jpg")).isEmpty());
    }

    @Test
    void getModelOk() {
        Optional<String> model = MiscUtils.getModel(new File(TEST_RESOURCE_DIR + "test1.jpg"));
        Assertions.assertTrue(model.isPresent());
        Assertions.assertEquals("FinePixS1Pro", model.orElseThrow());
    }

    @Test
    void getModelKo() {
        Assertions.assertTrue(MiscUtils.getModel(new File(TEST_RESOURCE_DIR + "test2.jpg")).isEmpty());
    }

}
