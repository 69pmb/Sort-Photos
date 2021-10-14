package pmb.sort.photos.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.drew.metadata.Metadata;

import pmb.my.starter.exception.MajorException;
import pmb.my.starter.utils.MyConstant;
import pmb.sort.photos.utils.MiscUtils;

/**
 * Class representing a Picture or Video file.
 */
public class Picture {

    private static final Function<Date, String> FORMAT = date -> new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(date);
    private String path;
    private String name;
    private String extension;
    private String size;
    private Optional<String> model;
    private Optional<Date> taken;
    private Supplier<Optional<String>> dimension;
    private Date creation;
    private Date modified;

    /**
     * Constructor reading file attributes with {@link BasicFileAttributes} and metadata-extractor.
     *
     * @param file to convert
     * @throws MajorException thrown if reading metadata fails
     * @see Files#readAttributes(Path, Class, java.nio.file.LinkOption...)
     */
    public Picture(File file) throws MajorException {
        path = file.getAbsolutePath();
        name = file.getName();
        extension = StringUtils.lowerCase(StringUtils.substringAfterLast(path, MyConstant.DOT));
        if (Files.exists(file.toPath())) {
            BasicFileAttributes attr;
            try {
                attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            } catch (IOException e) {
                throw new MajorException("Error when reading attributes of file: " + path, e);
            }
            size = attr.size() / 1024 + " KB";
            Optional<Metadata> metadata = MiscUtils.getMetadata(file);
            model = metadata.flatMap(MiscUtils::getModel);
            taken = metadata.flatMap(MiscUtils::getTakenTime);
            dimension = () -> metadata.flatMap(m -> MiscUtils.getDimension(m, extension));
            creation = Date.from(attr.creationTime().toInstant());
            modified = Date.from(attr.lastModifiedTime().toInstant());
        } else {
            size = "0 KB";
            model = Optional.empty();
            taken = Optional.empty();
            dimension = Optional::empty;
            creation = Date.from(Instant.now());
            modified = Date.from(Instant.now());
        }
    }

    /**
     * Resumes picture informations.
     *
     * @param bundle translations
     * @return a string with {@link MyConstant#NEW_LINE} separator
     */
    public String prettyPrint(ResourceBundle bundle) {
        List<String> sb = new ArrayList<>();
        BiConsumer<String, String> append = (key, value) -> sb.add(bundle.getString(key) + ": " + value);
        append.accept("duplicate.name", name);
        append.accept("duplicate.creation_date", FORMAT.apply(creation));
        append.accept("duplicate.modification_date", FORMAT.apply(modified));
        append.accept("duplicate.taken_time", taken.map(FORMAT::apply).orElse(bundle.getString("not_found")));
        append.accept("duplicate.model", model.orElse(bundle.getString("duplicate.model.not_found")));
        append.accept("duplicate.size", size);
        append.accept("duplicate.dimension", dimension.get().orElse(bundle.getString("not_found")));
        return sb.stream().collect(Collectors.joining(MyConstant.NEW_LINE));
    }

    /**
     * Converts {@link Picture#path} to {@link Path}.
     *
     * @return a path
     */
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

    public void setTaken(Date taken) {
        this.taken = Optional.ofNullable(taken);
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

    public void setModel(String model) {
        this.model = Optional.ofNullable(model);
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
        return Objects.equals(extension, other.extension) && Objects.equals(model, other.model)
                && Objects.equals(size, other.size) && Objects.equals(taken, other.taken);
    }

}
