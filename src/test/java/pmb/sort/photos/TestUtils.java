package pmb.sort.photos;

import java.io.File;

public class TestUtils {

    private static final String TEST_RESOURCE_DIR = "src/test/resources/";
    public static final File WITH_EXIF = new File(TEST_RESOURCE_DIR + "test1.jpg");
    public static final File WITHOUT_EXIF = new File(TEST_RESOURCE_DIR + "test2.jpg");

    public class MySpy {

        public MySpy() {}

        public void hello() {
            System.out.println("hello");
        }

    }

}
