package com.example;

import com.fazecast.jSerialComm.SerialPort;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Простой студенческий контроллер для управления Arduino Uno через COM-порт.
 */
public class MainController {

    // Элементы формы
    @FXML private TextField portField;
    @FXML private Label statusLabel;
    @FXML private TextArea logArea;

    // Объект последовательного порта
    private SerialPort port;

    // 1. Кнопка "Подключиться"
    @FXML
    void handleConnect() {
        String portName = portField.getText().trim();
        
        // Открываем указанный порт на скорости 9600 бод
        port = SerialPort.getCommPort(portName);
        port.setBaudRate(9600);

        if (port.openPort()) {
            statusLabel.setText("Подключено к " + portName);
            logArea.appendText("Порт " + portName + " открыт успешно!\n");
        } else {
            statusLabel.setText("Ошибка подключения!");
            logArea.appendText("Не удалось открыть " + portName + "\n");
        }
    }

    // 2. Кнопка "ВКЛЮЧИТЬ (1)"
    @FXML
    void handleTurnOn() {
        if (port != null && port.isOpen()) {
            try {
                // Отправляем символ 1 и перевод строки
                port.getOutputStream().write("1\n".getBytes());
                port.getOutputStream().flush();

                statusLabel.setText("Светодиод: ВКЛЮЧЕН");
                logArea.appendText("Отправлена команда: 1 (ВКЛ)\n");
            } catch (Exception e) {
                logArea.appendText("Ошибка отправки: " + e.getMessage() + "\n");
            }
        } else {
            logArea.appendText("Сначала нажмите 'Подключиться'!\n");
        }
    }

    // 3. Кнопка "ВЫКЛЮЧИТЬ (0)"
    @FXML
    void handleTurnOff() {
        if (port != null && port.isOpen()) {
            try {
                // Отправляем символ 0 и перевод строки
                port.getOutputStream().write("0\n".getBytes());
                port.getOutputStream().flush();

                statusLabel.setText("Светодиод: ВЫКЛЮЧЕН");
                logArea.appendText("Отправлена команда: 0 (ВЫКЛ)\n");
            } catch (Exception e) {
                logArea.appendText("Ошибка отправки: " + e.getMessage() + "\n");
            }
        } else {
            logArea.appendText("Сначала нажмите 'Подключиться'!\n");
        }
    }

    // Закрытие порта при закрытии приложения
    public void disconnect() {
        if (port != null && port.isOpen()) {
            port.closePort();
        }
    }
}
