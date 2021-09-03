package pmb.sort.photos.utils;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
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
     * @return a component holding the picture
     */
    public static BorderPane displayPicture(String absolutePath, String styleClass) {
        ImageView image = new ImageView();
        Button rotate = new Button("btn");
        BorderPane imageWrapper = new BorderPane(image, new Text("title"), null, rotate, null);
        image.setImage(new Image(Constant.FILE_PROTOCOL + absolutePath, 400, 0, true, false));
        imageWrapper.getStyleClass().add(styleClass);
        imageWrapper.setPrefWidth(400);
        rotate.setOnAction(e -> image.setRotate(image.getRotate() - 90));
        return imageWrapper;
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
    public static Parent load(String filePath, Locale locale) throws IOException {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n", locale);
        return FXMLLoader.load(Main.class.getResource(filePath), bundle);
    }

}
