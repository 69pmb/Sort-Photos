package pmb.sort.photos;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import com.sun.javafx.application.PlatformImpl;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyProperties;
import pmb.sort.photos.controller.MainController;
import pmb.sort.photos.model.Property;
import pmb.sort.photos.utils.Constant;
import pmb.sort.photos.utils.MiscUtils;

@RunMyTests
class ControllerTest
        implements WithAssertions {

    MainController controller;

    @BeforeAll
    static void start() {
        PlatformImpl.startup(() -> {});
    }

    @BeforeEach
    void setup() {
        controller = new MainController();
        controller.radioRoot = new RadioButton();
        controller.radioYear = new RadioButton();
        controller.messageProperties = new Text();
        controller.radioMonth = new RadioButton();
        controller.selectedDir = new TextField();
        controller.defaultDirectory = "";
        controller.enableFoldersOrganization = new CheckBox();
        controller.messages = new Text();
        controller.processBtn = new Button();
        controller.saveDirBtn = new Button();
        controller.dateFormat = new TextField();
        controller.overwriteIdentical = new CheckBox();
        controller.pictureExtension = new TextField();
        controller.videoExtension = new TextField();
        controller.pattern = new TextField();
        controller.fallbackPattern = new RadioButton();
        controller.fallbackCreate = new RadioButton();
        controller.fallbackEdit = new RadioButton();
    }

    @Nested
    class save_default_dir {

        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = { "  ", "TEST" })
        void failed_when_incorrect_value_given(String dir) {
            try (MockedStatic<MyProperties> myProperties = mockStatic(MyProperties.class)) {
                controller.selectedDir.setText(dir);

                controller.saveDefaultDir();

                assertAll(() -> assertTrue(controller.processBtn.isDisable()), () -> assertTrue(controller.saveDirBtn.isDisable()),
                        () -> assertTrue(controller.selectedDir.getStyleClass().contains(Constant.CSS_CLASS_ERROR)));

                myProperties.verify(() -> MyProperties.set(Property.DEFAULT_WORKING_DIR.getValue(), TestUtils.WITH_EXIF.getParent()), never());
                myProperties.verify(MyProperties::save, never());
            }
        }

        @Test
        void succeed_when_given_directory_is_correct() {
            try (MockedStatic<MyProperties> myProperties = mockStatic(MyProperties.class)) {
                controller.selectedDir.setText(TestUtils.WITH_EXIF.getParent());

                controller.saveDefaultDir();

                assertAll(() -> assertFalse(controller.processBtn.isDisable()), () -> assertFalse(controller.saveDirBtn.isDisable()),
                        () -> assertFalse(controller.selectedDir.getStyleClass().contains(Constant.CSS_CLASS_ERROR)),
                        () -> assertTrue(controller.radioRoot.isSelected()), () -> assertFalse(controller.radioMonth.isSelected()),
                        () -> assertFalse(controller.radioYear.isSelected()));

                myProperties.verify(() -> MyProperties.set(Property.DEFAULT_WORKING_DIR.getValue(), TestUtils.WITH_EXIF.getParent()));
                myProperties.verify(MyProperties::save);
            }
        }

    }

    @Test
    void init_properties() {
        List<Property> list = List.of(Property.DATE_FORMAT, Property.PICTURE_EXTENSION, Property.VIDEO_EXTENSION, Property.FALL_BACK_PATTERN);
        try (MockedStatic<MyProperties> myProperties = mockStatic(MyProperties.class)) {
            try (MockedStatic<MiscUtils> getValue = mockStatic(MiscUtils.class)) {
                getValue.when(() -> MiscUtils.getDefaultValue(Property.ENABLE_FOLDERS_ORGANIZATION)).thenReturn("false");
                getValue.when(() -> MiscUtils.getDefaultValue(Property.OVERWRITE_IDENTICAL)).thenReturn("true");
                list.forEach(p -> getValue.when(() -> MiscUtils.getDefaultValue(p)).thenReturn("TEST"));
                myProperties.when(() -> MyProperties.get("fall_back_choice")).thenReturn(Optional.of("pattern"));

                controller.initProperties();

                List.of(controller.dateFormat, controller.pictureExtension, controller.videoExtension, controller.pattern).forEach(txt -> {
                    assertEquals("TEST", txt.getText());
                    assertFalse(txt.getStyleClass().contains(Constant.CSS_CLASS_ERROR));
                });
                assertAll(() -> assertEquals("", controller.messageProperties.getText()),
                        () -> assertFalse(controller.enableFoldersOrganization.isSelected()),
                        () -> assertTrue(controller.overwriteIdentical.isSelected()), () -> assertFalse(controller.fallbackCreate.isSelected()),
                        () -> assertFalse(controller.fallbackEdit.isSelected()), () -> assertTrue(controller.fallbackPattern.isSelected()),
                        () -> assertFalse(controller.pattern.isDisable()));
                List.of(controller.radioYear, controller.radioMonth, controller.radioRoot).forEach(radio -> assertTrue(radio.isDisabled()));

                getValue.verify(() -> MiscUtils.getDefaultValue(Property.ENABLE_FOLDERS_ORGANIZATION));
                getValue.verify(() -> MiscUtils.getDefaultValue(Property.OVERWRITE_IDENTICAL));
                list.forEach(p -> getValue.verify(() -> MiscUtils.getDefaultValue(p)));
                myProperties.verify(() -> MyProperties.get("fall_back_choice"));
            }
        }
    }

    @Nested
    class select_directory {

        @Test
        void when_no_file_selected_and_default_dir_not_exist() {
            controller.defaultDirectory = TestUtils.WITH_EXIF.getParentFile().getAbsolutePath();
            assertTrue(StringUtils.isBlank(controller.selectedDir.getText()));
            controller = spy(controller);
            doReturn(null).when(controller).chooseDirectory(new File(controller.defaultDirectory));

            controller.selectDirectory();

            assertAll(() -> assertEquals(controller.defaultDirectory, controller.selectedDir.getText()),
                    () -> assertTrue(controller.processBtn.isDisable()), () -> assertTrue(controller.saveDirBtn.isDisable()),
                    () -> assertFalse(controller.radioYear.isSelected()), () -> assertFalse(controller.radioMonth.isSelected()),
                    () -> assertTrue(controller.radioRoot.isSelected()));

            verify(controller).chooseDirectory(TestUtils.WITH_EXIF.getParentFile());
            verify(controller).isValidSelectedDirectory(any());
        }

        @ParameterizedTest(name = "when {0} is selected expected year to be {1}, month {2} and root {3}")
        @CsvSource({ "2020, true, false, false", "03.2019/test2.jpg, false, true, false" })
        void when_a_file_is_selected_and_default_dir_exist(String selectedFile, boolean isYear, boolean isMonth, boolean isRoot) {
            controller.selectedDir.setText(TestUtils.WITH_EXIF.getAbsolutePath());
            controller = spy(controller);
            File folder = TestUtils.GET_FILE.apply(selectedFile);
            doReturn(folder).when(controller).chooseDirectory(TestUtils.WITH_EXIF);

            controller.selectDirectory();

            assertAll(
                    () -> assertEquals((folder.isDirectory() ? folder : folder.getParentFile()).getAbsolutePath(), controller.selectedDir.getText()),
                    () -> assertFalse(controller.processBtn.isDisable()), () -> assertFalse(controller.saveDirBtn.isDisable()),
                    () -> assertEquals(isYear, controller.radioYear.isSelected()), () -> assertEquals(isMonth, controller.radioMonth.isSelected()),
                    () -> assertEquals(isRoot, controller.radioRoot.isSelected()));

            verify(controller).chooseDirectory(TestUtils.WITH_EXIF);
            verify(controller, never()).isValidSelectedDirectory(() -> {});
        }

    }

    @ParameterizedTest
    @MethodSource("workDirProvider")
    void initialize(ArgumentsAccessor arguments) {
        controller = spy(controller);
        try (MockedStatic<MyProperties> myProperties = mockStatic(MyProperties.class)) {
            doNothing().when(controller).initProperties();
            myProperties.when(() -> MyProperties.get("default_working_dir")).thenReturn(Optional.ofNullable(arguments.getString(0)));

            controller.initialize(null, TestUtils.BUNDLE);

            assertAll(() -> assertThat(controller.selectedDir.getText()).isEqualTo(arguments.getString(1)),
                    () -> assertTrue(controller.radioRoot.isSelected()));

            myProperties.verify(() -> MyProperties.get("default_working_dir"));
            myProperties.verify(() -> MyProperties.setConfigPath(MyConstant.CONFIGURATION_FILENAME));
            verify(controller).initProperties();
        }
    }

    static Stream<String[]> workDirProvider() {
        return Stream.of(new String[] { "Test", MyConstant.USER_DIRECTORY }, new String[] { null, MyConstant.USER_DIRECTORY },
                new String[] { TestUtils.TEST_RESOURCE_DIR, TestUtils.TEST_RESOURCE_DIR });
    }

    @Nested
    class save_properties {

    }

}
