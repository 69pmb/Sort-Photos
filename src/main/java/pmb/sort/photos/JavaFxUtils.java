package pmb.sort.photos;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import pmb.my.starter.exception.MinorException;

public final class JavaFxUtils {

    private JavaFxUtils() {
        throw new AssertionError("Must not be used");
    }

    public static BorderPane displayPicture(String absolutePath, String styleClass, Integer width) {
        ImageView imageView = new ImageView();
        BorderPane imageViewWrapper = new BorderPane(imageView);
        try (InputStream stream = new FileInputStream(absolutePath)) {
            Image image = new Image(stream);
            imageView.setImage(image);
            imageView.setFitWidth(width);
            imageView.setPreserveRatio(true);
            imageViewWrapper.getStyleClass().add(styleClass);
        } catch (IOException e) {
            throw new MinorException("Error when displaying picture " + absolutePath, e);
        }
        return imageViewWrapper;
    }

    public static Button buildButton(Pane parent, String label, EventHandler<ActionEvent> action) {
        Button button = new Button(label);
        button.setAlignment(Pos.CENTER);
        button.setOnAction(action::handle);
        parent.getChildren().add(button);
        return button;
    }

    public static Text buildText(String str, int wrappingWidth) {
        Text text = new Text(str);
        text.setWrappingWidth(wrappingWidth);
        text.setTextAlignment(TextAlignment.JUSTIFY);
        return text;
    }

}
