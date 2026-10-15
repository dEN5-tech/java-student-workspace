package com.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * ============================================================================
 * ШАБЛОН КОНТРОЛЛЕРА ДЛЯ СТУДЕНТА
 * Вариант 1: Термостат климат-контроля
 * Специальность: 09.02.01 «Компьютерные системы и комплексы»
 * ============================================================================
 */
public class MainController {

    // 1. Панель подключения к COM-порту
    @FXML private TextField portField;
    @FXML private Button connectButton;
    @FXML private Label connectionStatusLabel;

    // 2. Виджеты телеметрии (заготовки для студента)
    @FXML private Label tempLabel; // Температура (°C)
    @FXML private Label limitLabel; // Порог (°C)
    @FXML private Label modeLabel; // Режим (> или <)
    @FXML private Label statusLabel; // Статус (NORMAL/ALERT)

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
                // Пример приходящего пакета: "TEMP:24.5;LIMIT:25.0;MODE:ABOVE;STATUS:NORMAL"
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
            // Температура (°C)
            if (tempLabel != null) tempLabel.setText(line);
            // Порог (°C)
            if (limitLabel != null) limitLabel.setText(line);
            // Режим (> или <)
            if (modeLabel != null) modeLabel.setText(line);
            // Статус (NORMAL/ALERT)
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

    // Отправка команды SET:28.0
    @FXML
    private void handleCmd1() {
        if (serial.isConnected()) {
            serial.sendLine("SET:28.0");
            if (logArea != null) logArea.appendText("[ОТПРАВЛЕНО]: SET:28.0\n");
        } else {
            showAlert("Внимание", "Сначала подключитесь к COM-порту!");
        }
    }
    // Отправка команды MODE
    @FXML
    private void handleCmd2() {
        if (serial.isConnected()) {
            serial.sendLine("MODE");
            if (logArea != null) logArea.appendText("[ОТПРАВЛЕНО]: MODE\n");
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
