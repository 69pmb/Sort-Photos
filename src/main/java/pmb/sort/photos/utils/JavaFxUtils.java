package pmb.sort.photos.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import pmb.my.starter.exception.MinorException;
import pmb.sort.photos.Main;

/**
 * Toolbox for JavaFx component.
 */
public final class JavaFxUtils {

    private JavaFxUtils() {
        throw new AssertionError("Must not be used");
    }

    /**
     * Displays a picture.
     *
     * @param absolutePath picture path
     * @param styleClass css class of the component
     * @param width of the picture
     * @return a component holding the picture
     */
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

    /**
     * Builds a button.
     *
     * @param parent component
     * @param label of the button
     * @param action when clicked
     * @return the built button
     */
    public static Button buildButton(Pane parent, String label, EventHandler<ActionEvent> action) {
        Button button = new Button(label);
        button.setAlignment(Pos.CENTER);
        button.setOnAction(action::handle);
        parent.getChildren().add(button);
        return button;
    }

    /**
     * Builds a {@link ScrollPane} with the given {@link Pane}.
     *
     * @param pane child pane
     * @param prefHeight pref scroll height
     * @param maxHeight max scroll height
     */
    public static void buildScrollPane(Pane pane, double prefHeight, double maxHeight) {
        ScrollPane scroll = new ScrollPane(pane);
        scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scroll.setPrefHeight(prefHeight);
        scroll.setMaxHeight(maxHeight);
    }

    /**
     * Builds a text component.
     *
     * @param str label
     * @param wrappingWidth width
     * @return the built text
     */
    public static Text buildText(String str, int wrappingWidth) {
        Text text = new Text(str);
        text.setWrappingWidth(wrappingWidth);
        text.setTextAlignment(TextAlignment.JUSTIFY);
        return text;
    }

    /**
     * Loads an object from a FXML document for a given language.
     *
     * @param filePath FXML file path to load
     * @param locale of the wanted language
     *
     * @return loaded object
     * @throws IOException if an error occurs during loading
     * @see {@link FXMLLoader#load(java.net.URL, ResourceBundle)}
     */
    public static <T> T load(String filePath, Locale locale) throws IOException {
        return loader(filePath, locale).<T> load();
    }

    /**
     * Build loader object from a FXML document for a given language.
     *
     * @param filePath FXML file path to load
     * @param locale of the wanted language
     *
     * @return loader object
     * @see {@link FXMLLoader#FXMLLoader(java.net.URL, ResourceBundle)
     */
    public static FXMLLoader loader(String filePath, Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n", locale);
        return new FXMLLoader(Main.class.getResource(filePath), bundle);
    }

}
