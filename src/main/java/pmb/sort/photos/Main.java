package pmb.sort.photos;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pmb.sort.photos.utils.Constant;

public class Main
        extends Application {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    @Override
    public void start(Stage stage) throws IOException {
        LOG.debug("Start start");
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle("i18n", locale);
        Parent root = FXMLLoader.load(getClass().getResource("Screen.fxml"), bundle);
        Scene scene = new Scene(root, 750, 500);
        scene.getStylesheets().add(Main.class.getResource(Constant.CSS_FILE).toExternalForm());
        stage.setScene(scene);
        stage.show();
        stage.setResizable(false);
        LOG.debug("End start");
    }

    public static void main(String[] args) {
        launch(args);
    }

}
