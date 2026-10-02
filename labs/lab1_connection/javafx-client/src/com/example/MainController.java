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

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.useSystemProxies", "false");
        System.setProperty("http.nonProxyHosts", "localhost|127.0.0.1|10.*");

        Unirest.config()
               .reset()
               .connectTimeout(3000)
               .socketTimeout(3000)
               .setDefaultHeader("Connection", "close")
               .proxy((kong.unirest.Proxy) null);
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
     * Универсальный метод построения URL (с защитой от опечаток и IPv6)
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
     * 1. Сформируйте URL через makeUrl("/status")
     * 2. Выполните асинхронный GET-запрос: Unirest.get(url).asStringAsync(...)
     * 3. Внутри Platform.runLater() проверьте response.isSuccess():
     *    - Установите isConnected = true, текст кнопки "Отключиться"
     *    - Покрасьте statusIndicator в Color.LIMEGREEN, статус "ПОДКЛЮЧЕНО"
     *    - Разблокируйте pingBtn.setDisable(false)
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
     * 2. Сформируйте URL: makeUrl("/ping")
     * 3. Выполните Unirest.get(url).asStringAsync(...)
     * 4. В Platform.runLater() посчитайте задержку: long rtt = System.currentTimeMillis() - startTime;
     * 5. Выведите результат в pingResultLabel
     */
    @FXML
    void handlePing() {
        pingResultLabel.setText("Измерение...");

        // ВАШ КОД ЗДЕСЬ (см. Раздел 6 в МЕТОДИЧКА.md)

    }
}
