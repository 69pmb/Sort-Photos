package pmb.sort.photos.utils;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import pmb.sort.photos.Main;

/** Toolbox for JavaFx component. */
public final class JavaFxUtils {

  private JavaFxUtils() {
    throw new AssertionError("Must not be used");
  }

  private static final Double PICTURE_WIDTH = 500D;

  /**
   * Displays a picture.
   *
   * @param absolutePath picture path
   * @param styleClass css class of the component
   * @param rotateBtnLabel label of the rotation button
   * @param imageTitle title above the image
   * @return a component holding the picture
   */
  public static BorderPane displayPicture(
      String absolutePath, String styleClass, String rotateBtnLabel, String imageTitle) {
    Double pictureHeight = Screen.getPrimary().getBounds().getHeight() - 300;
    ImageView image = new ImageView();
    Button rotate = new Button(rotateBtnLabel);
    image.setImage(new Image(Constant.FILE_PROTOCOL + absolutePath));
    Label label = new Label(imageTitle);
    label.setStyle("-fx-font-size: 16px");
    BorderPane imageWrapper = new BorderPane(image, label, null, rotate, null);
    BorderPane.setAlignment(label, Pos.CENTER);
    BorderPane.setAlignment(rotate, Pos.CENTER);
    imageWrapper.getStyleClass().add(styleClass);
    imageWrapper.setMinWidth(PICTURE_WIDTH);
    imageWrapper.setPrefWidth(PICTURE_WIDTH);
    imageWrapper.setMinHeight(pictureHeight + 60);
    imageWrapper.setPrefHeight(pictureHeight + 60);
    double height = image.getImage().getHeight();
    double width = image.getImage().getWidth();
    double ratio = width > height ? (pictureHeight - 120) / width : PICTURE_WIDTH / height;
    double pivotX = width / 2.0;
    double pivotY = height / 2.0;
    Scale scaleBig = new Scale(ratio, ratio, pivotX, pivotY);
    Scale scaleSmall;
    scaleSmall =
        width > height
            ? new Scale(PICTURE_WIDTH / width, PICTURE_WIDTH / width, pivotX, pivotY)
            : new Scale(
                (pictureHeight - 120) / height, (pictureHeight - 120) / height, pivotX, pivotY);
    image.getTransforms().add(scaleSmall);
    rotate.setOnAction(
        e -> {
          double angle = image.getRotate() - 90;
          if (angle / 90.0 % 2 != 0) {
            image.getTransforms().remove(scaleSmall);
            image.getTransforms().add(scaleBig);
          } else {
            image.getTransforms().remove(scaleBig);
            image.getTransforms().add(scaleSmall);
          }
          image.setRotate(angle);
        });
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
    Optional.ofNullable(parent).map(Pane::getChildren).ifPresent(children -> children.add(button));
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
   * @return loaded object
   * @throws IOException if an error occurs during loading
   * @see {@link FXMLLoader#load(java.net.URL, ResourceBundle)}
   */
  public static Parent load(String filePath, Locale locale) throws IOException {
    ResourceBundle bundle = ResourceBundle.getBundle("i18n", locale);
    return FXMLLoader.load(Main.class.getResource(filePath), bundle);
  }
}
