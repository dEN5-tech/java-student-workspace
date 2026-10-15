package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ThermostatView.fxml"));
        Parent root = loader.load();

        ThermostatController controller = loader.getController();

        primaryStage.setTitle("АРМ Оператора: Термостат климат-контроля (COM-порт)");
        primaryStage.setScene(new Scene(root, 950, 720));

        // При закрытии крестиком корректно освобождаем COM-порт и глушим процесс JVM
        primaryStage.setOnCloseRequest(event -> {
            controller.disconnect();
            javafx.application.Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
