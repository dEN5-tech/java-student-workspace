package com.example;

import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

/**
 * 💡 Практическая работа №3: Шаблон для студента.
 * Задание: реализовать двустороннее переключение светодиода (ВКЛ / ВЫКЛ) по МЕТОДИЧКЕ.
 */
public class MainController {

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.useSystemProxies", "false");
        System.setProperty("http.nonProxyHosts", "localhost|127.0.0.1|10.*");
    }

    @FXML private TextField hostField;
    @FXML private Button connectBtn;
    @FXML private Label statusLabel;

    @FXML private Circle ledIndicator;
    @FXML private Label ledStatusLabel;
    @FXML private Button btnToggleLed;

    private boolean isConnected = false;
    private boolean isLedOn = false;

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
        btnToggleLed.setDisable(true);
        updateLedUI(false);
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
                    btnToggleLed.setDisable(false);
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
        btnToggleLed.setDisable(true);
        updateLedUI(false);
    }

    /**
     * TODO: Задание лабораторной работы №3.
     * 1. Проверьте текущее состояние флага isLedOn.
     * 2. Если isLedOn == true -> сформируйте URL для выключения makeUrl("/led/off"), отправьте Client.get,
     *    и в ответе обновите UI через updateLedUI(false).
     * 3. Если isLedOn == false -> сформируйте URL для включения makeUrl("/led/on"), отправьте Client.get,
     *    и в ответе обновите UI через updateLedUI(true).
     * (см. подробнее в МЕТОДИЧКА.md)
     */
    @FXML
    void handleToggleLed() {
        // ВАШ КОД ЗДЕСЬ (см. Раздел 5 в МЕТОДИЧКА.md)

    }

    private void updateLedUI(boolean on) {
        this.isLedOn = on;
        if (on) {
            ledIndicator.setFill(Color.LIMEGREEN);
            ledStatusLabel.setText("СВЕТОДИОД ВКЛ");
            btnToggleLed.setText("Выключить LED");
        } else {
            ledIndicator.setFill(Color.GRAY);
            ledStatusLabel.setText("СВЕТОДИОД ВЫКЛ");
            btnToggleLed.setText("Включить LED");
        }
    }
}
