package com.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * ============================================================================
 * ШАБЛОН КОНТРОЛЛЕРА ДЛЯ СТУДЕНТА
 * Вариант 2: Ультразвуковой дальномер с сигнализацией
 * Специальность: 09.02.01 «Компьютерные системы и комплексы»
 * ============================================================================
 */
public class MainController {

    // 1. Панель подключения к COM-порту
    @FXML private TextField portField;
    @FXML private Button connectButton;
    @FXML private Label connectionStatusLabel;

    // 2. Виджеты телеметрии (заготовки для студента)
    @FXML private Label distLabel; // Дистанция (см)
    @FXML private Label zoneLabel; // Зона (SAFE/WARNING/DANGER)
    @FXML private Label statusLabel; // Звук (ACTIVE/MUTED)

    // 3. Журнал сырых входящих сообщений от платы
    @FXML private TextArea logArea;

    // Объект обертки для работы с COM-портом
    private SimpleSerial serial = new SimpleSerial();

    @FXML
    public void initialize() {
        // Настройка слушателя входящих строк от платы Arduino / Wokwi
        serial.setOnLineReceived(line -> {
            Platform.runLater(() -> {
                // Выводим полученную строку в текстовый журнал логов
                if (logArea != null) {
                    logArea.appendText("[ВХОД]: " + line + "\n");
                }

                // ==============================================================
                // TODO [Студенту]: Реализуйте разбор строки телеметрии!
                // Пример приходящего пакета: "DIST:45;ZONE:WARNING;STATUS:ACTIVE"
                //
                // Алгоритм разбора:
                // 1. Разбить строку по символу ';' через line.split(";")
                // 2. В цикле разобрать каждый токен по ':' на ключ и значение
                // 3. Обновить текстовые метки (Label), прогресс-бары или график
                // ==============================================================
                parseTelemetry(line);
            });
        });
    }

    // Заготовка разбора телеметрии
    private void parseTelemetry(String line) {
        try {
            // Пример временного обновления:
            // Дистанция (см)
            if (distLabel != null) distLabel.setText(line);
            // Зона (SAFE/WARNING/DANGER)
            if (zoneLabel != null) zoneLabel.setText(line);
            // Звук (ACTIVE/MUTED)
            if (statusLabel != null) statusLabel.setText(line);
        } catch (Exception e) {
            System.err.println("Ошибка разбора: " + e.getMessage());
        }
    }

    // Подключение / Отключение от COM-порта
    @FXML
    private void handleConnectToggle() {
        if (serial.isConnected()) {
            serial.disconnect();
            connectButton.setText("Подключиться");
            connectionStatusLabel.setText("Отключено");
            connectionStatusLabel.setStyle("-fx-text-fill: gray;");
            if (logArea != null) logArea.appendText("[ИНФО]: Отключено от порта.\n");
            return;
        }

        String portName = portField.getText().trim();
        if (portName.isEmpty()) {
            showAlert("Ошибка", "Введите имя COM-порта (например, COM3)!");
            return;
        }

        boolean ok = serial.connect(portName, 9600);
        if (ok) {
            connectionStatusLabel.setText("Связь установлена (" + portName + ")");
            connectionStatusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            connectButton.setText("Отключиться");
            if (logArea != null) logArea.appendText("[ИНФО]: Успешное подключение к " + portName + "\n");
        } else {
            connectionStatusLabel.setText("Ошибка открытия порта!");
            connectionStatusLabel.setStyle("-fx-text-fill: red;");
            showAlert("Ошибка", "Не удалось открыть " + portName + ". Проверьте Wokwi и HW VSP3s!");
        }
    }

    // Отправка команды MUTE
    @FXML
    private void handleCmd1() {
        if (serial.isConnected()) {
            serial.sendLine("MUTE");
            if (logArea != null) logArea.appendText("[ОТПРАВЛЕНО]: MUTE\n");
        } else {
            showAlert("Внимание", "Сначала подключитесь к COM-порту!");
        }
    }
    // Отправка команды UNMUTE
    @FXML
    private void handleCmd2() {
        if (serial.isConnected()) {
            serial.sendLine("UNMUTE");
            if (logArea != null) logArea.appendText("[ОТПРАВЛЕНО]: UNMUTE\n");
        } else {
            showAlert("Внимание", "Сначала подключитесь к COM-порту!");
        }
    }

    // Очистить журнал логов
    @FXML
    private void handleClearLog() {
        if (logArea != null) {
            logArea.clear();
        }
    }

    // Корректное освобождение порта
    public void disconnect() {
        serial.disconnect();
    }

    private void showAlert(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}
