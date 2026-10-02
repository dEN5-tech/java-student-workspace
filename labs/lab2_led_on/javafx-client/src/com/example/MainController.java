package com.example;

import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import kong.unirest.Unirest;

/**
 * 💡 Практическая работа №2: Шаблон для студента.
 * Задание: реализовать включение светодиода (GPIO 4) по МЕТОДИЧКЕ.
 */
public class MainController {

    // Элементы подключения
    @FXML private TextField hostField;
    @FXML private Button connectBtn;
    @FXML private Label statusLabel;

    // Элементы светодиода
    @FXML private Circle ledIndicator;
    @FXML private Label ledStatusLabel;
    @FXML private Button btnTurnOnLed;

    private boolean isConnected = false;

    private String getHost() {
        String host = hostField.getText().trim();
        if (!host.contains(":")) {
            return String.format("%s:4000", host);
        }
        return host;
    }

    @FXML
    public void initialize() {
        statusLabel.setText("ОТКЛЮЧЕНО");
        ledIndicator.setFill(Color.GRAY);
        ledStatusLabel.setText("ВЫКЛ");
        btnTurnOnLed.setDisable(true);
    }

    @FXML
    void handleConnect() {
        if (!isConnected) {
            statusLabel.setText("ПОДКЛЮЧЕНИЕ...");

            String url = String.format("http://%s/status", getHost());
            Unirest.get(url).connectTimeout(2000).asStringAsync(response -> {
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        isConnected = true;
                        connectBtn.setText("Отключиться");
                        statusLabel.setText("ПОДКЛЮЧЕНО");
                        btnTurnOnLed.setDisable(false);
                    } else {
                        statusLabel.setText("НЕТ СВЯЗИ");
                    }
                });
            });
        } else {
            disconnect();
        }
    }

    public void disconnect() {
        isConnected = false;
        connectBtn.setText("Подключиться");
        statusLabel.setText("ОТКЛЮЧЕНО");
        btnTurnOnLed.setDisable(true);
        ledIndicator.setFill(Color.GRAY);
        ledStatusLabel.setText("ВЫКЛ");
    }

    /**
     * TODO: Задание лабораторной работы №2.
     * 1. Сформируйте URL: String.format("http://%s/led/on", getHost())
     * 2. Отправьте асинхронный GET-запрос через Unirest.get(url).asStringAsync(...)
     * 3. В Platform.runLater() обновите интерфейс:
     *    - ledIndicator.setFill(Color.LIMEGREEN);
     *    - ledStatusLabel.setText("СВЕТОДИОД ВКЛЮЧЕН");
     *    (см. подробнее в МЕТОДИЧКА.md)
     */
    @FXML
    void handleTurnOnLed() {
        // ВАШ КОД ЗДЕСЬ (см. Раздел 5 в МЕТОДИЧКА.md)

    }
}
