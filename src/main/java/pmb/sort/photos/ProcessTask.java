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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import pmb.my.starter.exception.MajorException;
import pmb.my.starter.exception.MinorException;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyFileUtils;
import pmb.sort.photos.model.Picture;
import pmb.sort.photos.model.ProcessParams;
import pmb.sort.photos.model.Property;
import pmb.sort.photos.utils.Constant;

public class ProcessTask
    extends Task<List<Pair<Picture, Picture>>> {

    private static final Logger LOG = LogManager.getLogger(ProcessTask.class);

    private ProcessParams params;
    private List<Exception> exceptions;

    public ProcessTask(ProcessParams params) {
        this.params = params;
        exceptions = new ArrayList<>();
    }

    @Override
    protected List<Pair<Picture, Picture>> call() throws Exception {
        List<Pair<Picture, Picture>> duplicatePictures = new ArrayList<>();
        AtomicInteger count = new AtomicInteger(1);
        AtomicInteger count2 = new AtomicInteger(1);
        List<File> files = params.getFiles();
        int size = files.size();
        updateProgress(0, size);
        updateMessage("");

        files.stream().map(f -> {
            updateProgress(count, size, "analyzing");
            try {
                return new Picture(f);
            } catch (MajorException e) {
                exceptions.add(e);
                return null;
            }
        }).filter(Objects::nonNull).sorted(Comparator.comparing(p -> p.getTaken().orElse(null), Comparator.nullsLast(Comparator.naturalOrder())))
            .forEach(picture -> {
                updateProgress(count2, size, "processing");
                picture.getTaken().or(() -> processNoTakenDate(picture)).ifPresent(date -> {
                    String newName = params.getSdf().format(date);
                    try {
                        renameFile(picture, newName, new SimpleDateFormat(Constant.YEAR_FORMAT).format(date),
                            new SimpleDateFormat(Constant.MONTH_FORMAT).format(date), duplicatePictures);
                    } catch (MajorException | IOException e) {
                        exceptions.add(new MinorException("Error when renaming picture " + picture.getPath() + " to " + newName, e));
                    }
                });
            });
        return duplicatePictures;
    }

    private Optional<Date> processNoTakenDate(Picture picture) {
        if (params.getCheckBoxValue(Property.IGNORE_NO_DATE)) {
            return Optional.empty();
        }
        Date fallbackDate = params.getGetFallbackDate().apply(picture);
        if (fallbackDate == null) {
            warningDialogFallBackDate(picture);
            return Optional.empty();
        } else {
            FutureTask<Optional<Date>> noTakenDateTask = new FutureTask<>(new NoTakenDateTask(picture, fallbackDate,
                MessageFormat.format(params.getBundle().getString(params.getKey()), params.getSdf().format(fallbackDate)),
                params.getBundle().getString("alert.message")));
            Platform.runLater(noTakenDateTask);
            try {
                return noTakenDateTask.get();
            } catch (InterruptedException | ExecutionException e) {
                exceptions.add(e);
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
        }
    }

    private void updateProgress(AtomicInteger count, int size, String message) {
        int i = count.getAndIncrement();
        String percent = NumberFormat.getNumberInstance().format(100 * (double) i / size);
        updateProgress(i, size);
        updateMessage(params.getBundle().getString(message) + percent + "%");
    }

    private void warningDialogFallBackDate(Picture picture) {
        FutureTask<Void> warningTask = new FutureTask<>(() -> {
            Alert warning = new Alert(AlertType.WARNING,
                MessageFormat.format(params.getBundle().getString("alert.fail"), StringUtils.abbreviate(picture.getName(), 40)));
            warning.setResizable(true);
            warning.getDialogPane().setMinWidth(700D);
            warning.showAndWait();
            return null;
        });
        Platform.runLater(warningTask);
        try {
            warningTask.get();
        } catch (InterruptedException | ExecutionException e) {
            exceptions.add(e);
            Thread.currentThread().interrupt();
        }
    }

    private void renameFile(Picture picture, String newName, String yearFolder, String monthFolder, List<Pair<Picture, Picture>> duplicatePictures)
        throws IOException, MajorException {
        String newFilename = newName + MyConstant.DOT + picture.getExtension();
        String newPath;

        String selectedDir = params.getSelectedDir();
        if (params.getCheckBoxValue(Property.ENABLE_FOLDERS_ORGANIZATION) && params.getCheckBoxValue(Property.RADIO_ROOT)) {
            String yearPath = selectedDir + MyConstant.FS + yearFolder;
            String monthPath = yearPath + MyConstant.FS + monthFolder;
            MyFileUtils.createFolderIfNotExists(yearPath);
            MyFileUtils.createFolderIfNotExists(monthPath);
            newPath = monthPath + MyConstant.FS + newFilename;
        } else if (params.getCheckBoxValue(Property.ENABLE_FOLDERS_ORGANIZATION) && params.getCheckBoxValue(Property.RADIO_YEAR)) {
            String monthPath = selectedDir + MyConstant.FS + monthFolder;
            MyFileUtils.createFolderIfNotExists(monthPath);
            newPath = monthPath + MyConstant.FS + newFilename;
        } else {
            newPath = selectedDir + MyConstant.FS + newFilename;
        }

        File newFile = new File(newPath);
        if (!StringUtils.equals(newPath, picture.getPath()) && !StringUtils.equals(StringUtils.substringBeforeLast(newPath, MyConstant.DOT),
            StringUtils.substringBeforeLast(picture.getPath(), Constant.SUFFIX_SEPARATOR))) {
            Picture newPicture = new Picture(newFile);
            if (!newFile.exists() || params.getCheckBoxValue(Property.OVERWRITE_IDENTICAL) && picture.equals(newPicture)
                && !Files.isSameFile(picture.toPath(), newFile.toPath())) {
                LOG.debug("{} renamed to {}", picture.getPath(), newPath);
                Files.move(picture.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else if (!Files.isSameFile(picture.toPath(), newFile.toPath())) {
                LOG.debug("File {} already exist for {}", newPath, picture.getPath());
                duplicatePictures.add(Pair.of(picture, newPicture));
            }
        }
    }

    public List<Exception> getExceptions() {
        return exceptions;
    }

}
