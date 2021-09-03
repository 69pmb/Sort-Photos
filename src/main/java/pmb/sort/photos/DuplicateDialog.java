package pmb.sort.photos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
import pmb.my.starter.exception.MinorException;
import pmb.my.starter.utils.MyConstant;
import pmb.sort.photos.model.Picture;
import pmb.sort.photos.utils.Constant;
import pmb.sort.photos.utils.JavaFxUtils;

/**
 * Component asking the user how to process two similar pictures.
 */
public class DuplicateDialog
        extends Stage {

    private static final Logger LOG = LogManager.getLogger(DuplicateDialog.class);

    private GridPane container;
    private ResourceBundle bundle;

    public DuplicateDialog(GridPane container, ResourceBundle bundle, Picture picture, String newPath, Picture existingPicture, String count) {
        LOG.debug("Start duplicateDialog");
        this.container = container;
        this.bundle = bundle;
        Stage dialog = new Stage();
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
        JavaFxUtils.buildButton(hBox, bundle.getString("duplicate.button.overwrite"), e -> overwrite(picture, newPath, existingPicture, dialog));
        JavaFxUtils.buildButton(hBox, bundle.getString("duplicate.button.rename"), e -> renameWithSuffix(picture, newPath, dialog));
        JavaFxUtils.buildButton(hBox, bundle.getString("duplicate.button.delete"), e -> delete(picture, dialog));
        hBox.setSpacing(10);

        root.add(gridPane, 1, 1);
        root.add(hBox, 1, 2);
        GridPane.setMargin(hBox, new Insets(10));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Controller.class.getResource(Constant.CSS_FILE).toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
        LOG.debug("End duplicateDialog");
    }

    private void renameWithSuffix(Picture picture, String newPath, Stage dialog) {
        LOG.debug("Suffix");

        Integer index = 1;
        String bareNewPath = StringUtils.substringBeforeLast(newPath, MyConstant.DOT);
        String suffix = StringUtils.substringAfterLast(bareNewPath, Constant.SUFFIX_SEPARATOR);
        if (StringUtils.isNumeric(suffix)) {
            index = Integer.valueOf(suffix) + 1;
        }
        String suffixedPath = StringUtils.substringBeforeLast(bareNewPath, Constant.SUFFIX_SEPARATOR) + Constant.SUFFIX_SEPARATOR + index
                + MyConstant.DOT + picture.getExtension();

        File suffixedFile = new File(suffixedPath);
        dialog.close();
        if (!suffixedFile.exists()) {
            try {
                Files.move(picture.toPath(), suffixedFile.toPath());
            } catch (IOException e1) {
                throw new MinorException("Error renaming file " + picture.getPath() + " to " + newPath, e1);
            }
        } else {
            new DuplicateDialog(container, bundle, picture, suffixedPath, new Picture(suffixedFile), "");
        }
    }

    private static void delete(Picture picture, Stage dialog) {
        LOG.debug("Delete");
        try {
            dialog.close();
            Files.deleteIfExists(picture.toPath());
        } catch (IOException e1) {
            throw new MinorException("Error deleting file " + picture.getPath(), e1);
        }
    }

    private static void overwrite(Picture picture, String newPath, Picture existingPicture, Stage dialog) {
        LOG.debug("Overwrite");
        try {
            dialog.close();
            Files.move(picture.toPath(), existingPicture.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e1) {
            throw new MinorException("Error moving file " + picture.getPath() + " to " + newPath, e1);
        }
    }

}
