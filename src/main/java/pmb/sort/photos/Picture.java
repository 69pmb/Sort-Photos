package pmb.sort.photos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import pmb.my.starter.exception.MinorException;
import pmb.my.starter.utils.MyConstant;

public class Picture {

    private String path;
    private String name;
    private String extension;
    private String size;
    private String model;
    private Optional<Date> taken;
    private Date creation;
    private Date modified;

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
        model = MiscUtils.getModel(file).orElse("Unknown");
        taken = MiscUtils.getTakenTime(file);
        creation = Date.from(attr.creationTime().toInstant());
        modified = Date.from(attr.lastModifiedTime().toInstant());
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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
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