package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private MainController controller;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        stage.setTitle("Arduino LED Control");
        stage.setScene(new Scene(root, 380, 420));
        stage.setResizable(false);
        stage.setOnCloseRequest(e -> controller.disconnect());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
