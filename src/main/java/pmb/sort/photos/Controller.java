package pmb.sort.photos;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyFileUtils;
import pmb.my.starter.utils.MyProperties;

public class Controller implements Initializable {
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
    private ResourceBundle resources;
    private Map<TextField, Property> properties;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        properties = Map.of(displayDir, Property.DEFAULT_WORKING_DIR, dateFormat, Property.DATE_FORMAT, yearFormat,
                Property.YEAR_FOLDER_FORMAT, monthFormat, Property.MONTH_FOLDER_FORMAT, pictureExtention,
                Property.PICTURE_EXTENTION, videoExtention, Property.VIDEO_EXTENTION);
        properties.forEach((field, prop) -> field.setText(MiscUtils.getDefaultValue(prop)));
        selectedDir = new File(
                MyProperties.getOrDefault(Property.DEFAULT_WORKING_DIR.getValue(), MyConstant.USER_DIRECTORY));
        detectFolder();
    }

    @FXML
    private void selectDirectory() {
        FileChooser dirChooser = new FileChooser();
        dirChooser.setTitle(resources.getString("directory.chooser.title"));
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
                .filter(MiscUtils.validDateFormat.negate().or(MiscUtils.invalidCharacters))
                .collect(Collectors.toList());
        List<TextField> invalidExtentions = List.of(pictureExtention, videoExtention).stream()
                .filter(MiscUtils.validExtention.negate().or(MiscUtils.invalidCharacters)).collect(Collectors.toList());
        fields.get().forEach(f -> f.getStyleClass().remove("error"));
        Stream.of(blanks, invalidDates, invalidExtentions).flatMap(List::stream).collect(Collectors.toSet())
        .forEach(f -> f.getStyleClass().add("error"));

        List<String> messages = new ArrayList<>();
        if (!blanks.isEmpty()) {
            messages.add(resources.getString("warning.empty"));
        }
        if (!invalidDates.isEmpty()) {
            messages.add(resources.getString("warning.date.format"));
        }
        if (!invalidExtentions.isEmpty()) {
            messages.add(resources.getString("warning.extention"));
        }

        if (!messages.isEmpty()) {
            messageProperties.setText(resources.getString("warning") + StringUtils.join(messages, ","));
        } else {
            messageProperties.setText(resources.getString("properties.saved"));
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
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dateFormat.getText());
        DateTimeFormatter yearDtf = DateTimeFormatter.ofPattern(yearFormat.getText());
        DateTimeFormatter monthDtf = DateTimeFormatter.ofPattern(monthFormat.getText());
        List<String> extentions = Arrays.asList(ArrayUtils.addAll(StringUtils.split(pictureExtention.getText(), ","),
                StringUtils.split(videoExtention.getText(), ",")));
        MyFileUtils.listFilesInFolder(selectedDir, extentions, false).forEach(file -> {
            String absolutePath = file.getAbsolutePath();
            try {
                Metadata metadata = ImageMetadataReader.readMetadata(file);
                Directory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                String newName;
                String yearFolder;
                String monthFolder;
                if (directory != null) {
                    Date takenTime = directory.getDate(ExifDirectoryBase.TAG_DATETIME_ORIGINAL, TimeZone.getDefault());
                    newName = sdf.format(takenTime);
                    yearFolder = yearSdf.format(takenTime);
                    monthFolder = monthSdf.format(takenTime);
                    LOG.info("file {} taken at {}", file.getName(), newName);
                } else {
                    LocalDateTime creationDate = MyFileUtils.getCreationDate(file);
                    newName = creationDate.format(dtf);
                    yearFolder = creationDate.format(yearDtf);
                    monthFolder = creationDate.format(monthDtf);
                    LOG.info("Pas d'exif {}, creation date {}", absolutePath, newName);
                }
                renameFile(file, newName, yearFolder, monthFolder);
            } catch (ImageProcessingException | IOException e) {
                LOG.error("Error reading metadata of file: {}", absolutePath);
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
            MyFileUtils.createFolderIfNotExists(selectedDir.getAbsolutePath() + MyConstant.FS + monthFolder);
            newPath = selectedDir.getAbsolutePath() + MyConstant.FS + monthFolder + MyConstant.FS + newFilename;
        } else {
            newPath = selectedDir.getAbsolutePath() + MyConstant.FS + newFilename;
        }

        LOG.info("Path {}", newPath);
        File newFile = new File(newPath);
        if (!newFile.exists()) {
            Files.move(file.toPath(), newFile.toPath());
        }
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
