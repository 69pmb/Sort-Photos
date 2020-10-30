package pmb.sort.photos;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pmb.my.starter.exception.MinorException;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyFileUtils;
import pmb.my.starter.utils.MyProperties;

public class Controller
        implements Initializable {

    private static final Logger LOG = LogManager.getLogger(Controller.class);
    private static final String SUFFIX_SEPARATOR = "-";
    private static final String CSS_CLASS_ERROR = "error";
    private static final String CSS_CLASS_BOX = "box";
    @FXML
    private GridPane container;
    @FXML
    private TextField displayDir;
    @FXML
    private TextField dateFormat;
    @FXML
    private TextField yearFormat;
    private TextField pictureExtension;
    @FXML
    private TextField monthFormat;
    private TextField videoExtension;
    @FXML
    private Text messageProperties;
    @FXML
    private RadioButton radioYear;
    @FXML
    private RadioButton radioMonth;
    @FXML
    private RadioButton radioRoot;
    private File selectedDir;
    private ResourceBundle bundle;
    private Map<TextField, Property> properties;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOG.debug("Start initialize");
        this.bundle = resources;
        properties = Map.of(dateFormat, Property.DATE_FORMAT, yearFormat, Property.YEAR_FOLDER_FORMAT, monthFormat, Property.MONTH_FOLDER_FORMAT,
                pictureExtension, Property.PICTURE_EXTENSION, videoExtension, Property.VIDEO_EXTENSION);
        properties.forEach((field, prop) -> field.setText(MiscUtils.getDefaultValue(prop)));
        selectedDir = MyProperties.get(Property.DEFAULT_WORKING_DIR.getValue()).map(File::new).filter(File::exists)
                .orElseGet(() -> new File(MyConstant.USER_DIRECTORY));
        displayDir.setText(selectedDir.getAbsolutePath());
        detectFolder();
        LOG.debug("End initialize");
    }

    @FXML
    public void selectDirectory() {
        LOG.debug("Start selectDirectory");
        FileChooser dirChooser = new FileChooser();
        dirChooser.setTitle(bundle.getString("directory.chooser.title"));
        dirChooser.setInitialDirectory(selectedDir);
        selectedDir = dirChooser.showOpenDialog(container.getScene().getWindow());
        if (selectedDir != null) {
            LOG.debug("A directory was selected");
            if (selectedDir.isFile()) {
                selectedDir = selectedDir.getParentFile();
            }
            displayDir.setText(selectedDir.getAbsolutePath());
            detectFolder();
        }
        LOG.debug("End selectDirectory");
    }

    @FXML
    public void saveDefaultDir() {
        LOG.debug("Start saveDefaultDir");
        MyProperties.set(Property.DEFAULT_WORKING_DIR.getValue(), displayDir.getText());
        MyProperties.save();
        LOG.debug("End saveDefaultDir");
    }

    @FXML
    public void saveProperties() {
        LOG.debug("Start saveProperties");
        Supplier<Stream<TextField>> fields = () -> properties.keySet().stream().filter(k -> !displayDir.equals(k));
        List<TextField> blanks = fields.get().filter(MiscUtils.isBlank).collect(Collectors.toList());
        List<TextField> invalidDates = List.of(dateFormat, yearFormat, monthFormat).stream()
                .filter(MiscUtils.validDateFormat.negate().or(MiscUtils.invalidCharacters)).collect(Collectors.toList());
        List<TextField> invalidExtensions = List.of(pictureExtension, videoExtension).stream()
                .filter(MiscUtils.isValidExtension.negate().or(MiscUtils.isInvalidCharacters)).collect(Collectors.toList());
        fields.get().forEach(f -> f.getStyleClass().remove(CSS_CLASS_ERROR));
        Stream.of(blanks, invalidDates, invalidExtensions).flatMap(List::stream).collect(Collectors.toSet())
                .forEach(f -> f.getStyleClass().add(CSS_CLASS_ERROR));

        List<String> messages = new ArrayList<>();
        if (!blanks.isEmpty()) {
            messages.add(bundle.getString("warning.empty"));
        }
        if (!invalidDates.isEmpty()) {
            messages.add(bundle.getString("warning.date.format"));
        }
        if (!invalidExtensions.isEmpty()) {
            messages.add(bundle.getString("warning.extension"));
        }

        if (!messages.isEmpty()) {
            LOG.debug("Incorrect inputs");
            messageProperties.setText(bundle.getString("warning") + StringUtils.join(messages, ","));
        } else {
            LOG.debug("Save properties");
            messageProperties.setText(bundle.getString("properties.saved"));
            properties.entrySet().stream().filter(e -> e.getValue() != Property.DEFAULT_WORKING_DIR)
                    .forEach(e -> MyProperties.set(e.getValue().getValue(), e.getKey().getText()));
            MyProperties.save();
            detectFolder();
        }
        LOG.debug("End saveProperties");
    }

    @FXML
    public void process() {
        LOG.debug("Start process");
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat.getText());
        SimpleDateFormat yearSdf = new SimpleDateFormat(yearFormat.getText());
        SimpleDateFormat monthSdf = new SimpleDateFormat(monthFormat.getText());
        List<String> extensions = Arrays
                .asList(ArrayUtils.addAll(StringUtils.split(pictureExtension.getText(), ","), StringUtils.split(videoExtension.getText(), ",")));
        MyFileUtils.listFilesInFolder(selectedDir, extensions, false).stream().map(Picture::new).forEach(picture -> {
            Date date = picture.getTaken().orElse(picture.getCreation());
            String newName = sdf.format(date);
            try {
                renameFile(picture, newName, yearSdf.format(date), monthSdf.format(date));
            } catch (IOException e) {
                throw new MinorException("Error when renaming picture " + picture.getPath() + " to " + newName, e);
            }
        });
        LOG.debug("End process");
    }

    private void renameFile(Picture picture, String newName, String yearFolder, String monthFolder) throws IOException {
        String newFilename = newName + MyConstant.DOT + picture.getExtension();
        String newPath;

        if (radioRoot.isSelected()) {
            String yearPath = selectedDir.getAbsolutePath() + MyConstant.FS + yearFolder;
            String monthPath = yearPath + MyConstant.FS + monthFolder;
            MyFileUtils.createFolderIfNotExists(yearPath);
            MyFileUtils.createFolderIfNotExists(monthPath);
            newPath = monthPath + MyConstant.FS + newFilename;
        } else if (radioYear.isSelected()) {
            String monthPath = selectedDir.getAbsolutePath() + MyConstant.FS + monthFolder;
            MyFileUtils.createFolderIfNotExists(monthPath);
            newPath = monthPath + MyConstant.FS + newFilename;
        } else {
            newPath = selectedDir.getAbsolutePath() + MyConstant.FS + newFilename;
        }

        File newFile = new File(newPath);
        if (!StringUtils.equals(newPath, picture.getPath()) && !StringUtils.equals(StringUtils.substringBeforeLast(newPath, MyConstant.DOT),
                StringUtils.substringBeforeLast(picture.getPath(), SUFFIX_SEPARATOR))) {
            LOG.info("New path {}", newPath);
            if (!newFile.exists()) {
                Files.move(picture.toPath(), newFile.toPath());
            } else if (!Files.isSameFile(picture.toPath(), newFile.toPath())) {
                duplicateDialog(picture, picture.getPath(), picture.getExtension(), newPath, new Picture(newFile));
            }
        }
    }

    private void duplicateDialog(Picture picture, String absolutePath, String extension, String newPath, Picture newPicture) {
        LOG.debug("Start duplicateDialog");
        Stage dialog = new Stage();
        dialog.initOwner(container.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.add(JavaFxUtils.displayPicture(picture.getPath(), CSS_CLASS_BOX, 600), 1, 1);
        gridPane.add(JavaFxUtils.displayPicture(newPicture.getPath(), CSS_CLASS_BOX, 600), 2, 1);
        Text details = buildDetails(picture);
        gridPane.add(details, 1, 2);
        Text newDetails = buildDetails(newPicture);
        gridPane.add(newDetails, 2, 2);

        // Buttons
        HBox hBox = new HBox();
        String msg;
        if (picture.equals(newPicture)) {
            msg = bundle.getString("duplicate.warning.equals");
        } else {
            msg = bundle.getString("duplicate.warning");
        }
        hBox.getChildren().add(new Text(msg));
        JavaFxUtils.buildButton(hBox, bundle.getString("duplicate.button.cancel"), e -> {
            LOG.debug("Do nothing");
            dialog.close();
        });
        JavaFxUtils.buildButton(hBox, bundle.getString("duplicate.button.overwrite"), e -> {
            try {
                LOG.debug("Overwrite");
                dialog.close();
                Files.move(picture.toPath(), newPicture.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e1) {
                throw new MinorException("Error when moving file " + absolutePath + " to " + newPath, e1);
            }
        });
        JavaFxUtils.buildButton(hBox, bundle.getString("duplicate.button.rename"), e -> {
            Integer index = 1;
            File renamedFile;
            do {
                renamedFile = new File(
                        StringUtils.substringBeforeLast(newPath, MyConstant.DOT) + SUFFIX_SEPARATOR + index + MyConstant.DOT + extension);
                index++;
            } while (renamedFile.exists());
            try {
                LOG.debug("Suffix");
                dialog.close();
                Files.move(picture.toPath(), renamedFile.toPath());
            } catch (IOException e1) {
                throw new MinorException("Error when moving file " + absolutePath + " to " + newPath, e1);
            }
        });
        hBox.setSpacing(10);

        gridPane.add(hBox, 1, 3);
        Scene scene = new Scene(gridPane);
        scene.getStylesheets().add(Controller.class.getResource("application.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
        LOG.debug("End duplicateDialog");
    }

    private Text buildDetails(Picture picture) {
        Text text = new Text();
        text.setWrappingWidth(400);
        text.setTextAlignment(TextAlignment.JUSTIFY);
        Function<Date, String> format = date -> DateFormat.getInstance().format(date);
        List<String> sb = new ArrayList<>();
        BiConsumer<String, String> append = (key, value) -> sb.add(bundle.getString(key) + ": " + value);
        append.accept("duplicate.name", picture.getName());
        append.accept("duplicate.creation_date", format.apply(picture.getCreation()));
        append.accept("duplicate.modification_date", format.apply(picture.getModified()));
        append.accept("duplicate.taken_time", picture.getTaken().map(format::apply).orElse("Not Found"));
        append.accept("duplicate.model", picture.getModel());
        append.accept("duplicate.size", picture.getSize());
        text.setText(sb.stream().collect(Collectors.joining(MyConstant.NEW_LINE)));
        return text;
    }

    private void detectFolder() {
        LOG.debug("Start detectFolder");
        if (MiscUtils.validateDate(yearFormat.getText(), selectedDir.getName())) {
            radioYear.setSelected(true);
        } else if (MiscUtils.validateDate(monthFormat.getText(), selectedDir.getName())) {
            radioMonth.setSelected(true);
        } else {
            radioRoot.setSelected(true);
        }
        LOG.debug("End detectFolder");
    }

}
