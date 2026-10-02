package com.example;

import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import kong.unirest.Unirest;

/**
 * ⚙️ Практическая работа №4: Шаблон для студента.
 * Задание: реализовать управление углом сервопривода (0-180°) по МЕТОДИЧКЕ.
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

    @FXML private TextField hostField;
    @FXML private Button connectBtn;
    @FXML private Label statusLabel;

    @FXML private Slider servoSlider;
    @FXML private Label servoAngleLabel;
    @FXML private Button btnServo0;
    @FXML private Button btnServo90;
    @FXML private Button btnServo180;

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
        setControlsDisabled(true);

        // TODO: Задание 1. Добавьте слушатель изменения положения ползунка servoSlider
        // servoSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
        //     int angle = newVal.intValue();
        //     servoAngleLabel.setText(String.format("%d°", angle));
        //     setServoAngle(angle);
        // });
    }

    @FXML
    void handleConnect() {
        if (!isConnected) {
            statusLabel.setText("ПОДКЛЮЧЕНИЕ...");

            String url = makeUrl("/status");
            Unirest.get(url).asStringAsync(response -> {
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        isConnected = true;
                        connectBtn.setText("Отключиться");
                        statusLabel.setText("ПОДКЛЮЧЕНО");
                        setControlsDisabled(false);
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
        setControlsDisabled(true);
    }

    /**
     * TODO: Задание 2. Реализуйте метод отправки угла на ESP32
     * 1. Сформируйте URL: makeUrl(String.format("/servo?angle=%d", angle))
     * 2. Выполните асинхронный GET запрос Unirest.get(url).asStringAsync()
     */
    private void setServoAngle(int angle) {
        // ВАШ КОД ЗДЕСЬ (см. Раздел 5 в МЕТОДИЧКА.md)

    }

    // Обработчики быстрых кнопок
    @FXML void handleServo0()   { servoSlider.setValue(0); }
    @FXML void handleServo90()  { servoSlider.setValue(90); }
    @FXML void handleServo180() { servoSlider.setValue(180); }

    private void setControlsDisabled(boolean disabled) {
        servoSlider.setDisable(disabled);
        btnServo0.setDisable(disabled);
        btnServo90.setDisable(disabled);
        btnServo180.setDisable(disabled);
    }
}
