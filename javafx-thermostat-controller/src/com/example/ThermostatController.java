package com.example;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ============================================================================
 * Контроллер АРМ Оператора: Термостат климат-контроля
 * Специальность: 09.02.01 «Компьютерные системы и комплексы»
 * 
 * Использует класс-обертку SimpleSerial для надежного чтения и отправки команд.
 * ============================================================================
 */
public class ThermostatController {

    // 1. Панель подключения к порту
    @FXML private TextField portField;
    @FXML private Button connectButton;
    @FXML private Label connectionStatusLabel;

    // 2. Блок текущей телеметрии (Dashboard)
    @FXML private Label tempDisplayLabel;
    @FXML private Label limitDisplayLabel;
    @FXML private Label modeDisplayLabel;
    @FXML private Label statusBadgeLabel;

    // 3. Блок удаленного управления
    @FXML private TextField newLimitField;
    @FXML private Button setLimitButton;
    @FXML private Button toggleModeButton;

    // 4. График в реальном времени
    @FXML private LineChart<String, Number> tempChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    private XYChart.Series<String, Number> tempSeries = new XYChart.Series<>();
    private XYChart.Series<String, Number> limitSeries = new XYChart.Series<>();

    // 5. Таблица журнала
    @FXML private TableView<ThermostatRecord> logTable;
    @FXML private TableColumn<ThermostatRecord, String> colTime;
    @FXML private TableColumn<ThermostatRecord, Double> colTemp;
    @FXML private TableColumn<ThermostatRecord, Double> colLimit;
    @FXML private TableColumn<ThermostatRecord, String> colMode;
    @FXML private TableColumn<ThermostatRecord, String> colStatus;
    private ObservableList<ThermostatRecord> recordsList = FXCollections.observableArrayList();

    // Экземпляр простой обертки COM-порта
    private SimpleSerial serial = new SimpleSerial();

