package pmb.sort.photos;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Assertions;

public class TestUtils {

    private static final String TEST_RESOURCE_DIR = "src/test/resources/";
    public static final File WITH_EXIF = new File(TEST_RESOURCE_DIR + "test1.jpg");
    public static final File WITHOUT_EXIF = new File(TEST_RESOURCE_DIR + "test2.jpg");
    public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("i18n", Locale.FRENCH);

    public class MySpy {

        public MySpy() {}

        public void hello() {
            System.out.println("hello");
        }

    }

    public static void assertDate(String expected, Date actual) {
        Assertions.assertEquals(expected, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(actual));
    }

}
