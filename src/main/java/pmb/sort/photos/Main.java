package pmb.sort.photos;

import java.io.IOException;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import pmb.sort.photos.utils.Constant;
import pmb.sort.photos.utils.JavaFxUtils;

public class Main
        extends Application {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    @Override
    public void start(Stage stage) throws IOException {
        LOG.debug("Start start");
        Locale locale = Locale.getDefault();
        GridPane root = (GridPane) JavaFxUtils.load("Screen.fxml", locale);
        Parent langBox = JavaFxUtils.load("I18n.fxml", locale);
        root.getChildren().add(0, langBox);
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
