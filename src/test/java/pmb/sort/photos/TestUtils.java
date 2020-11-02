package pmb.sort.photos;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;

import pmb.my.starter.utils.MyConstant;

public class TestUtils {

    private static final String TEST_RESOURCE_DIR = MyConstant.USER_DIRECTORY + "src/test/resources/";
    public static final Function<String, File> GET_FILE = name -> new File(TEST_RESOURCE_DIR + name);
    public static final File WITH_EXIF = GET_FILE.apply("test1.jpg");
    public static final File WITHOUT_EXIF = GET_FILE.apply("test2.jpg");
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
