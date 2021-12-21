package pmb.sort.photos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pmb.my.starter.exception.MajorException;
import pmb.my.starter.exception.MinorException;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyFileUtils;
import pmb.sort.photos.model.Picture;
import pmb.sort.photos.model.ProcessParams;
import pmb.sort.photos.model.Property;
import pmb.sort.photos.utils.Constant;
import pmb.sort.photos.utils.MiscUtils;

public class ProcessTask extends Task<List<Pair<Picture, Picture>>> {

  private static final Logger LOG = LogManager.getLogger(ProcessTask.class);

  private ProcessParams params;
  private Boolean noTakenDateShowAgain;
  private List<Exception> exceptions;

  public ProcessTask(ProcessParams params) {
    this.params = params;
    exceptions = new ArrayList<>();
  }

  @Override
  protected List<Pair<Picture, Picture>> call() throws Exception {
    LOG.debug("Start ProcessTask");
    List<Pair<Picture, Picture>> duplicatePictures = new ArrayList<>();
    AtomicInteger count = new AtomicInteger(1);
    AtomicInteger count2 = new AtomicInteger(1);
    List<File> files = params.getFiles();
    int size = files.size();
    updateProgress(0, size);
    updateMessage("");

    files.stream()
        .filter(
            file ->
                !params.getCheckBoxValue(Property.IGNORE_FORMATED)
                    || !MiscUtils.isStringMatchDateFormat(file.getName(), params.getSdf()))
        .map(
            f -> {
              updateProgress(count, size, "analyzing");
              try {
                return new Picture(f);
              } catch (MajorException e) {
                exceptions.add(e);
                return null;
              }
            })
        .filter(Objects::nonNull)
        .sorted(
            Comparator.comparing(
                p -> p.getTaken().orElse(null), Comparator.nullsLast(Comparator.naturalOrder())))
        .forEach(
            picture -> {
              updateProgress(count2, size, "processing");
              picture
                  .getTaken()
                  .or(() -> processNoTakenDate(picture))
                  .ifPresent(
                      date -> {
                        String newName = params.getSdf().format(date);
                        try {
                          renameFile(
                              picture,
                              newName,
                              new SimpleDateFormat(Constant.YEAR_FORMAT).format(date),
                              new SimpleDateFormat(Constant.MONTH_FORMAT).format(date),
                              duplicatePictures);
                        } catch (MajorException | IOException e) {
                          exceptions.add(
                              new MinorException(
                                  "Error when renaming picture "
                                      + picture.getPath()
                                      + " to "
                                      + newName,
                                  e));
                        }
                      });
            });
    LOG.debug("End ProcessTask");
    return duplicatePictures;
  }

  private Optional<Date> processNoTakenDate(Picture picture) {
    if (params.getCheckBoxValue(Property.IGNORE_NO_DATE) || isCancelled()) {
      return Optional.empty();
    }
    Date fallbackDate = params.getGetFallbackDate().apply(picture);
    if (fallbackDate == null) {
      if (BooleanUtils.isTrue(warningDialogFallBackDate(picture))) {
        LOG.debug("Stop from warning Dialog FallBack Date");
        cancel(true);
      }
      return Optional.empty();
    } else {
      if (noTakenDateShowAgain == null) {
        FutureTask<Triple<Optional<Date>, Boolean, Boolean>> noTakenDateTask =
            new FutureTask<>(
                new NoTakenDateTask(
                    picture,
                    fallbackDate,
                    MessageFormat.format(
                        params.getBundle().getString(params.getKey()),
                        params.getSdf().format(fallbackDate)),
                    params.getBundle().getString("alert.message"),
                    params.getBundle().getString("alert.checkbox"),
                    params.getBundle().getString("stop")));
        Platform.runLater(noTakenDateTask);
        try {
          Triple<Optional<Date>, Boolean, Boolean> triple = noTakenDateTask.get();
          if (BooleanUtils.isTrue(triple.getRight())) {
            LOG.debug("Stop from warning no Taken Date Task");
            cancel(true);
          }
          noTakenDateShowAgain =
              BooleanUtils.isTrue(triple.getRight()) ? triple.getLeft().isPresent() : null;
          return triple.getLeft();
        } catch (InterruptedException | ExecutionException e) {
          exceptions.add(e);
          Thread.currentThread().interrupt();
          return Optional.empty();
        }
      } else {
        LOG.debug("Not show again enable");
        return noTakenDateShowAgain ? Optional.ofNullable(fallbackDate) : Optional.empty();
      }
    }
  }

