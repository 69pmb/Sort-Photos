package pmb.sort.photos;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.sort.photos.utils.Constant;

public class I18n implements Initializable {

  private static final Logger LOG = LogManager.getLogger(I18n.class);

  @FXML protected HBox i18nContainer;
  @FXML protected ComboBox<Locale> langBox;

  public I18n() {
    // Empty
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    LOG.debug("Start initialize");
    langBox.setItems(FXCollections.observableArrayList(Constant.AVAILABLE_LANGS));
    langBox.setValue(
        Constant.AVAILABLE_LANGS.stream()
            .filter(l -> l.equals(Locale.getDefault()))
            .findFirst()
            .orElse(Locale.FRENCH));
    langBox.setConverter(
        new StringConverter<>() {

          @Override
          public String toString(Locale object) {
            return WordUtils.capitalize(object.getDisplayLanguage(langBox.getValue()));
          }

          @Override
          public Locale fromString(String string) {
            return null;
          }
        });
    LOG.debug("End initialize");
  }

  @FXML
  public void switchLang() throws IOException {
    LOG.debug("Start switchLang");
    GridPane gridPane = (GridPane) i18nContainer.getParent();
    Scene scene = gridPane.getScene();
    gridPane.getChildren().clear();
    Locale newLocale = langBox.getValue();
    Locale.setDefault(newLocale);
    scene.setRoot(Main.buildApp(newLocale));
    LOG.debug("End switchLang");
  }
}
