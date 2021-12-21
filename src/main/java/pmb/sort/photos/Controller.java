package pmb.sort.photos;

import java.io.File;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pmb.my.starter.exception.MajorException;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyFileUtils;
import pmb.my.starter.utils.MyProperties;
import pmb.my.starter.utils.VariousUtils;
import pmb.sort.photos.model.Fallback;
import pmb.sort.photos.model.Picture;
import pmb.sort.photos.model.ProcessParams;
import pmb.sort.photos.model.Property;
import pmb.sort.photos.utils.Constant;
import pmb.sort.photos.utils.MiscUtils;

public class Controller implements Initializable {

  private static final Logger LOG = LogManager.getLogger(Controller.class);

  @FXML protected GridPane container;
  @FXML protected TextField selectedDir;
  @FXML protected TextField dateFormat;
  @FXML protected TextField pictureExtension;
  @FXML protected TextField videoExtension;
  @FXML protected CheckBox enableFoldersOrganization;
  @FXML protected CheckBox overwriteIdentical;
  @FXML protected CheckBox suffixAuto;
  @FXML protected CheckBox ignoreFormated;
  @FXML protected CheckBox ignoreNoDate;
  @FXML protected Text messageProperties;
  @FXML protected RadioButton radioYear;
  @FXML protected RadioButton radioMonth;
  @FXML protected RadioButton radioRoot;
  @FXML protected Button saveDirBtn;
  @FXML protected Button processBtn;
  @FXML protected Button stopBtn;
  @FXML protected Button saveProperties;
  @FXML protected Text messages;
  @FXML protected RadioButton fallbackEdit;
  @FXML protected RadioButton fallbackCreate;
  @FXML protected RadioButton fallbackPattern;
  @FXML protected TextField pattern;
  @FXML protected ProgressBar progressBar;
  @FXML protected Label progressText;
  protected String defaultDirectory;
  private ResourceBundle bundle;
  private Map<Property, TextField> textProperties;
  private Map<Property, CheckBox> boxProperties;
  private Map<String, String> warnings = new HashMap<>();

  public Controller() {
    // Empty
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    LOG.debug("Start initialize");
    bundle = resources;
    setProgressVisibility(false);
    MyProperties.setConfigPath(MyConstant.CONFIGURATION_FILENAME);
    initProperties();
    defaultDirectory =
        MyProperties.get(Property.DEFAULT_WORKING_DIR.getValue())
            .filter(path -> new File(path).exists())
            .orElse(MyConstant.USER_DIRECTORY);
    selectedDir.setText(defaultDirectory);
    selectedDir.setOnKeyReleased(
        e -> {
          messages.setText("");
          isValidSelectedDirectory(() -> {});
        });
    detectFolder();
    enableFoldersOrganization.setOnAction(e -> disableRadioButtons());
    LOG.debug("End initialize");
  }

  public void openLink() {
    VariousUtils.openUrl(
        "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/SimpleDateFormat.html");
  }

