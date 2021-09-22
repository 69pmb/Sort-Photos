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
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import pmb.my.starter.exception.MinorException;
import pmb.my.starter.utils.MyConstant;
import pmb.my.starter.utils.MyFileUtils;
import pmb.sort.photos.model.Picture;
import pmb.sort.photos.model.ProcessParams;
import pmb.sort.photos.utils.Constant;

public class ProcessTask
    extends Task<List<List<Picture>>> {

    private static final Logger LOG = LogManager.getLogger(ProcessTask.class);

    private ProcessParams params;

    public ProcessTask(ProcessParams params) {
        this.params = params;
    }

    @Override
    protected List<List<Picture>> call() throws Exception {
        List<List<Picture>> duplicatePictures = new ArrayList<>();
        AtomicInteger count = new AtomicInteger(1);
        AtomicInteger count2 = new AtomicInteger(1);
        List<File> files = params.getFiles();
        int size = files.size();
        updateProgress(0, size);
        updateMessage("");

        files.stream().map(f -> {
            int i = count.getAndIncrement();
            String percent = NumberFormat.getNumberInstance().format(100 * (double) i / size);
            updateProgress(i, size);
            updateMessage(params.getBundle().getString("analyzing") + percent + "%");
            return new Picture(f);
        }).sorted(Comparator.comparing(p -> p.getTaken().orElse(null), Comparator.nullsLast(Comparator.naturalOrder()))).forEach(picture -> {
            int i = count2.getAndIncrement();
            String percent = NumberFormat.getNumberInstance().format(100 * (double) i / size);
            updateProgress(i, size);
            updateMessage(params.getBundle().getString("processing") + percent + "%");
            picture.getTaken().or(() -> {
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
                        Thread.currentThread().interrupt();
                        return Optional.empty();
                    }
                }
            }).ifPresent(date -> {
                String newName = params.getSdf().format(date);
                try {
                    renameFile(picture, newName, new SimpleDateFormat(Constant.YEAR_FORMAT).format(date),
                        new SimpleDateFormat(Constant.MONTH_FORMAT).format(date), duplicatePictures);
                } catch (IOException e) {
                    throw new MinorException("Error when renaming picture " + picture.getPath() + " to " + newName, e);
                }
            });
        });
        return duplicatePictures;
    }

    private void warningDialogFallBackDate(Picture picture) {
        Alert warning = new Alert(AlertType.WARNING,
            MessageFormat.format(params.getBundle().getString("alert.fail"), StringUtils.abbreviate(picture.getName(), 40)));
        warning.setResizable(true);
        warning.getDialogPane().setMinWidth(700D);
        warning.showAndWait();
    }

    private void renameFile(Picture picture, String newName, String yearFolder, String monthFolder, List<List<Picture>> duplicatePictures)
        throws IOException {
        String newFilename = newName + MyConstant.DOT + picture.getExtension();
        String newPath;

        String selectedDir = params.getSelectedDir();
        if (params.getEnableFoldersOrganization() && params.getRadioRoot()) {
            String yearPath = selectedDir + MyConstant.FS + yearFolder;
            String monthPath = yearPath + MyConstant.FS + monthFolder;
            MyFileUtils.createFolderIfNotExists(yearPath);
            MyFileUtils.createFolderIfNotExists(monthPath);
            newPath = monthPath + MyConstant.FS + newFilename;
        } else if (params.getEnableFoldersOrganization() && params.getRadioYear()) {
            String monthPath = selectedDir + MyConstant.FS + monthFolder;
            MyFileUtils.createFolderIfNotExists(monthPath);
            newPath = monthPath + MyConstant.FS + newFilename;
        } else {
            newPath = selectedDir + MyConstant.FS + newFilename;
        }

        File newFile = new File(newPath);
        if (!StringUtils.equals(newPath, picture.getPath()) && !StringUtils.equals(StringUtils.substringBeforeLast(newPath, MyConstant.DOT),
            StringUtils.substringBeforeLast(picture.getPath(), Constant.SUFFIX_SEPARATOR))) {
            if (!newFile.exists()
                || params.getOverwriteIdentical() && picture.equals(new Picture(newFile)) && !Files.isSameFile(picture.toPath(), newFile.toPath())) {
                LOG.debug("{} renamed to {}", picture.getPath(), newPath);
                Files.move(picture.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else if (!Files.isSameFile(picture.toPath(), newFile.toPath())) {
                LOG.debug("File {} already exist for {}", newPath, picture.getPath());
                duplicatePictures.add(List.of(picture, new Picture(newFile)));
            }
        }
    }

}
