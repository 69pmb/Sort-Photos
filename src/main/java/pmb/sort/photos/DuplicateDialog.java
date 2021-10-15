package pmb.sort.photos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pmb.my.starter.exception.MajorException;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.RunnableThrowing;
import pmb.sort.photos.model.Picture;
import pmb.sort.photos.utils.Constant;
import pmb.sort.photos.utils.JavaFxUtils;

/**
 * Component asking the user how to process two similar pictures.
 */
public class DuplicateDialog
        extends Stage {

    private static final Logger LOG = LogManager.getLogger(DuplicateDialog.class);

    private Stage dialog;
    private List<Exception> exceptions;

    public DuplicateDialog(GridPane container, ResourceBundle bundle, Picture picture, Picture existingPicture, String count,
        List<Exception> exceptions) {
        LOG.info("Start duplicateDialog to rename: '{}' with existing picture '{}'", picture.getPath(), existingPicture.getPath());
        this.exceptions = exceptions;
        dialog = new Stage();
        dialog.initOwner(container.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(count);
        GridPane root = new GridPane();
        root.setVgap(10);
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        String btnLabel = bundle.getString("duplicate.button.rotate");
        gridPane.add(JavaFxUtils.displayPicture(picture.getPath(), Constant.CSS_CLASS_BOX, btnLabel, bundle.getString("duplicate.title.rename")), 0, 0);
        gridPane.add(
            JavaFxUtils.displayPicture(existingPicture.getPath(), Constant.CSS_CLASS_BOX, btnLabel, bundle.getString("duplicate.title.existing")), 1, 0);
        gridPane.add(JavaFxUtils.buildText(picture.prettyPrint(bundle), 500), 0, 1);
        gridPane.add(JavaFxUtils.buildText(existingPicture.prettyPrint(bundle), 500), 1, 1);

        // Buttons
        HBox hBox = new HBox();
        hBox.getChildren().add(new Text(bundle.getString(picture.equals(existingPicture) ? "duplicate.warning.equals" : "duplicate.warning")));
        JavaFxUtils.buildButton(hBox, bundle.getString("duplicate.button.cancel"), e -> {
            LOG.debug("Do nothing");
            dialog.close();
        });
        JavaFxUtils.buildButton(hBox, bundle.getString("duplicate.button.overwrite"), e -> duplicateAction(() -> overwrite(picture, existingPicture)));
        JavaFxUtils.buildButton(hBox, bundle.getString("duplicate.button.rename"),
            e -> duplicateAction(() -> renameWithSuffix(container, bundle, exceptions, picture, existingPicture.getPath(), false)));
        JavaFxUtils.buildButton(hBox, bundle.getString("duplicate.button.delete"), e -> duplicateAction(() -> delete(picture)));

        hBox.setSpacing(10);

        root.add(gridPane, 1, 1);
        root.add(hBox, 1, 2);
        GridPane.setMargin(hBox, new Insets(10));

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Controller.class.getResource(Constant.CSS_FILE).toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
        LOG.info("End duplicateDialog");
    }

    public static void renameWithSuffix(GridPane container, ResourceBundle bundle, List<Exception> exceptions, Picture picture, String newPath,
        boolean hideDialog) throws MajorException {
        LOG.debug("Start Suffix");
        Integer index = 1;
        String bareNewPath = StringUtils.substringBeforeLast(newPath, MyConstant.DOT);
        String suffix = StringUtils.substringAfterLast(bareNewPath, Constant.SUFFIX_SEPARATOR);
        if (StringUtils.isNumeric(suffix)) {
            index = Integer.valueOf(suffix) + 1;
        }
        String suffixedPath = StringUtils.substringBeforeLast(bareNewPath, Constant.SUFFIX_SEPARATOR) + Constant.SUFFIX_SEPARATOR + index + MyConstant.DOT
            + picture.getExtension();

        File suffixedFile = new File(suffixedPath);
        if (!suffixedFile.exists()) {
            try {
                LOG.debug("Moving file from: {} to: {}", picture.toPath(), suffixedFile.toPath());
                Files.move(picture.toPath(), suffixedFile.toPath());
            } catch (IOException e1) {
                throw new MajorException("Error renaming file " + picture.getPath() + " to " + newPath, e1);
            }
        } else {
            LOG.debug("Existing picture: {}", suffixedFile.toPath());
            if (!hideDialog) {
                new DuplicateDialog(container, bundle, picture, new Picture(suffixedFile), "", exceptions);
            } else {
                renameWithSuffix(container, bundle, exceptions, picture, suffixedFile.getAbsolutePath(), true);
            }
        }
        LOG.debug("End Suffix");
    }

    private static void delete(Picture picture) throws MajorException {
        LOG.debug("Start Delete");
        try {
            Files.deleteIfExists(picture.toPath());
        } catch (IOException e1) {
            throw new MajorException("Error deleting file " + picture.getPath(), e1);
        }
        LOG.debug("End Delete");
    }

    private static void overwrite(Picture picture, Picture existingPicture) throws MajorException {
        LOG.debug("Start Overwrite");
        try {
            Files.move(picture.toPath(), existingPicture.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e1) {
            throw new MajorException("Error moving file " + picture.getPath() + " to " + existingPicture.getPath(), e1);
        }
        LOG.debug("End Overwrite");
    }

    private void duplicateAction(RunnableThrowing runnable) {
        try {
            runnable.run();
        } catch (MajorException e) {
            exceptions.add(e);
        } finally {
            dialog.close();
        }
    }

}