  private void updateProgress(AtomicInteger count, int size, String message) {
    int i = count.getAndIncrement();
    String percent = NumberFormat.getNumberInstance().format(100 * (double) i / size);
    updateProgress(i, size);
    updateMessage(params.getBundle().getString(message) + percent + "%");
  }

  private Boolean warningDialogFallBackDate(Picture picture) {
    FutureTask<Boolean> warningTask =
        new FutureTask<>(
            () -> {
              Alert warning =
                  new Alert(
                      AlertType.WARNING,
                      MessageFormat.format(
                          params.getBundle().getString("alert.fail"),
                          StringUtils.abbreviate(picture.getName(), 40)),
                      new ButtonType(params.getBundle().getString("stop")),
                      ButtonType.OK);
              warning.setResizable(true);
              warning.getDialogPane().setMinWidth(700D);
              return warning
                  .showAndWait()
                  .map(btn -> btn.getButtonData() == ButtonData.OTHER)
                  .orElse(false);
            });
    Platform.runLater(warningTask);
    try {
      return warningTask.get();
    } catch (InterruptedException | ExecutionException e) {
      exceptions.add(e);
      Thread.currentThread().interrupt();
    }
    return false;
  }

  private void renameFile(
      Picture picture,
      String newName,
      String yearFolder,
      String monthFolder,
      List<Pair<Picture, Picture>> duplicatePictures)
      throws IOException, MajorException {
    String newFilename = newName + MyConstant.DOT + picture.getExtension();
    String newPath;

    String selectedDir = params.getSelectedDir();
    if (params.getCheckBoxValue(Property.ENABLE_FOLDERS_ORGANIZATION)
        && params.getCheckBoxValue(Property.RADIO_ROOT)) {
      String yearPath = selectedDir + MyConstant.FS + yearFolder;
      String monthPath = yearPath + MyConstant.FS + monthFolder;
      MyFileUtils.createFolderIfNotExists(yearPath);
      MyFileUtils.createFolderIfNotExists(monthPath);
      newPath = monthPath + MyConstant.FS + newFilename;
    } else if (params.getCheckBoxValue(Property.ENABLE_FOLDERS_ORGANIZATION)
        && params.getCheckBoxValue(Property.RADIO_YEAR)) {
      String monthPath = selectedDir + MyConstant.FS + monthFolder;
      MyFileUtils.createFolderIfNotExists(monthPath);
      newPath = monthPath + MyConstant.FS + newFilename;
    } else {
      newPath = selectedDir + MyConstant.FS + newFilename;
    }

    File newFile = new File(newPath);
    if (!StringUtils.equals(newPath, picture.getPath())
        && !StringUtils.equals(
            StringUtils.substringBeforeLast(newPath, MyConstant.DOT),
            StringUtils.substringBeforeLast(picture.getPath(), Constant.SUFFIX_SEPARATOR))) {
      Picture newPicture = new Picture(newFile);
      if (!newFile.exists()
          || params.getCheckBoxValue(Property.OVERWRITE_IDENTICAL)
              && picture.equals(newPicture)
              && !Files.isSameFile(picture.toPath(), newFile.toPath())) {
        LOG.debug("{} renamed to {}", picture.getPath(), newPath);
        Files.move(picture.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } else if (!Files.isSameFile(picture.toPath(), newFile.toPath())) {
        LOG.debug("Can't rename {} to {}, file already exist", picture.getPath(), newPath);
        duplicatePictures.add(Pair.of(picture, newPicture));
      }
    }
  }

  public List<Exception> getExceptions() {
    return exceptions;
  }
}
