package pmb.sort.photos;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
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
    @FXML
    private GridPane container;
    @FXML
    private TextField displayDir;
    @FXML
    private TextField dateFormat;
    @FXML
    private TextField yearFormat;
    @FXML
    private TextField monthFormat;
    @FXML
    private TextField pictureExtention;
    @FXML
    private TextField videoExtention;
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
        this.bundle = resources;
        properties = Map.of(dateFormat, Property.DATE_FORMAT, yearFormat, Property.YEAR_FOLDER_FORMAT, monthFormat, Property.MONTH_FOLDER_FORMAT,
                pictureExtention, Property.PICTURE_EXTENTION, videoExtention, Property.VIDEO_EXTENTION);
        properties.forEach((field, prop) -> field.setText(MiscUtils.getDefaultValue(prop)));
        selectedDir = MyProperties.get(Property.DEFAULT_WORKING_DIR.getValue()).map(File::new).filter(File::exists)
                .orElse(new File(MyConstant.USER_DIRECTORY));
        displayDir.setText(selectedDir.getAbsolutePath());
        detectFolder();
    }

    @FXML
    private void selectDirectory() {
        FileChooser dirChooser = new FileChooser();
        dirChooser.setTitle(bundle.getString("directory.chooser.title"));
        dirChooser.setInitialDirectory(selectedDir);
        selectedDir = dirChooser.showOpenDialog(container.getScene().getWindow());
        if (selectedDir != null) {
            if (selectedDir.isFile()) {
                selectedDir = selectedDir.getParentFile();
            }
            displayDir.setText(selectedDir.getAbsolutePath());
            detectFolder();
        }
    }

    @FXML
    private void saveDefaultDir() {
        MyProperties.set(Property.DEFAULT_WORKING_DIR.getValue(), displayDir.getText());
        MyProperties.save();
    }

    @FXML
    private void saveProperties() {
        Supplier<Stream<TextField>> fields = () -> properties.keySet().stream().filter(k -> !displayDir.equals(k));
        List<TextField> blanks = fields.get().filter(MiscUtils.isBlank).collect(Collectors.toList());
        List<TextField> invalidDates = List.of(dateFormat, yearFormat, monthFormat).stream()
                .filter(MiscUtils.validDateFormat.negate().or(MiscUtils.invalidCharacters)).collect(Collectors.toList());
        List<TextField> invalidExtentions = List.of(pictureExtention, videoExtention).stream()
                .filter(MiscUtils.validExtention.negate().or(MiscUtils.invalidCharacters)).collect(Collectors.toList());
        fields.get().forEach(f -> f.getStyleClass().remove("error"));
        Stream.of(blanks, invalidDates, invalidExtentions).flatMap(List::stream).collect(Collectors.toSet())
                .forEach(f -> f.getStyleClass().add("error"));

        List<String> messages = new ArrayList<>();
        if (!blanks.isEmpty()) {
            messages.add(bundle.getString("warning.empty"));
        }
        if (!invalidDates.isEmpty()) {
            messages.add(bundle.getString("warning.date.format"));
        }
        if (!invalidExtentions.isEmpty()) {
            messages.add(bundle.getString("warning.extention"));
        }

        if (!messages.isEmpty()) {
            messageProperties.setText(bundle.getString("warning") + StringUtils.join(messages, ","));
        } else {
            messageProperties.setText(bundle.getString("properties.saved"));
            properties.entrySet().stream().filter(e -> e.getValue() != Property.DEFAULT_WORKING_DIR)
                    .forEach(e -> MyProperties.set(e.getValue().getValue(), e.getKey().getText()));
            MyProperties.save();
            detectFolder();
        }
    }

    @FXML
    private void process() {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat.getText());
        SimpleDateFormat yearSdf = new SimpleDateFormat(yearFormat.getText());
        SimpleDateFormat monthSdf = new SimpleDateFormat(monthFormat.getText());
        List<String> extentions = Arrays
                .asList(ArrayUtils.addAll(StringUtils.split(pictureExtention.getText(), ","), StringUtils.split(videoExtention.getText(), ",")));
        MyFileUtils.listFilesInFolder(selectedDir, extentions, false).forEach(file -> {
            Date date = MiscUtils.getTakenTime(file).orElseGet(() -> MyFileUtils.getCreationDate(file));
            String newName = sdf.format(date);
            LOG.debug("File {} taken at {}", file.getName(), newName);
            try {
                renameFile(file, newName, yearSdf.format(date), monthSdf.format(date));
            } catch (IOException e) {
                throw new MinorException("Error when renaming file " + file.getAbsolutePath() + " to " + newName, e);
            }
        });
    }

    private void renameFile(File file, String newName, String yearFolder, String monthFolder) throws IOException {
        String absolutePath = file.getAbsolutePath();
        String extention = StringUtils.lowerCase(StringUtils.substringAfterLast(absolutePath, MyConstant.DOT));
        String newFilename = newName + MyConstant.DOT + extention;
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
        if (!StringUtils.equals(newPath, absolutePath) && !Files.isSameFile(file.toPath(), newFile.toPath())) {
            LOG.info("Path {}", newPath);
            if (!newFile.exists()) {
                Files.move(file.toPath(), newFile.toPath());
            } else {
                duplicateDialog(file, absolutePath, extention, newPath, newFile);
            }
        }
    }

    private void duplicateDialog(File file, String absolutePath, String extention, String newPath, File newFile) throws IOException {
        Stage dialog = new Stage();
        dialog.initOwner(container.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.add(JavaFxUtils.displayPicture(file, "box", 600), 1, 1);
        gridPane.add(JavaFxUtils.displayPicture(newFile, "box", 600), 2, 1);
        gridPane.add(buildDescriptions(file), 1, 2);
        gridPane.add(buildDescriptions(newFile), 2, 2);
        HBox hBox = new HBox();
        hBox.getChildren().add(new Text(bundle.getString("duplicate.warning")));
        JavaFxUtils.buildButton(hBox, bundle.getString("duplicate.button.cancel"), e -> dialog.close());
        JavaFxUtils.buildButton(hBox, bundle.getString("duplicate.button.overwrite"), e -> {
            try {
                dialog.close();
                Files.move(file.toPath(), newFile.toPath());
            } catch (IOException e1) {
                throw new MinorException("Error when moving file " + absolutePath + " to " + newPath, e1);
            }
        });
        JavaFxUtils.buildButton(hBox, bundle.getString("duplicate.button.rename"), e -> {
            Integer index = 1;
            File renamedFile;
            do {
                renamedFile = new File(StringUtils.substringBeforeLast(newPath, MyConstant.DOT) + "-" + index + MyConstant.DOT + extention);
                index++;
            } while (renamedFile.exists());
            try {
                dialog.close();
                Files.move(file.toPath(), renamedFile.toPath());
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
    }

    public Text buildDescriptions(File file) throws IOException {
        Text text = new Text();
        text.setWrappingWidth(400);
        text.setTextAlignment(TextAlignment.JUSTIFY);
        BasicFileAttributes basic = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        Function<Instant, String> format = date -> DateFormat.getInstance().format(Date.from(date));
        List<String> sb = new ArrayList<>();
        BiConsumer<String, String> append = (key, value) -> sb.add(bundle.getString(key) + ": " + value);
        append.accept("duplicate.creation_date", format.apply(basic.creationTime().toInstant()));
        append.accept("duplicate.modification_date", format.apply(basic.lastModifiedTime().toInstant()));
        append.accept("duplicate.taken_time", MiscUtils.getTakenTime(file).map(Date::toInstant).map(format::apply).orElse("Not Found"));
        append.accept("duplicate.model", MiscUtils.getModel(file).orElse("Unknown"));
        append.accept("duplicate.size", (basic.size() / 1024) + " KB");
        text.setText(sb.stream().collect(Collectors.joining("\n")));
        return text;
    }

    private void detectFolder() {
        if (MiscUtils.validateDate(yearFormat.getText(), selectedDir.getName())) {
            radioYear.setSelected(true);
        } else if (MiscUtils.validateDate(monthFormat.getText(), selectedDir.getName())) {
            radioMonth.setSelected(true);
        } else {
            radioRoot.setSelected(true);
        }
    }

}
