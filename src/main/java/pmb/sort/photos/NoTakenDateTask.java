package pmb.sort.photos;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Region;
import pmb.sort.photos.model.Picture;

public class NoTakenDateTask implements Callable<Pair<Optional<Date>, Boolean>> {

    private static final Logger LOG = LogManager.getLogger(NoTakenDateTask.class);

    private Picture picture;
    private Date fallbackDate;
    private String message;
    private String alertMessage;
    private String checkboxLabel;

    NoTakenDateTask(Picture picture, Date fallbackDate, String message, String alertMessage, String checkboxLabel) {
        this.picture = picture;
        this.fallbackDate = fallbackDate;
        this.message = message;
        this.alertMessage = alertMessage;
        this.checkboxLabel = checkboxLabel;
    }

    @Override
    public Pair<Optional<Date>, Boolean> call() throws Exception {
        LOG.info("No taken date for picture: {}", picture.getPath());
        AtomicBoolean result = new AtomicBoolean();
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.getDialogPane().applyCss();
        Node graphic = alert.getDialogPane().getGraphic();
        alert.setDialogPane(new DialogPane() {
            @Override
            protected Node createDetailsButton() {
                CheckBox checkBox = new CheckBox(checkboxLabel);
                checkBox.setOnAction(e -> result.set(checkBox.isSelected()));
                return checkBox;
            }
        });
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        dialogPane.setContentText(message);
        dialogPane.setExpandableContent(new Group());
        dialogPane.setExpanded(true);
        alert.getDialogPane().setGraphic(graphic);
        alert.setHeaderText(MessageFormat.format(alertMessage, StringUtils.abbreviate(picture.getName(), 40)));
        alert.setResizable(true);
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);
        if (alert.showAndWait().map(response -> response == ButtonType.YES).orElse(false)) {
            return Pair.of(Optional.ofNullable(fallbackDate), result.get());
        } else {
            LOG.debug("Picture is ignored: {}", picture.getPath());
            return Pair.of(Optional.empty(), result.get());
        }
    }
}
