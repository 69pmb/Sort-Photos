package pmb.sort.photos;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pmb.sort.photos.model.Picture;
import pmb.sort.photos.utils.JavaFxUtils;

public class NoTakenDateTask implements Callable<Triple<Optional<Date>, Boolean, Boolean>> {

  private static final Logger LOG = LogManager.getLogger(NoTakenDateTask.class);

  private Picture picture;
  private Date fallbackDate;
  private String message;
  private String alertMessage;
  private String checkboxLabel;
  private String stopLabel;

  NoTakenDateTask(
      Picture picture,
      Date fallbackDate,
      String message,
      String alertMessage,
      String checkboxLabel,
      String stopLabel) {
    this.picture = picture;
    this.fallbackDate = fallbackDate;
    this.message = message;
    this.alertMessage = alertMessage;
    this.checkboxLabel = checkboxLabel;
    this.stopLabel = stopLabel;
  }

  @Override
  public Triple<Optional<Date>, Boolean, Boolean> call() throws Exception {
    LOG.info("No taken date for picture: {}", picture.getPath());
    AtomicBoolean showAgain = new AtomicBoolean();
    AtomicBoolean stop = new AtomicBoolean();
    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.getDialogPane().applyCss();
    Node graphic = alert.getDialogPane().getGraphic();
    alert.setDialogPane(
        new DialogPane() {
          @Override
          protected Node createDetailsButton() {
            HBox box = new HBox();
            box.setSpacing(10D);
            box.setAlignment(Pos.CENTER);
            JavaFxUtils.buildButton(
                box,
                stopLabel,
                e -> {
                  alert.close();
                  stop.set(true);
                });
            CheckBox checkBox = new CheckBox(checkboxLabel);
            checkBox.setOnAction(e -> showAgain.set(checkBox.isSelected()));
            box.getChildren().add(checkBox);
            return box;
          }
        });
    DialogPane dialogPane = alert.getDialogPane();
    dialogPane.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
    dialogPane.setContentText(message);
    dialogPane.setExpandableContent(new Group());
    dialogPane.setExpanded(true);
    alert.getDialogPane().setGraphic(graphic);
    alert.setHeaderText(
        MessageFormat.format(alertMessage, StringUtils.abbreviate(picture.getName(), 40)));
    alert.setResizable(true);
    dialogPane.setMinHeight(Region.USE_PREF_SIZE);
    if (alert.showAndWait().map(response -> response == ButtonType.YES).orElse(false)) {
      return Triple.of(Optional.ofNullable(fallbackDate), showAgain.get(), false);
    } else {
      LOG.debug("Picture is ignored: {}", picture.getPath());
      return Triple.of(Optional.empty(), showAgain.get(), stop.get());
    }
  }
}
