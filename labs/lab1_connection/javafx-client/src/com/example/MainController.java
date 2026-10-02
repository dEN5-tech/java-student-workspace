package com.example;

import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

/**
 * 📘 Практическая работа №1: Шаблон для студента.
 * Задание: реализовать подключение к ESP32 и проверку связи (Ping) по МЕТОДИЧКЕ.
 */
public class MainController {

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.useSystemProxies", "false");
        System.setProperty("http.nonProxyHosts", "localhost|127.0.0.1|10.*");
    }

    // Элементы интерфейса
    @FXML private TextField hostField;
    @FXML private Button connectBtn;
    @FXML private Circle statusIndicator;
    @FXML private Label statusLabel;
    @FXML private Button pingBtn;
    @FXML private Label pingResultLabel;

    private boolean isConnected = false;

    /**
     * Универсальный метод построения URL (с защитой от опечаток и авто-портом 4000)
     */
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
        statusLabel.setText("СТАТУС: НЕ ПОДКЛЮЧЕНО");
        statusIndicator.setFill(Color.GRAY);
        pingBtn.setDisable(true);
    }

    /**
     * TODO: Задание 1. Подключение к плате ESP32
     * 1. Сформируйте URL: String url = makeUrl("/status");
     * 2. Выполните асинхронный GET-запрос:
     *    Client.get(url, response -> {
     *        if (response.isSuccess()) {
     *            isConnected = true;
     *            connectBtn.setText("Отключиться");
     *            statusLabel.setText("ПОДКЛЮЧЕНО (" + response.getBody() + ")");
     *            statusIndicator.setFill(Color.LIMEGREEN);
     *            pingBtn.setDisable(false);
     *        } else {
     *            statusLabel.setText("НЕТ СВЯЗИ");
     *            statusIndicator.setFill(Color.RED);
     *        }
     *    });
     */
    @FXML
    void handleConnect() {
        if (!isConnected) {
            statusLabel.setText("ПОДКЛЮЧЕНИЕ...");

            // ВАШ КОД ЗДЕСЬ (см. Раздел 6 в МЕТОДИЧКА.md)

        } else {
            disconnect();
        }
    }

    public void disconnect() {
        isConnected = false;
        connectBtn.setText("Подключиться");
        statusLabel.setText("СТАТУС: ОТКЛЮЧЕНО");
        statusIndicator.setFill(Color.GRAY);
        pingBtn.setDisable(true);
        pingResultLabel.setText("—");
    }

    /**
     * TODO: Задание 2. Проверка задержки связи (Ping)
     * 1. Засеките время старта: long startTime = System.currentTimeMillis();
     * 2. Сформируйте URL: String url = makeUrl("/ping");
     * 3. Выполните:
     *    Client.get(url, response -> {
     *        long latency = System.currentTimeMillis() - startTime;
     *        if (response.isSuccess()) {
     *            pingResultLabel.setText(String.format("%s (%d ms)", response.getBody(), latency));
     *        } else {
     *            pingResultLabel.setText("Таймаут пинга");
     *        }
     *    });
     */
    @FXML
    void handlePing() {
        pingResultLabel.setText("Измерение...");

        // ВАШ КОД ЗДЕСЬ (см. Раздел 6 в МЕТОДИЧКА.md)

    }
}