  protected void isValidSelectedDirectory(Runnable action) {
    String dir = selectedDir.getText();
    File file = new File(dir);
    if (StringUtils.isNotBlank(dir) && file.exists()) {
      selectedDir.pseudoClassStateChanged(
          PseudoClass.getPseudoClass(Constant.CSS_CLASS_ERROR), false);
      processBtn.setDisable(false);
      saveDirBtn.setDisable(false);
      detectFolder();
      action.run();
    } else {
      selectedDir.pseudoClassStateChanged(
          PseudoClass.getPseudoClass(Constant.CSS_CLASS_ERROR), true);
      processBtn.setDisable(true);
      saveDirBtn.setDisable(true);
    }
  }

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
      processBtn.setDisable(false);
      saveDirBtn.setDisable(false);
      detectFolder();
    }
    LOG.debug("End selectDirectory");
  }

  public File chooseDirectory(File inputDir) {
    FileChooser dirChooser = new FileChooser();
    dirChooser.setTitle(bundle.getString("directory.chooser.title"));
    dirChooser.setInitialDirectory(inputDir);
    return dirChooser.showOpenDialog(container.getScene().getWindow());
  }

  @FXML
  public void saveDefaultDir() {
    LOG.debug("Start saveDefaultDir");
    isValidSelectedDirectory(
        () -> {
          MyProperties.set(Property.DEFAULT_WORKING_DIR.getValue(), selectedDir.getText());
          MyProperties.save();
        });
    LOG.debug("End saveDefaultDir");
  }

  @FXML
  public void initProperties() {
    LOG.debug("Start initProperties");
    messageProperties.setText("");
    textProperties =
        Map.of(
            Property.DATE_FORMAT,
            dateFormat,
            Property.PICTURE_EXTENSION,
            pictureExtension,
            Property.VIDEO_EXTENSION,
            videoExtension,
            Property.FALL_BACK_PATTERN,
            pattern);
    List.of(pictureExtension, videoExtension)
        .forEach(field -> addValidation(field, MiscUtils.isValidExtension, "extension"));
    List.of(dateFormat, pattern)
        .forEach(field -> addValidation(field, MiscUtils.isValidDateFormat, "date.format"));
    textProperties.forEach((prop, text) -> text.setText(MiscUtils.getDefaultValue(prop)));
    boxProperties =
        Map.of(
            Property.ENABLE_FOLDERS_ORGANIZATION,
            enableFoldersOrganization,
            Property.OVERWRITE_IDENTICAL,
            overwriteIdentical,
            Property.SUFFIX_AUTO,
            suffixAuto,
            Property.IGNORE_NO_DATE,
            ignoreNoDate,
            Property.IGNORE_FORMATED,
            ignoreFormated);
    boxProperties.forEach(
        (prop, box) -> box.setSelected(BooleanUtils.toBoolean(MiscUtils.getDefaultValue(prop))));
    initFallbackValue();
    disableRadioButtons();
    LOG.debug("End initProperties");
  }

  private void addValidation(TextField field, Predicate<TextField> validate, String key) {
    field
        .textProperty()
        .addListener(
            event -> {
              boolean isBlank = MiscUtils.isBlank.test(field);
              boolean isinValid = validate.negate().or(MiscUtils.isInvalidCharacters).test(field);
              field.pseudoClassStateChanged(
                  PseudoClass.getPseudoClass(Constant.CSS_CLASS_ERROR), isBlank || isinValid);
              udapteWarningMessage(field.getId(), !isBlank, "blank");
              udapteWarningMessage(field.getId(), !isinValid, key);
              if (!warnings.isEmpty()) {
                processBtn.setDisable(true);
                saveProperties.setDisable(true);
                messageProperties.setText(
                    bundle.getString("warning")
                        + warnings.values().stream()
                            .distinct()
                            .map(k -> bundle.getString("warning." + k))
                            .collect(Collectors.joining(", ")));
              } else {
                processBtn.setDisable(false);
                saveProperties.setDisable(false);
                messageProperties.setText("");
              }
            });
  }

  private void udapteWarningMessage(String key, boolean remove, String msg) {
    if (remove) {
      warnings.remove(key);
    } else {
      warnings.put(key, msg);
    }
  }

  private void initFallbackValue() {
    MyProperties.get(Property.FALL_BACK_CHOICE.getValue())
        .map(StringUtils::upperCase)
        .filter(Fallback::exist)
        .map(Fallback::valueOf)
        .ifPresent(
            choice -> {
              switch (choice) {
                case CREATE:
                  setFallbackValues(true, false, false, true);
                  break;
                case EDIT:
                  setFallbackValues(false, true, false, true);
                  break;
                case PATTERN:
                  setFallbackValues(false, false, true, false);
                  break;
              }
            });
    fallbackPattern.setOnAction(e -> pattern.setDisable(!fallbackPattern.isSelected()));
    fallbackEdit.setOnAction(e -> pattern.setDisable(fallbackEdit.isSelected()));
    fallbackCreate.setOnAction(e -> pattern.setDisable(fallbackCreate.isSelected()));
  }

  private void setFallbackValues(
      boolean fallbackCreateValue,
      boolean fallbackEditValue,
      boolean fallbackPatternValue,
      boolean patternValue) {
    fallbackCreate.setSelected(fallbackCreateValue);
    fallbackEdit.setSelected(fallbackEditValue);
    fallbackPattern.setSelected(fallbackPatternValue);
    pattern.setDisable(patternValue);
  }

  private void disableRadioButtons() {
    List.of(radioYear, radioMonth, radioRoot)
        .forEach(radio -> radio.setDisable(!enableFoldersOrganization.isSelected()));
  }

  @FXML
  public void saveProperties() {
    LOG.debug("Start saveProperties");
    if (warnings.isEmpty()) {
      LOG.debug("Save properties");
      messageProperties.setText(bundle.getString("properties.saved"));
      textProperties.entrySet().stream()
          .forEach(e -> MyProperties.set(e.getKey().getValue(), e.getValue().getText()));
      boxProperties.entrySet().stream()
          .forEach(
              e ->
                  MyProperties.set(
                      e.getKey().getValue(), Boolean.toString(e.getValue().isSelected())));
      saveFallbackValue();
      MyProperties.save();
      detectFolder();
    }
    LOG.debug("End saveProperties");
  }

  private void saveFallbackValue() {
    String fallbackValue;
    if (fallbackCreate.isSelected()) {
      fallbackValue = Fallback.CREATE.toString();
    } else if (fallbackEdit.isSelected()) {
      fallbackValue = Fallback.EDIT.toString();
    } else {
      fallbackValue = Fallback.PATTERN.toString();
    }
    MyProperties.set(Property.FALL_BACK_CHOICE.getValue(), fallbackValue);
  }

  @FXML
  public void process() {
    LOG.debug("Start process");
    messages.setText("");
    setProgressVisibility(true);
    Pair<String, Function<Picture, Date>> fallback = initFallbackDate();
    List<File> files =
        MyFileUtils.listFilesInFolder(
            new File(selectedDir.getText()),
            Arrays.asList(
                ArrayUtils.addAll(
                    StringUtils.split(pictureExtension.getText(), Constant.EXTENSION_SEPARATOR),
                    StringUtils.split(videoExtension.getText(), Constant.EXTENSION_SEPARATOR))),
            false);

    Map<Property, Boolean> checkBoxParams =
        boxProperties.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().isSelected()));
    checkBoxParams.put(Property.RADIO_YEAR, radioYear.isSelected());
    checkBoxParams.put(Property.RADIO_ROOT, radioRoot.isSelected());
    Task<List<Pair<Picture, Picture>>> task =
        new ProcessTask(
            new ProcessParams(
                files,
                bundle,
                fallback.getRight(),
                new SimpleDateFormat(dateFormat.getText()),
                fallback.getLeft(),
                selectedDir.getText(),
                checkBoxParams));

    stopBtn.setOnAction(e -> task.cancel(true));

    task.setOnCancelled(
        wse -> {
          LOG.debug("Task cancelled");
          processBtn.setDisable(false);
          messages.setText(bundle.getString("cancelled"));
          setProgressVisibility(false);
        });

    task.setOnFailed(
        wse -> {
          LOG.error(wse.getSource().getException());
          processBtn.setDisable(false);
          messages.setText(bundle.getString("finished.error"));
          setProgressVisibility(false);
        });

    task.setOnSucceeded(
        wse -> {
          LOG.debug("Task succeeded");
          List<Pair<Picture, Picture>> duplicatePictures = task.getValue();
          List<Exception> exceptions = ((ProcessTask) task).getExceptions();
          int size = duplicatePictures.size();
          for (int i = 0; i < size; i++) {
            if (!suffixAuto.isSelected()) {
              DuplicateDialog dialog =
                  new DuplicateDialog(
                      container,
                      bundle,
                      duplicatePictures.get(i).getLeft(),
                      duplicatePictures.get(i).getRight(),
                      i + 1 + "/" + size,
                      exceptions);
              if (dialog.isStop()) {
                break;
              }
            } else {
              try {
                DuplicateDialog.renameWithSuffix(
                    container,
                    bundle,
                    exceptions,
                    duplicatePictures.get(i).getLeft(),
                    duplicatePictures.get(i).getRight().getPath(),
                    true);
              } catch (MajorException e1) {
                exceptions.add(e1);
              }
            }
          }
          processBtn.setDisable(false);
          setProgressVisibility(false);
          if (exceptions.isEmpty()) {
            messages.setText(bundle.getString("finished"));
          } else {
            LOG.error("Encounter {} exception(s) during process", exceptions.size());
            exceptions.forEach(e -> LOG.error("", e));
            messages.setText(bundle.getString("finished.error"));
          }
        });

    // Binding our UI values to the properties on the task
    progressBar.progressProperty().bind(task.progressProperty());
    progressText.textProperty().bind(task.messageProperty());

    processBtn.setDisable(true);
    new Thread(task).start();
    LOG.debug("End process");
  }

  private Pair<String, Function<Picture, Date>> initFallbackDate() {
    SimpleDateFormat patternSdf = new SimpleDateFormat(pattern.getText());
    String key;
    Function<Picture, Date> getFallbackDate;
    if (fallbackCreate.isSelected()) {
      key = "alert.create";
      getFallbackDate = Picture::getCreation;
    } else if (fallbackEdit.isSelected()) {
      key = "alert.edit";
      getFallbackDate = Picture::getModified;
    } else {
      key = "alert.pattern";
      getFallbackDate =
          picture -> {
            try {
              return patternSdf.parse(picture.getName());
            } catch (ParseException e) {
              LOG.error(
                  "Error when parsing: {} with pattern: {}", picture.getName(), pattern.getText());
              return null;
            }
          };
    }
    return Pair.of(key, getFallbackDate);
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

  private void setProgressVisibility(boolean visible) {
    Consumer<ObservableList<String>> hide = style -> style.add(Constant.CSS_CLASS_HIDDEN);
    Consumer<ObservableList<String>> show = style -> style.removeAll(Constant.CSS_CLASS_HIDDEN);
    List.of(progressBar, progressText).stream()
        .map(Node::getStyleClass)
        .forEach(visible ? show : hide);
    stopBtn.setVisible(visible);
  }
}
