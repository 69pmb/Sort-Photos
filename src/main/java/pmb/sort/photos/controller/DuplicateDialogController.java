package pmb.sort.photos.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import pmb.my.starter.exception.MinorException;
import pmb.my.starter.utils.MyConstant;
import pmb.sort.photos.model.Picture;
import pmb.sort.photos.utils.Constant;

/**
 * Component asking the user how to process two similar pictures.
 */
public class DuplicateDialogController
        implements Initializable {

    @FXML
    protected ImageView viewer;
    @FXML
    protected ImageView existingViewer;
    @FXML
    protected Text detail;
    @FXML
    protected Text existingDetail;
    @FXML
    protected Text message;
    @FXML
    protected ScrollPane dialogContainer;

    private Stage dialog;
    private String newPath;
    private Picture existingPicture;
    private Picture picture;

    private static final Logger LOG = LogManager.getLogger(DuplicateDialogController.class);

    @Override
    public void initialize(URL arg0, ResourceBundle bundle) {}

    public void show(Window owner, String newPath, String count, Picture picture, Picture existingPicture) {
        LOG.debug("Start duplicateDialog");
        this.picture = picture;
        this.newPath = newPath;
        this.existingPicture = existingPicture;
        viewer.setImage(new Image(Constant.FILE_PROTOCOL + picture.getPath()));

        // existingViewerUrl = Constant.FILE_PROTOCOL + existingPicture.getPath();
        // detail.setText(picture.prettyPrint(bundle));
        // existingDetail.setText(existingPicture.prettyPrint(bundle));
        // message.setText(bundle.getString(picture.equals(existingPicture) ? "duplicate.warning.equals" : "duplicate.warning"));

        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(count);
        dialog.showAndWait();
        LOG.debug("End duplicateDialog");
    }

    @FXML
    protected void cancel() {
        LOG.debug("Do nothing");
        dialog.close();
    }

    @FXML
    protected void renameWithSuffix() {
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
            // new DuplicateDialogController(container, bundle, picture, suffixedPath, new Picture(suffixedFile), "");
        }
    }

    @FXML
    protected void delete() {
        LOG.debug("Delete");
        try {
            dialog.close();
            Files.deleteIfExists(picture.toPath());
        } catch (IOException e1) {
            throw new MinorException("Error deleting file " + picture.getPath(), e1);
        }
    }

    @FXML
    protected void overwrite() {
        LOG.debug("Overwrite");
        try {
            dialog.close();
            Files.move(picture.toPath(), existingPicture.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e1) {
            throw new MinorException("Error moving file " + picture.getPath() + " to " + newPath, e1);
        }
    }

    public Stage getDialog() {
        return dialog;
    }

    public void setDialog(Stage dialog) {
        this.dialog = dialog;
    }

}
