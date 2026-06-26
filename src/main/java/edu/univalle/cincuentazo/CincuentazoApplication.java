package edu.univalle.cincuentazo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * JavaFX entry point for the Cincuentazo application.
 */
public class CincuentazoApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(CincuentazoApplication.class.getResource("view/game-view.fxml"));
        Scene scene = new Scene(loader.load(), 1120, 760);
        URL stylesheet = CincuentazoApplication.class.getResource("view/styles.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
        stage.setTitle("Cincuentazo");
        stage.setMinWidth(960);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
