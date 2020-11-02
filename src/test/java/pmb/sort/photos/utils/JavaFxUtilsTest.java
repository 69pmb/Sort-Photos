package pmb.sort.photos.utils;

import static com.sun.javafx.application.PlatformImpl.startup;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import pmb.my.starter.exception.MinorException;
import pmb.sort.photos.RunMyTests;
import pmb.sort.photos.TestUtils;
import pmb.sort.photos.TestUtils.MySpy;

@RunMyTests
class JavaFxUtilsTest {

    @BeforeAll
    static void setup() {
        startup(() -> {
        });
    }

    @Nested
    class display_picture {

        @Test
        void ok() {
            BorderPane actual = JavaFxUtils.displayPicture(TestUtils.WITH_EXIF.getAbsolutePath(), "my_css", 300);

            ImageView image = (ImageView) actual.getCenter();

            assertAll(() -> assertEquals(300, image.getFitWidth()), () -> assertTrue(image.isPreserveRatio()),
                    () -> assertEquals(1, actual.getStyleClass().size()),
                    () -> assertEquals("my_css", actual.getStyleClass().get(0)));
        }

        @Test
        void ko() {
            assertThrows(MinorException.class, () -> JavaFxUtils.displayPicture("my\\failed\\test", "my_css", 300));
        }

    }

    @Test
    void buildButton() {
        BorderPane pane = new BorderPane();
        MySpy spy = mock(TestUtils.MySpy.class);

        Button button = JavaFxUtils.buildButton(pane, "label", e -> spy.hello());

        assertAll(() -> assertEquals(Pos.CENTER, button.getAlignment()),
                () -> assertEquals(1, pane.getChildren().size()),
                () -> assertEquals(button, pane.getChildren().get(0)));

        button.fire();
        verify(spy).hello();
    }

    @Test
    void buildText() {
        Text actual = JavaFxUtils.buildText("myText", 420);

        assertAll(() -> assertEquals(420, actual.getWrappingWidth()), () -> assertEquals("myText", actual.getText()),
                () -> assertEquals(TextAlignment.JUSTIFY, actual.getTextAlignment()));
    }

}