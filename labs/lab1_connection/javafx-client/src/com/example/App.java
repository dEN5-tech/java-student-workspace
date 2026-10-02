package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 460, 420);

        stage.setTitle("Лабораторная №1: Подключение и Ping (ESP32)");
        stage.setScene(scene);

        try (InputStream iconStream = getClass().getResourceAsStream("/assets/icon.png")) {
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception ignored) {}

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
