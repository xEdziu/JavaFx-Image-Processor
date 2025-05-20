package dev.goral.javafximageprocessor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        LoggerService.log("Aplikacja uruchomiona", "INFO", 0);
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Edytor obrazów - JavaFX");
        stage.setWidth(750);
        stage.setHeight(500);
        stage.setScene(scene);
        stage.setOnCloseRequest(_ -> LoggerService.log("Aplikacja zamknięta", "INFO", 0));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}