    @FXML
    public void initialize() {
        // Настройка серий графиков
        tempSeries.setName("Температура (°C)");
        limitSeries.setName("Порог аварии (°C)");
        tempChart.getData().addAll(tempSeries, limitSeries);
        tempChart.setAnimated(false); // Отключаем анимацию для максимальной плавности

        // Настройка столбцов таблицы
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colTemp.setCellValueFactory(new PropertyValueFactory<>("temperature"));
        colLimit.setCellValueFactory(new PropertyValueFactory<>("threshold"));
        colMode.setCellValueFactory(new PropertyValueFactory<>("mode"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        logTable.setItems(recordsList);

        // Настраиваем слушатель входящих строк от COM-порта (Wokwi/Arduino)
        serial.setOnLineReceived(line -> {
            parseIncomingLine(line);
        });

        // Начальные демонстрационные данные, чтобы интерфейс не был пустым при запуске
        addTelemetryToUI(24.0, 25.0, "ABOVE", "NORMAL");
    }

    // --- 1. Подключение / Отключение от COM-порта ---
    @FXML
    private void handleConnectToggle() {
        // Если уже подключены - отключаемся
        if (serial.isConnected()) {
            serial.disconnect();
            connectButton.setText("Подключиться");
            connectionStatusLabel.setText("Отключено");
            connectionStatusLabel.setStyle("-fx-text-fill: gray;");
            return;
        }

        // Проверяем ввод имени порта
        String portName = portField.getText().trim();
        if (portName.isEmpty()) {
            showAlert("Ошибка", "Введите имя COM-порта (например, COM3)!");
            return;
        }

        // Подключаемся через SimpleSerial
        boolean ok = serial.connect(portName, 9600);
        if (ok) {
            connectionStatusLabel.setText("Связь установлена (" + portName + ")");
            connectionStatusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            connectButton.setText("Отключиться");
        } else {
            connectionStatusLabel.setText("Ошибка открытия порта!");
            connectionStatusLabel.setStyle("-fx-text-fill: red;");
            showAlert("Ошибка подключения", "Не удалось открыть " + portName + "!\n\n"
                    + "Проверьте:\n"
                    + "1. Не открыт ли порт в другой программе (закройте мониторы портов).\n"
                    + "2. Запущен ли Wokwi и настроен ли HW VSP3s на 127.0.0.1:4000.");
        }
    }

    // --- Разбор строки протокола (TEMP:24.0;LIMIT:25.0;MODE:ABOVE;STATUS:NORMAL) ---
    private void parseIncomingLine(String line) {
        try {
            // Игнорируем квитанции ACK от платы
            if (line.startsWith("ACK:")) {
                System.out.println("[ARDUINO КВИТАНЦИЯ]: " + line);
                return;
            }

            double temp = 0.0;
            double limit = 25.0;
            String mode = "ABOVE";
            String status = "NORMAL";

            String[] tokens = line.split(";");
            for (String token : tokens) {
                String[] kv = token.split(":");
                if (kv.length >= 2) {
                    String key = kv[0].trim().toUpperCase();
                    String val = kv[1].trim();

                    if (key.equals("TEMP")) {
                        temp = Double.parseDouble(val.replace(',', '.'));
                    } else if (key.equals("LIMIT")) {
                        limit = Double.parseDouble(val.replace(',', '.'));
                    } else if (key.equals("MODE")) {
                        mode = val;
                    } else if (key.equals("STATUS")) {
                        status = val;
                    }
                }
            }

            final double fTemp = temp;
            final double fLimit = limit;
            final String fMode = mode;
            final String fStatus = status;

            // Обновление элементов интерфейса в главном UI-потоке JavaFX
            Platform.runLater(() -> addTelemetryToUI(fTemp, fLimit, fMode, fStatus));

        } catch (Exception e) {
            System.err.println("Ошибка разбора пакета: " + line + " (" + e.getMessage() + ")");
        }
    }

    // --- Обновление графических элементов АРМ ---
    private void addTelemetryToUI(double temp, double limit, String mode, String status) {
        String timeStr = new SimpleDateFormat("HH:mm:ss").format(new Date());

        // 1. Обновляем текстовые индикаторы
        tempDisplayLabel.setText(String.format(Locale.US, "%.1f °C", temp));
        limitDisplayLabel.setText(String.format(Locale.US, "Порог: %.1f °C", limit));
        modeDisplayLabel.setText("Режим: " + (mode.equalsIgnoreCase("ABOVE") ? "> ПОРОГА" : "< ПОРОГА"));

        // 2. Бейдж статуса («НОРМА» зеленый / «ТРЕВОГА» красный)
        if (status.equalsIgnoreCase("ALERT") || status.contains("ALARM")) {
            statusBadgeLabel.setText("ТРЕВОГА!");
            statusBadgeLabel.setStyle("-fx-background-color: #ff3333; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 8; -fx-font-weight: bold;");
        } else {
            statusBadgeLabel.setText("НОРМА");
            statusBadgeLabel.setStyle("-fx-background-color: #33cc33; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 8; -fx-font-weight: bold;");
        }

        // 3. Добавляем точки на график
        tempSeries.getData().add(new XYChart.Data<>(timeStr, temp));
        limitSeries.getData().add(new XYChart.Data<>(timeStr, limit));

        // Ограничиваем историю графика 25 точками
        if (tempSeries.getData().size() > 25) {
            tempSeries.getData().remove(0);
            limitSeries.getData().remove(0);
        }

        // 4. Добавляем строку в таблицу журнала
        ThermostatRecord record = new ThermostatRecord(timeStr, temp, limit, mode, status);
        recordsList.add(record);
        logTable.scrollTo(record);
    }

    // --- 2. Отправка команды установки нового порога ---
    @FXML
    private void handleSetLimit() {
        String valText = newLimitField.getText().trim();
        if (valText.isEmpty()) {
            showAlert("Внимание", "Введите числовое значение нового порога!");
            return;
        }

        try {
            double newLimit = Double.parseDouble(valText);
            sendCommand("SET:" + newLimit);
            newLimitField.clear();
        } catch (NumberFormatException e) {
            showAlert("Ошибка формата", "Порог должен быть числом (например, 28 или 28.5)!");
        }
    }

    // --- 3. Отправка команды переключения режима ---
    @FXML
    private void handleToggleMode() {
        sendCommand("MODE");
    }

    // Вспомогательный метод отправки команды через SimpleSerial
    private void sendCommand(String cmd) {
        if (serial.isConnected()) {
            serial.sendLine(cmd);
        } else {
            showAlert("Внимание", "Сначала подключитесь к COM-порту!");
        }
    }

    // --- 4. Экспорт журнала в файл CSV ---
    @FXML
    private void handleExportCsv() {
        if (recordsList.isEmpty()) {
            showAlert("Экспорт", "В журнале нет записей для сохранения!");
            return;
        }

        try {
            String fileName = "thermostat_telemetry_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv";
            File file = new File(fileName);
            PrintWriter writer = new PrintWriter(file, "UTF-8");

            // Заголовок CSV
            writer.println("Время;Температура_C;Порог_C;Режим;Статус");

            for (ThermostatRecord rec : recordsList) {
                writer.println(rec.getTime() + ";" + rec.getTemperature() + ";" + rec.getThreshold() + ";" + rec.getMode() + ";" + rec.getStatus());
            }

            writer.close();
            showAlert("Экспорт завершен", "Данные успешно выгружены в файл:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            showAlert("Ошибка экспорта", e.getMessage());
        }
    }

    // --- 5. Очистка таблицы и графика ---
    @FXML
    private void handleClearData() {
        recordsList.clear();
        tempSeries.getData().clear();
        limitSeries.getData().clear();
    }

    // Корректное закрытие порта при выходе
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
