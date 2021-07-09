package pmb.sort.photos.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyProperties;
import pmb.sort.photos.Controller;
import pmb.sort.photos.model.Fallback;
import pmb.sort.photos.model.Property;

public class PropertiesUtils {

    private static final Logger LOG = LogManager.getLogger(PropertiesUtils.class);

    private Controller controller;
    private Map<Property, TextField> textProperties;
    private Map<Property, CheckBox> boxProperties;

    public PropertiesUtils(Controller controller, Map<Property, TextField> textProperties, Map<Property, CheckBox> boxProperties) {
        this.controller = controller;
        this.textProperties = textProperties;
        this.boxProperties = boxProperties;
        MyProperties.setConfigPath(MyConstant.CONFIGURATION_FILENAME);
        initProperties();
        controller.setDefaultDirectory(
                MyProperties.get(Property.DEFAULT_WORKING_DIR.getValue()).filter(path -> new File(path).exists()).orElse(MyConstant.USER_DIRECTORY));
        controller.setSelectedDir(controller.getDefaultDirectory());
    }

    public void initProperties() {
        LOG.debug("Start initProperties");
        controller.setMessageProperties("");
        textProperties.forEach((prop, text) -> {
            text.setText(MiscUtils.getDefaultValue(prop));
            text.getStyleClass().removeAll(Constant.CSS_CLASS_ERROR);
        });
        boxProperties.forEach((prop, box) -> box.setSelected(BooleanUtils.toBoolean(MiscUtils.getDefaultValue(prop))));
        initFallbackValue();
        controller.disableRadioButtons();
        LOG.debug("End initProperties");
    }

    private void initFallbackValue() {
        MyProperties.get(Property.FALL_BACK_CHOICE.getValue()).map(StringUtils::upperCase).filter(Fallback::exist).map(Fallback::valueOf)
                .ifPresent(choice -> {
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
        controller.getFallbackPattern().setOnAction(e -> controller.getPattern().setDisable(!controller.getFallbackPattern().isSelected()));
        controller.getFallbackEdit().setOnAction(e -> controller.getPattern().setDisable(controller.getFallbackEdit().isSelected()));
        controller.getFallbackCreate().setOnAction(e -> controller.getPattern().setDisable(controller.getFallbackCreate().isSelected()));
    }

    private void setFallbackValues(boolean fallbackCreateValue, boolean fallbackEditValue, boolean fallbackPatternValue, boolean patternValue) {
        controller.getFallbackCreate().setSelected(fallbackCreateValue);
        controller.getFallbackEdit().setSelected(fallbackEditValue);
        controller.getFallbackPattern().setSelected(fallbackPatternValue);
        controller.getPattern().setDisable(patternValue);
    }

    public void saveProperties() {
        LOG.debug("Start saveProperties");
        List<String> warnings = inputsValidation();

        if (!warnings.isEmpty()) {
            LOG.debug("Incorrect inputs");
            controller.disableGoBtn(true);
            controller.setMessageProperties(controller.getLabel("warning") + StringUtils.join(warnings, ","));
        } else {
            LOG.debug("Save properties");
            controller.disableGoBtn(false);
            controller.setMessageProperties(controller.getLabel("properties.saved"));
            textProperties.entrySet().stream().forEach(e -> MyProperties.set(e.getKey().getValue(), e.getValue().getText()));
            boxProperties.entrySet().stream().forEach(e -> MyProperties.set(e.getKey().getValue(), Boolean.toString(e.getValue().isSelected())));
            saveFallbackValue();
            MyProperties.save();
            controller.detectFolder();
        }
        LOG.debug("End saveProperties");
    }

    private List<String> inputsValidation() {
        List<TextField> blanks = textProperties.values().stream().filter(MiscUtils.isBlank).collect(Collectors.toList());
        List<TextField> invalidDate = List.of(controller.getDateFormat(), controller.getPattern()).stream()
                .filter(MiscUtils.isValidDateFormat.negate().or(MiscUtils.isInvalidCharacters)).collect(Collectors.toList());
        List<TextField> invalidExtensions = List.of(controller.getPictureExtension(), controller.getVideoExtension()).stream()
                .filter(MiscUtils.isValidExtension.negate().or(MiscUtils.isInvalidCharacters)).collect(Collectors.toList());
        textProperties.values().stream().forEach(f -> f.getStyleClass().removeAll(Constant.CSS_CLASS_ERROR));
        Stream.of(blanks, invalidDate, invalidExtensions).flatMap(List::stream).collect(Collectors.toSet())
                .forEach(f -> f.getStyleClass().add(Constant.CSS_CLASS_ERROR));

        List<String> warnings = new ArrayList<>();
        if (!blanks.isEmpty()) {
            warnings.add(controller.getLabel("warning.empty"));
        }
        if (!invalidDate.isEmpty()) {
            warnings.add(controller.getLabel("warning.date.format"));
        }
        if (!invalidExtensions.isEmpty()) {
            warnings.add(controller.getLabel("warning.extension"));
        }
        return warnings;
    }

    private void saveFallbackValue() {
        String fallbackValue;
        if (controller.getFallbackCreate().isSelected()) {
            fallbackValue = Fallback.CREATE.toString();
        } else if (controller.getFallbackEdit().isSelected()) {
            fallbackValue = Fallback.EDIT.toString();
        } else {
            fallbackValue = Fallback.PATTERN.toString();
        }
        MyProperties.set(Property.FALL_BACK_CHOICE.getValue(), fallbackValue);
    }

    public void saveDefaultDir() {
        LOG.debug("Start saveDefaultDir");
        controller.isValidSelectedDirectory(() -> {
            MyProperties.set(Property.DEFAULT_WORKING_DIR.getValue(), controller.getSelectedDir());
            MyProperties.save();
        });
        LOG.debug("End saveDefaultDir");
    }

}
