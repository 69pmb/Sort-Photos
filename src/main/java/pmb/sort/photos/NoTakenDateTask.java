package pmb.sort.photos;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import pmb.my.starter.utils.MyConstant;
import pmb.sort.photos.model.Picture;

public class NoTakenDateTask
    implements Callable<Optional<Date>> {

    private static final Logger LOG = LogManager.getLogger(NoTakenDateTask.class);

    private Picture picture;
    private Date fallbackDate;
    private String message;
    private String alertMessage;

    NoTakenDateTask(Picture picture, Date fallbackDate, String message, String alertMessage) {
        this.picture = picture;
        this.fallbackDate = fallbackDate;
        this.message = message;
        this.alertMessage = alertMessage;
    }

    @Override
    public Optional<Date> call() throws Exception {
        LOG.info("No taken date for picture: {}", picture.getPath());
        Alert alert = new Alert(AlertType.CONFIRMATION,
            MessageFormat.format(alertMessage, StringUtils.abbreviate(picture.getName(), 40)) + MyConstant.NEW_LINE + message, ButtonType.YES,
            ButtonType.NO);
        alert.setResizable(true);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        if (alert.showAndWait().map(response -> response == ButtonType.YES).orElse(false)) {
            return Optional.ofNullable(fallbackDate);
        } else {
            LOG.debug("Picture is ignored");
            return Optional.empty();
        }

    }

}
