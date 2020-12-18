package pmb.sort.photos;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pmb.my.starter.exception.MinorException;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyFileUtils;
import pmb.my.starter.utils.MyProperties;
import pmb.sort.photos.model.Picture;
import pmb.sort.photos.model.Property;
import pmb.sort.photos.utils.Constant;
import pmb.sort.photos.utils.JavaFxUtils;
import pmb.sort.photos.utils.MiscUtils;

public class Controller implements Initializable {

    private static final Logger LOG = LogManager.getLogger(Controller.class);

    @FXML
    protected GridPane container;
    @FXML
    protected TextField selectedDir;
    @FXML
    protected TextField dateFormat;
    @FXML
    protected TextField pictureExtension;
    @FXML
    protected TextField videoExtension;
    @FXML
    protected CheckBox enableFoldersOrganization;
    @FXML
    protected Text messageProperties;
    @FXML
    protected RadioButton radioYear;
    @FXML
    protected RadioButton radioMonth;
    @FXML
    protected RadioButton radioRoot;
    @FXML
    protected Button saveDirBtn;
    @FXML
    protected Button goBtn;
    @FXML
    protected Text messages;
    private ResourceBundle bundle;
    private Map<TextField, Property> properties;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOG.debug("Start initialize");
        this.bundle = resources;
        initProperties();
        selectedDir.setText(
                MyProperties.get(Property.DEFAULT_WORKING_DIR.getValue()).filter(path -> new File(path).exists()).orElse(MyConstant.USER_DIRECTORY));
        selectedDir.setOnKeyReleased(e -> {
            messages.setText("");
            isValidSelectedDirectory(() -> {});
        });
        detectFolder();
        enableFoldersOrganization.setOnAction(e -> disableRadioButtons());
        LOG.debug("End initialize");
    }

    private void isValidSelectedDirectory(Runnable action) {
        String dir = selectedDir.getText();
        File file = new File(dir);
        if (StringUtils.isNotBlank(dir) && file.exists()) {
            selectedDir.getStyleClass().removeAll(Constant.CSS_CLASS_ERROR);
            goBtn.setDisable(false);
            saveDirBtn.setDisable(false);
            detectFolder();
            action.run();
        } else {
            selectedDir.getStyleClass().add(Constant.CSS_CLASS_ERROR);
            goBtn.setDisable(true);
            saveDirBtn.setDisable(true);
        }
    }

    @FXML
    public void selectDirectory() {
        LOG.debug("Start selectDirectory");
        messages.setText("");
        File dir = chooseDirectory();
        if (dir != null) {
            LOG.debug("A directory was selected");
            if (dir.isFile()) {
                dir = dir.getParentFile();
            }
            selectedDir.setText(dir.getAbsolutePath());
            goBtn.setDisable(false);
            saveDirBtn.setDisable(false);
            detectFolder();
        } else {
            goBtn.setDisable(true);
            saveDirBtn.setDisable(true);
        }
        LOG.debug("End selectDirectory");
    }

    public File chooseDirectory() {
        FileChooser dirChooser = new FileChooser();
        dirChooser.setTitle(bundle.getString("directory.chooser.title"));
        dirChooser.setInitialDirectory(new File(selectedDir.getText()));
        return dirChooser.showOpenDialog(container.getScene().getWindow());
    }

    @FXML
    public void saveDefaultDir() {
        LOG.debug("Start saveDefaultDir");
        isValidSelectedDirectory(() -> {
            MyProperties.set(Property.DEFAULT_WORKING_DIR.getValue(), selectedDir.getText());
            MyProperties.save();
        });
        LOG.debug("End saveDefaultDir");
    }

    @FXML
    public void initProperties() {
        LOG.debug("Start resetProperties");
        properties = Map.of(dateFormat, Property.DATE_FORMAT, pictureExtension, Property.PICTURE_EXTENSION,
                videoExtension, Property.VIDEO_EXTENSION);
        properties.forEach((field, prop) -> {
            field.setText(MiscUtils.getDefaultValue(prop));
            field.getStyleClass().removeAll(Constant.CSS_CLASS_ERROR);
        });
        enableFoldersOrganization.setSelected(BooleanUtils.toBoolean(MiscUtils.getDefaultValue(Property.ENABLE_FOLDERS_ORGANIZATION)));
        disableRadioButtons();
        LOG.debug("End resetProperties");
    }

    private void disableRadioButtons() {
        List.of(radioYear, radioMonth, radioRoot).forEach(radio -> radio.setDisable(!enableFoldersOrganization.isSelected()));
    }

    @FXML
    public void saveProperties() {
        LOG.debug("Start saveProperties");
        List<String> warnings = inputsValidation();

        if (!warnings.isEmpty()) {
            LOG.debug("Incorrect inputs");
            goBtn.setDisable(true);
            messageProperties.setText(bundle.getString("warning") + StringUtils.join(warnings, ","));
        } else {
            LOG.debug("Save properties");
            goBtn.setDisable(false);
            messageProperties.setText(bundle.getString("properties.saved"));
            properties.entrySet().stream().forEach(e -> MyProperties.set(e.getValue().getValue(), e.getKey().getText()));
            MyProperties.set(Property.ENABLE_FOLDERS_ORGANIZATION.getValue(), Boolean.toString(enableFoldersOrganization.isSelected()));
            MyProperties.save();
            detectFolder();
        }
        LOG.debug("End saveProperties");
    }

    private List<String> inputsValidation() {
        List<TextField> blanks = properties.keySet().stream().filter(MiscUtils.isBlank).collect(Collectors.toList());
        Optional<TextField> invalidDate = Optional.of(dateFormat).filter(MiscUtils.isValidDateFormat.negate().or(MiscUtils.isInvalidCharacters));
        List<TextField> invalidExtensions = List.of(pictureExtension, videoExtension).stream()
                .filter(MiscUtils.isValidExtension.negate().or(MiscUtils.isInvalidCharacters)).collect(Collectors.toList());
        properties.keySet().stream().forEach(f -> f.getStyleClass().removeAll(Constant.CSS_CLASS_ERROR));
        Stream.of(blanks, invalidDate.map(List::of).orElse(new ArrayList<>()), invalidExtensions).flatMap(List::stream).collect(Collectors.toSet())
                .forEach(f -> f.getStyleClass().add(Constant.CSS_CLASS_ERROR));

        List<String> warnings = new ArrayList<>();
        if (!blanks.isEmpty()) {
            warnings.add(bundle.getString("warning.empty"));
        }
        if (invalidDate.isPresent()) {
            warnings.add(bundle.getString("warning.date.format"));
        }
        if (!invalidExtensions.isEmpty()) {
            warnings.add(bundle.getString("warning.extension"));
        }
        return warnings;
    }

    @FXML
    public void process() {
        LOG.debug("Start process");
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat.getText());
        List<String> extensions = Arrays
                .asList(ArrayUtils.addAll(StringUtils.split(pictureExtension.getText(), Constant.EXTENSION_SEPARATOR),
                        StringUtils.split(videoExtension.getText(), Constant.EXTENSION_SEPARATOR)));
        List<File> files = MyFileUtils.listFilesInFolder(new File(selectedDir.getText()), extensions, false);
        int size = files.size();
        IntStream.iterate(0, i -> i < size, i -> i + 1).forEach(i -> {
            Picture picture = new Picture(files.get(i));
            if (picture.getTaken().isPresent()) {
                Date date = picture.getTaken().get();
                String newName = sdf.format(date);
                try {
                    renameFile(picture, newName, new SimpleDateFormat(Constant.YEAR_FORMAT).format(date),
                            new SimpleDateFormat(Constant.MONTH_FORMAT).format(date), (i + 1) + "/" + size);
                } catch (IOException e) {
                    throw new MinorException("Error when renaming picture " + picture.getPath() + " to " + newName, e);
                }
            } else {
                LOG.warn("No taken date for picture: {}", picture.getPath());
            }
        });
        messages.setText(bundle.getString("finished"));
        LOG.debug("End process");
    }

    private void renameFile(Picture picture, String newName, String yearFolder, String monthFolder, String count)
            throws IOException {
        String newFilename = newName + MyConstant.DOT + picture.getExtension();
        String newPath;

        if (enableFoldersOrganization.isSelected() && radioRoot.isSelected()) {
            String yearPath = selectedDir.getText() + MyConstant.FS + yearFolder;
            String monthPath = yearPath + MyConstant.FS + monthFolder;
            MyFileUtils.createFolderIfNotExists(yearPath);
            MyFileUtils.createFolderIfNotExists(monthPath);
            newPath = monthPath + MyConstant.FS + newFilename;
        } else if (enableFoldersOrganization.isSelected() && radioYear.isSelected()) {
            String monthPath = selectedDir.getText() + MyConstant.FS + monthFolder;
            MyFileUtils.createFolderIfNotExists(monthPath);
            newPath = monthPath + MyConstant.FS + newFilename;
        } else {
            newPath = selectedDir.getText() + MyConstant.FS + newFilename;
        }

        File newFile = new File(newPath);
        if (!StringUtils.equals(newPath, picture.getPath()) && !StringUtils.equals(StringUtils.substringBeforeLast(newPath, MyConstant.DOT),
                StringUtils.substringBeforeLast(picture.getPath(), Constant.SUFFIX_SEPARATOR))) {
            LOG.info("New path {} for {}", newPath, picture.getPath());
            if (!newFile.exists()) {
                Files.move(picture.toPath(), newFile.toPath());
            } else if (!Files.isSameFile(picture.toPath(), newFile.toPath())) {
                duplicateDialog(picture, newPath, new Picture(newFile), count);
            }
        }
    }

    private void duplicateDialog(Picture picture, String newPath, Picture existingPicture, String count) {
        LOG.debug("Start duplicateDialog");
        Stage dialog = new Stage();
        dialog.initOwner(container.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(count);
        GridPane root = new GridPane();
        root.setVgap(10);
        GridPane gridPane = new GridPane();
        JavaFxUtils.buildScrollPane(gridPane, 500, 700);
        gridPane.setHgap(10);
        gridPane.add(JavaFxUtils.displayPicture(picture.getPath(), Constant.CSS_CLASS_BOX, 600), 1, 1);
        gridPane.add(JavaFxUtils.displayPicture(existingPicture.getPath(), Constant.CSS_CLASS_BOX, 600), 2, 1);
        gridPane.add(JavaFxUtils.buildText(picture.prettyPrint(bundle), 400), 1, 2);
        gridPane.add(JavaFxUtils.buildText(existingPicture.prettyPrint(bundle), 400), 2, 2);

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

    private void delete(Picture picture, Stage dialog) {
        LOG.debug("Delete");
        try {
            dialog.close();
            Files.deleteIfExists(picture.toPath());
        } catch (IOException e1) {
            throw new MinorException("Error deleting file " + picture.getPath(), e1);
        }
    }

    private void overwrite(Picture picture, String newPath, Picture existingPicture, Stage dialog) {
        LOG.debug("Overwrite");
        try {
            dialog.close();
            Files.move(picture.toPath(), existingPicture.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e1) {
            throw new MinorException("Error moving file " + picture.getPath() + " to " + newPath, e1);
        }
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
            duplicateDialog(picture, suffixedPath, new Picture(suffixedFile), "");
        }
    }

    private void detectFolder() {
        LOG.debug("Start detectFolder");
        File folder = new File(selectedDir.getText());
        if (MiscUtils.isValidRegex.test(folder.getName(), Constant.YEAR_REGEX)) {
            radioYear.setSelected(true);
        } else if (MiscUtils.isValidRegex.test(folder.getName(), Constant.MONTH_REGEX)) {
            radioMonth.setSelected(true);
        } else {
            radioRoot.setSelected(true);
        }
        LOG.debug("End detectFolder");
    }

}
