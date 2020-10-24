package pmb.sort.photos;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle("i18n", locale);
        Parent root = FXMLLoader.load(getClass().getResource("Screen.fxml"), bundle);
        Scene scene = new Scene(root, 700, 600);
        scene.getStylesheets().add(Main.class.getResource("application.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
