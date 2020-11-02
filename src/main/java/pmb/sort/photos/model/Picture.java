package pmb.sort.photos.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import pmb.my.starter.exception.MinorException;
import pmb.my.starter.utils.MyConstant;
import pmb.sort.photos.utils.MiscUtils;

public class Picture {

    private String path;
    private String name;
    private String extension;
    private String size;
    private Optional<String> model;
    private Optional<Date> taken;
    private Date creation;
    private Date modified;
    private static final Function<Date, String> FORMAT = date -> DateFormat.getInstance().format(date);

    public Picture(File file) {
        path = file.getAbsolutePath();
        name = file.getName();
        extension = StringUtils.lowerCase(StringUtils.substringAfterLast(path, MyConstant.DOT));
        BasicFileAttributes attr;
        try {
            attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        } catch (IOException e) {
            throw new MinorException("Error when reading attributes of file: " + path, e);
        }
        size = (attr.size() / 1024) + " KB";
        model = MiscUtils.getModel(file);
        taken = MiscUtils.getTakenTime(file);
        creation = Date.from(attr.creationTime().toInstant());
        modified = Date.from(attr.lastModifiedTime().toInstant());
    }

    public String prettyPrint(ResourceBundle bundle) {
        List<String> sb = new ArrayList<>();
        BiConsumer<String, String> append = (key, value) -> sb.add(bundle.getString(key) + ": " + value);
        append.accept("duplicate.name", name);
        append.accept("duplicate.creation_date", FORMAT.apply(creation));
        append.accept("duplicate.modification_date", FORMAT.apply(modified));
        append.accept("duplicate.taken_time", taken.map(FORMAT::apply).orElse(bundle.getString("duplicate.taken_time.not_found")));
        append.accept("duplicate.model", model.orElse(bundle.getString("duplicate.model.not_found")));
        append.accept("duplicate.size", size);
        return sb.stream().collect(Collectors.joining(MyConstant.NEW_LINE));
    }

    public Path toPath() {
        return Path.of(path);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Optional<Date> getTaken() {
        return taken;
    }

    public void setTaken(Optional<Date> taken) {
        this.taken = taken;
    }

    public Date getCreation() {
        return creation;
    }

    public void setCreation(Date creation) {
        this.creation = creation;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Optional<String> getModel() {
        return model;
    }

    public void setModel(Optional<String> model) {
        this.model = model;
    }

    @Override
    public int hashCode() {
        return Objects.hash(extension, model, size, taken);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Picture)) {
            return false;
        }
        Picture other = (Picture) obj;
        return Objects.equals(extension, other.extension) && Objects.equals(model, other.model) && Objects.equals(size, other.size)
                && Objects.equals(taken, other.taken);
    }

}