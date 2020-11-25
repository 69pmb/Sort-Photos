package pmb.sort.photos;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import com.sun.javafx.application.PlatformImpl;

import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import pmb.my.starter.utils.MyProperties;
import pmb.sort.photos.model.Property;
import pmb.sort.photos.utils.Constant;
import pmb.sort.photos.utils.MiscUtils;

@RunMyTests
class ControllerTest {

    Controller controller;

    @BeforeAll
    static void start() {
        PlatformImpl.startup(() -> {});
    }

    @BeforeEach
    void setup() {
        controller = new Controller();
        controller.radioRoot = new RadioButton();
        controller.radioYear = new RadioButton();
        controller.radioMonth = new RadioButton();
        controller.selectedDir = new TextField();
        controller.messages = new Text();
        controller.goBtn = new Button();
        controller.saveDirBtn = new Button();
        controller.dateFormat = new TextField();
        controller.pictureExtension = new TextField();
        controller.videoExtension = new TextField();
    }

    @Nested
    class save_default_dir {

        @BeforeEach
        void setup() {}

        @ParameterizedTest
        @ValueSource(strings = { "", "  ", "TEST" })
        void failed_when_incorrect_value_given(String dir) {
            try (MockedStatic<MyProperties> myProperties = mockStatic(MyProperties.class)) {
                controller.selectedDir.setText(dir);

                controller.saveDefaultDir();

                assertAll(() -> assertTrue(controller.goBtn.isDisable()), () -> assertTrue(controller.saveDirBtn.isDisable()),
                        () -> assertTrue(controller.selectedDir.getStyleClass().contains(Constant.CSS_CLASS_ERROR)));

                myProperties.verify(never(), () -> MyProperties.set(Property.DEFAULT_WORKING_DIR.getValue(), TestUtils.WITH_EXIF.getParent()));
                myProperties.verify(never(), () -> MyProperties.save());
            }
        }

        @Test
        void succeed_when_given_directory_is_correct() {
            try (MockedStatic<MyProperties> myProperties = mockStatic(MyProperties.class)) {
                controller.selectedDir.setText(TestUtils.WITH_EXIF.getParent());

                controller.saveDefaultDir();

                assertAll(() -> assertFalse(controller.goBtn.isDisable()), () -> assertFalse(controller.saveDirBtn.isDisable()),
                        () -> assertFalse(controller.selectedDir.getStyleClass().contains(Constant.CSS_CLASS_ERROR)),
                        () -> assertTrue(controller.radioRoot.isSelected()), () -> assertFalse(controller.radioMonth.isSelected()),
                        () -> assertFalse(controller.radioYear.isSelected()));

                myProperties.verify(() -> MyProperties.set(Property.DEFAULT_WORKING_DIR.getValue(), TestUtils.WITH_EXIF.getParent()));
                myProperties.verify(() -> MyProperties.save());
            }
        }

    }

    @Test
    void reset_properties() {
        try (MockedStatic<MiscUtils> getValue = mockStatic(MiscUtils.class)) {
            getValue.when(() -> MiscUtils.getDefaultValue(any(Property.class))).thenReturn("TEST");

            controller.resetProperties();

            List.of(controller.dateFormat, controller.pictureExtension, controller.videoExtension).forEach(txt -> {
                assertEquals("TEST", txt.getText());
                assertFalse(txt.getStyleClass().contains(Constant.CSS_CLASS_ERROR));
            });

            getValue.verify(times(3), () -> MiscUtils.getDefaultValue(any(Property.class)));
        }
    }

    @Nested
    class select_directory {

        @Test
        void when_no_file_selected() {
            assertTrue(StringUtils.isBlank(controller.selectedDir.getText()));
            controller = spy(controller);
            doReturn(null).when(controller).chooseDirectory();

            controller.selectDirectory();

            assertAll(() -> assertEquals("", controller.selectedDir.getText()), () -> assertTrue(controller.goBtn.isDisable()),
                    () -> assertTrue(controller.saveDirBtn.isDisable()), () -> assertFalse(controller.radioYear.isSelected()),
                    () -> assertFalse(controller.radioMonth.isSelected()), () -> assertFalse(controller.radioRoot.isSelected()));
        }

        @ParameterizedTest(name = "when {0} is selected expected year to be {1}, month {2} and root {3}")
        @CsvSource({ "2020, true, false, false", "03.2019/test2.jpg, false, true, false" })
        void when_a_file_is_selected(String selectedFile, boolean isYear, boolean isMonth, boolean isRoot) {
            assertTrue(StringUtils.isBlank(controller.selectedDir.getText()));
            controller = spy(controller);
            File folder = TestUtils.GET_FILE.apply(selectedFile);
            doReturn(folder).when(controller).chooseDirectory();

            controller.selectDirectory();

            assertAll(
                    () -> assertEquals((folder.isDirectory() ? folder : folder.getParentFile()).getAbsolutePath(), controller.selectedDir.getText()),
                    () -> assertFalse(controller.goBtn.isDisable()), () -> assertFalse(controller.saveDirBtn.isDisable()),
                    () -> assertEquals(isYear, controller.radioYear.isSelected()), () -> assertEquals(isMonth, controller.radioMonth.isSelected()),
                    () -> assertEquals(isRoot, controller.radioRoot.isSelected()));
        }

    }

}