package pmb.sort.photos;

import java.io.File;
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
import pmb.my.starter.exception.MinorException;

public final class JavaFxUtils {

    private JavaFxUtils() {
        throw new AssertionError("Must not be used");
    }

    public static BorderPane displayPicture(File file, String styleClass, Integer width) {
        ImageView imageView = new ImageView();
        BorderPane imageViewWrapper = new BorderPane(imageView);
        try (InputStream stream = new FileInputStream(file)) {
            Image image = new Image(stream);
            imageView.setImage(image);
            imageView.setFitWidth(width);
            imageView.setPreserveRatio(true);
            imageViewWrapper.getStyleClass().add(styleClass);
        } catch (IOException e) {
            throw new MinorException("Error when displaying picture " + file.getAbsolutePath(), e);
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

}
