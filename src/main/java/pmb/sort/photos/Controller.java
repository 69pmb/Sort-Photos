package pmb.sort.photos;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyProperties;

public class Controller implements Initializable {
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
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle(resources.getString("directory.chooser.title"));
        dirChooser.setInitialDirectory(selectedDir);
        selectedDir = dirChooser.showDialog(container.getScene().getWindow());
        if (selectedDir != null) {
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
