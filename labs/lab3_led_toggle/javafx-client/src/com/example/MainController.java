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
 * 💡 Практическая работа №3: Шаблон для студента.
 * Задание: реализовать двустороннее переключение светодиода (ВКЛ / ВЫКЛ) по МЕТОДИЧКЕ.
 */
public class MainController {

    @FXML private TextField hostField;
    @FXML private Button connectBtn;
    @FXML private Label statusLabel;

    @FXML private Circle ledIndicator;
    @FXML private Label ledStatusLabel;
    @FXML private Button btnToggleLed;

    private boolean isConnected = false;
    private boolean isLedOn = false;

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
        btnToggleLed.setDisable(true);
        updateLedUI(false);
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
                        btnToggleLed.setDisable(false);
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
        btnToggleLed.setDisable(true);
        updateLedUI(false);
    }

    /**
     * TODO: Задание лабораторной работы №3.
     * 1. Проверьте текущее состояние флага isLedOn.
     * 2. Если isLedOn == true -> сформируйте URL для выключения "/led/off", отправьте Unirest GET,
     *    и в callback обновите UI через updateLedUI(false).
     * 3. Если isLedOn == false -> сформируйте URL для включения "/led/on", отправьте Unirest GET,
     *    и в callback обновите UI через updateLedUI(true).
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
