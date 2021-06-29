package pmb.sort.photos;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import pmb.my.starter.exception.MinorException;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyFileUtils;
import pmb.my.starter.utils.MyProperties;
import pmb.my.starter.utils.VariousUtils;
import pmb.sort.photos.model.Fallback;
import pmb.sort.photos.model.Picture;
import pmb.sort.photos.model.Property;
import pmb.sort.photos.utils.Constant;
import pmb.sort.photos.utils.MiscUtils;
import pmb.sort.photos.utils.PropertiesUtils;

/**
 *
 */
public class Controller
        implements Initializable {

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
    protected CheckBox overwriteIdentical;
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
    @FXML
    protected RadioButton fallbackEdit;
    @FXML
    protected RadioButton fallbackCreate;
    @FXML
    protected RadioButton fallbackPattern;
    @FXML
    protected TextField pattern;
    protected String defaultDirectory;
    private ResourceBundle bundle;
    private PropertiesUtils propertiesUtils;

    public Controller() {
        // empty
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOG.debug("Start initialize");
        bundle = resources;
        propertiesUtils = new PropertiesUtils(this,
                Map.of(Property.DATE_FORMAT, dateFormat, Property.PICTURE_EXTENSION, pictureExtension, Property.VIDEO_EXTENSION, videoExtension,
                        Property.FALL_BACK_PATTERN, pattern),
                Map.of(Property.ENABLE_FOLDERS_ORGANIZATION, enableFoldersOrganization, Property.OVERWRITE_IDENTICAL, overwriteIdentical));
        selectedDir.setOnKeyReleased(e -> {
            messages.setText("");
            isValidSelectedDirectory(() -> {});
        });
        detectFolder();
        enableFoldersOrganization.setOnAction(e -> disableRadioButtons());
        LOG.debug("End initialize");
    }

    public void openLink() {
        VariousUtils.openUrl("https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/SimpleDateFormat.html");
    }
    
    protected void isValidSelectedDirectory(Runnable action) {
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

    /**
     *
     */
    @FXML
    public void selectDirectory() {
        LOG.debug("Start selectDirectory");
        messages.setText("");
        File inputDir = new File(selectedDir.getText());
        if (!inputDir.exists()) {
            selectedDir.setText(defaultDirectory);
            inputDir = new File(defaultDirectory);
            isValidSelectedDirectory(() -> {});
        }
        File dir = chooseDirectory(inputDir);
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

    /**
     * @param inputDir
     * @return
     */
    public File chooseDirectory(File inputDir) {
        FileChooser dirChooser = new FileChooser();
        dirChooser.setTitle(bundle.getString("directory.chooser.title"));
        dirChooser.setInitialDirectory(inputDir);
        return dirChooser.showOpenDialog(container.getScene().getWindow());
    }

    /**
     *
     */
    @FXML
    public void saveDefaultDir() {
        propertiesUtils.saveDefaultDir();
    }

    /**
     *
     */
    @FXML
    public void initProperties() {
        propertiesUtils.initProperties();
    }

    public void disableRadioButtons() {
        List.of(radioYear, radioMonth, radioRoot).forEach(radio -> radio.setDisable(!enableFoldersOrganization.isSelected()));
    }

    /**
     *
     */
    @FXML
    public void saveProperties() {
        propertiesUtils.saveProperties();
    }

    /**
     *
     */
    @FXML
    public void process() {
        LOG.debug("Start process");
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat.getText());
        SimpleDateFormat patternSdf = new SimpleDateFormat(pattern.getText());
        List<String> extensions = Arrays.asList(ArrayUtils.addAll(StringUtils.split(pictureExtension.getText(), Constant.EXTENSION_SEPARATOR),
                StringUtils.split(videoExtension.getText(), Constant.EXTENSION_SEPARATOR)));
        List<File> files = MyFileUtils.listFilesInFolder(new File(selectedDir.getText()), extensions, false);
        int size = files.size();
        boolean alertShown = false;
        AtomicBoolean useFallback = new AtomicBoolean(false);
        String message;
        Function<Picture, Date> fallbackDate;
        if (fallbackCreate.isSelected()) {
            message = bundle.getString("alert.create");
            fallbackDate = Picture::getCreation;
        } else if (fallbackEdit.isSelected()) {
            message = bundle.getString("alert.edit");
            fallbackDate = Picture::getModified;
        } else {
            message = MessageFormat.format(bundle.getString("alert.pattern"), pattern.getText());
            fallbackDate = picture -> {
                try {
                    return patternSdf.parse(picture.getName());
                } catch (ParseException e) {
                    throw new MinorException("Error when parsing: " + picture.getName() + " with pattern: " + pattern.getText());
                }
            };
        }
        IntStream.iterate(0, i -> i < size, i -> i + 1).forEach(i -> {
            Picture picture = new Picture(files.get(i));
            picture.getTaken().or(() -> processNoTakenDate(alertShown, useFallback, picture, message, fallbackDate)).ifPresent(date -> {
                String newName = sdf.format(date);
                try {
                    renameFile(picture, newName, new SimpleDateFormat(Constant.YEAR_FORMAT).format(date),
                            new SimpleDateFormat(Constant.MONTH_FORMAT).format(date), i + 1 + "/" + size);
                } catch (IOException e) {
                    throw new MinorException("Error when renaming picture " + picture.getPath() + " to " + newName, e);
                }
            });
        });
        messages.setText(bundle.getString("finished"));
        LOG.debug("End process");
    }

    private Optional<Date> processNoTakenDate(boolean alertShown, AtomicBoolean useFallback, Picture picture, String message,
            Function<Picture, Date> fallbackDate) {
        LOG.warn("No taken date for picture: {}", picture.getPath());
        if (!alertShown) {
            useFallback.set(new Alert(AlertType.CONFIRMATION,
                    MessageFormat.format(bundle.getString("alert.message"), picture.getName()) + MyConstant.NEW_LINE + message, ButtonType.YES,
                    ButtonType.NO).showAndWait().map(response -> response == ButtonType.YES).orElse(false));
        }
        if (useFallback.get()) {
            return Optional.of(fallbackDate.apply(picture));
        } else {
            LOG.info("Picture is ignored");
            return Optional.empty();
        }
    }

    private void renameFile(Picture picture, String newName, String yearFolder, String monthFolder, String count) throws IOException {
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
            if (!newFile.exists() || overwriteIdentical.isSelected() && picture.equals(new Picture(newFile))
                    && !Files.isSameFile(picture.toPath(), newFile.toPath())) {
                Files.move(picture.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else if (!Files.isSameFile(picture.toPath(), newFile.toPath())) {
                new DuplicateDialog(container, bundle, picture, newPath, new Picture(newFile), count);
            }
        }
    }

    /**
     * Determines if the selected directory is either a year folder, a month folder or a root folder (default).
     */
    public void detectFolder() {
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

    public String getSelectedDir() {
        return selectedDir.getText();
    }

    public void setSelectedDir(String text) {
        selectedDir.setText(text);
    }

    public TextField getDateFormat() {
        return dateFormat;
    }

    public TextField getPictureExtension() {
        return pictureExtension;
    }

    public TextField getVideoExtension() {
        return videoExtension;
    }

    public void setMessageProperties(String text) {
        messageProperties.setText(text);
    }

    public void disableGoBtn(Boolean disable) {
        goBtn.setDisable(disable);
    }

    public RadioButton getFallbackEdit() {
        return fallbackEdit;
    }

    public RadioButton getFallbackCreate() {
        return fallbackCreate;
    }

    public RadioButton getFallbackPattern() {
        return fallbackPattern;
    }

    public TextField getPattern() {
        return pattern;
    }

    public String getDefaultDirectory() {
        return defaultDirectory;
    }

    public void setDefaultDirectory(String defaultDirectory) {
        this.defaultDirectory = defaultDirectory;
    }

    public String getLabel(String key) {
        return bundle.getString(key);
    }

}
