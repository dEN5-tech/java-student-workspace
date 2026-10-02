package com.example;

import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

/**
 * 💡 Практическая работа №2: Шаблон для студента.
 * Задание: реализовать включение светодиода (GPIO 4) по МЕТОДИЧКЕ.
 */
public class MainController {

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.useSystemProxies", "false");
        System.setProperty("http.nonProxyHosts", "localhost|127.0.0.1|10.*");
    }

    // Элементы подключения
    @FXML private TextField hostField;
    @FXML private Button connectBtn;
    @FXML private Label statusLabel;

    // Элементы светодиода
    @FXML private Circle ledIndicator;
    @FXML private Label ledStatusLabel;
    @FXML private Button btnTurnOnLed;

    private boolean isConnected = false;

    public String makeUrl(String endpoint) {
        String input = hostField.getText().trim();
        if (input.startsWith("http://")) input = input.substring(7);
        if (input.startsWith("https://")) input = input.substring(8);
        while (input.endsWith("/")) input = input.substring(0, input.length() - 1);

        if (input.startsWith("localhost")) {
            input = "127.0.0.1" + input.substring(9);
        }

        if (!input.contains(":")) {
            input = input + ":4000";
        }

        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }

        return "http://" + input + endpoint;
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

            String url = makeUrl("/status");
            Client.get(url, response -> {
                if (response.isSuccess()) {
                    isConnected = true;
                    connectBtn.setText("Отключиться");
                    statusLabel.setText("ПОДКЛЮЧЕНО");
                    btnTurnOnLed.setDisable(false);
                } else {
                    statusLabel.setText("НЕТ СВЯЗИ");
                }
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
     * 1. Сформируйте URL через makeUrl("/led/on")
     * 2. Отправьте асинхронный GET-запрос через:
     *    Client.get(url, response -> {
     *        if (response.isSuccess()) {
     *            ledIndicator.setFill(Color.LIMEGREEN);
     *            ledStatusLabel.setText("СВЕТОДИОД ВКЛЮЧЕН");
     *        }
     *    });
     * (см. подробнее в МЕТОДИЧКА.md)
     */
    @FXML
    void handleTurnOnLed() {
        // ВАШ КОД ЗДЕСЬ (см. Раздел 5 в МЕТОДИЧКА.md)

    }
}
