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
 * 📘 Практическая работа №1: Шаблон для студента.
 * Задание: реализовать подключение к ESP32 и проверку связи (Ping) по МЕТОДИЧКЕ.
 */
public class MainController {

    // Элементы интерфейса
    @FXML private TextField hostField;
    @FXML private Button connectBtn;
    @FXML private Circle statusIndicator;
    @FXML private Label statusLabel;
    @FXML private Button pingBtn;
    @FXML private Label pingResultLabel;

    private boolean isConnected = false;

    // Вспомогательный метод получения хоста (порт 4000 для Wokwi)
    private String getHost() {
        String host = hostField.getText().trim();
        if (!host.contains(":")) {
            return String.format("%s:4000", host);
        }
        return host;
    }

    @FXML
    public void initialize() {
        // Начальное состояние при запуске
        statusLabel.setText("СТАТУС: НЕ ПОДКЛЮЧЕНО");
        statusIndicator.setFill(Color.GRAY);
        pingBtn.setDisable(true);
    }

    /**
     * TODO: Задание 1. Подключение к плате ESP32
     * 1. Сформируйте URL: "http://" + getHost() + "/status"
     * 2. Выполните асинхронный GET-запрос через Unirest.get(url).asStringAsync(...)
     * 3. Внутри Platform.runLater() проверьте response.isSuccess():
     *    - Установите isConnected = true, текст кнопки "Отключиться"
     *    - Покрасьте statusIndicator в Color.LIMEGREEN, статус "ПОДКЛЮЧЕНО"
     *    - Разблокируйте pingBtn.setDisable(false)
     */
    @FXML
    void handleConnect() {
        if (!isConnected) {
            statusLabel.setText("ПОДКЛЮЧЕНИЕ...");

            // ВАШ КОД ЗДЕСЬ (см. Раздел 5.1 в МЕТОДИЧКА.md)
            
        } else {
            disconnect();
        }
    }

    /**
     * Отключение от платы
     */
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
     * 2. Сформируйте URL: "http://" + getHost() + "/ping"
     * 3. Выполните Unirest.get(url).asStringAsync(...)
     * 4. В Platform.runLater() посчитайте задержку: long rtt = System.currentTimeMillis() - startTime;
     * 5. Выведите результат в pingResultLabel
     */
    @FXML
    void handlePing() {
        pingResultLabel.setText("Измерение...");

        // ВАШ КОД ЗДЕСЬ (см. Раздел 5.2 в МЕТОДИЧКА.md)

    }
}
