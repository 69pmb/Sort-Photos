package pmb.sort.photos.model;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;

public class ProcessParams {

    private List<File> files;
    private ResourceBundle bundle;
    private Function<Picture, Date> getFallbackDate;
    private SimpleDateFormat sdf;
    private String key;
    private String selectedDir;
    private Map<Property, Boolean> checkBoxParams;

    public ProcessParams(List<File> files, ResourceBundle bundle, Function<Picture, Date> getFallbackDate, SimpleDateFormat sdf, String key,
        String selectedDir, Map<Property, Boolean> checkBoxParams) {
        super();
        this.files = files;
        this.bundle = bundle;
        this.getFallbackDate = getFallbackDate;
        this.sdf = sdf;
        this.key = key;
        this.selectedDir = selectedDir;
        this.checkBoxParams = checkBoxParams;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public Function<Picture, Date> getGetFallbackDate() {
        return getFallbackDate;
    }

    public void setGetFallbackDate(Function<Picture, Date> getFallbackDate) {
        this.getFallbackDate = getFallbackDate;
    }

    public SimpleDateFormat getSdf() {
        return sdf;
    }

    public void setSdf(SimpleDateFormat sdf) {
        this.sdf = sdf;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSelectedDir() {
        return selectedDir;
    }

    public void setSelectedDir(String selectedDir) {
        this.selectedDir = selectedDir;
    }

    public boolean getCheckBoxValue(Property property) {
        return checkBoxParams.get(property);
    }

}
