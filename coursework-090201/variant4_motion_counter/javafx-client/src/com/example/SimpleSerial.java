package com.example;

import com.fazecast.jSerialComm.SerialPort;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * Класс-обертка: SimpleSerial
 * Специально для студентов специальности 09.02.01 «Компьютерные системы и комплексы»
 * 
 * Назначение: Максимально простая и надежная работа с COM-портом (Wokwi / Arduino).
 * Скрывает всю сложную работу с потоками, таймаутами и буферами ОС.
 * 
 * Все основные методы:
 * 1. SimpleSerial.getAvailablePorts()       -> Получить список портов (["COM1", "COM3"])
 * 2. serial.connect("COM3", 9600)          -> Подключиться к порту (true/false)
 * 3. serial.setOnLineReceived(line -> ...) -> Задать действие при получении строки
 * 4. serial.sendLine("SET:28.0")           -> Отправить команду на плату
 * 5. serial.isConnected()                  -> Проверить, подключен ли сейчас (true/false)
 * 6. serial.disconnect()                   -> Отключиться и освободить порт
 * ============================================================================
 */
public class SimpleSerial {

    /**
     * Простой интерфейс слушателя для приема строк.
     */
    public interface LineListener {
        void onLineReceived(String line);
    }

    // Внутренние переменные
    private SerialPort activePort;
    private Thread readerThread;
    private volatile boolean isRunning = false;
    private LineListener lineListener;

    /**
     * 1. Получить список всех доступных COM-портов в системе Windows.
     * Полезно для выпадающего списка ComboBox.
     */
    public static List<String> getAvailablePorts() {
        List<String> portsList = new ArrayList<>();
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort p : ports) {
            portsList.add(p.getSystemPortName());
        }
        return portsList;
    }

    /**
     * 2. Подключиться к указанному COM-порту на заданной скорости (обычно 9600).
     * @param portName Имя порта, например "COM3"
     * @param baudRate Скорость, например 9600
     * @return true - успешно подключились, false - ошибка открытия порта
     */
    public boolean connect(String portName, int baudRate) {
        // Если уже было открыто соединение, сначала безопасно закрываем его
        disconnect();

        if (portName == null || portName.trim().isEmpty()) {
            System.err.println("[SimpleSerial] Ошибка: Не указано имя COM-порта!");
            return false;
        }

        // Находим порт в системе
        activePort = SerialPort.getCommPort(portName.trim());
        activePort.setBaudRate(baudRate);
        activePort.setNumDataBits(8);
        activePort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        activePort.setParity(SerialPort.NO_PARITY);

        // Таймаут полублокирующего чтения (100 мс)
        activePort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);

        // Открываем порт в Windows
        boolean isOpened = activePort.openPort();
        if (!isOpened) {
            System.err.println("[SimpleSerial] Не удалось открыть порт " + portName + " (возможно, он занят другой программой)");
            return false;
        }

        System.out.println("[SimpleSerial] Порт " + portName + " успешно открыт (" + baudRate + " бод).");

        // Запускаем фоновый поток автоматического чтения
        startReadingThread();
        return true;
    }

    /**
     * 3. Задать обработчик (действие), который будет автоматически вызываться
     * каждый раз, когда плата присылает законченную строку телеметрии.
     * 
     * Пример использования:
     * serial.setOnLineReceived(line -> {
     *     System.out.println("Получена строка: " + line);
     * });
     */
    public void setOnLineReceived(LineListener listener) {
        this.lineListener = listener;
    }

    /**
     * 4. Отправить текстовую строку в плату.
     * Символ перевода строки \n добавляется автоматически.
     * 
     * Пример: serial.sendLine("MODE"); или serial.sendLine("SET:28.5");
     */
    public boolean sendLine(String message) {
        if (!isConnected()) {
            System.err.println("[SimpleSerial] Ошибка отправки: Порт закрыт!");
            return false;
        }

        try {
            String textToSend = message + "\n";
            byte[] bytes = textToSend.getBytes(StandardCharsets.UTF_8);
            activePort.getOutputStream().write(bytes);
            activePort.getOutputStream().flush();
            System.out.println("[SimpleSerial Отправлено]: " + message);
            return true;
        } catch (Exception e) {
            System.err.println("[SimpleSerial] Сбой отправки команды: " + e.getMessage());
            return false;
        }
    }

    /**
     * 5. Проверить, открыт ли порт в данный момент.
     */
    public boolean isConnected() {
        return activePort != null && activePort.isOpen();
    }

    /**
     * 6. Получить текущее имя подключенного порта (например, "COM3").
     */
    public String getPortName() {
        if (activePort != null) {
            return activePort.getSystemPortName();
        }
        return "";
    }

    /**
     * 7. Отключиться от COM-порта и корректно освободить ресурсы ОС.
     */
    public void disconnect() {
        isRunning = false;

        // Прерываем фоновый поток чтения
        if (readerThread != null && readerThread.isAlive()) {
            readerThread.interrupt();
        }

        // Закрываем системный порт
        if (activePort != null && activePort.isOpen()) {
            try {
                activePort.closePort();
                System.out.println("[SimpleSerial] Порт успешно закрыт.");
            } catch (Exception e) {
                System.err.println("[SimpleSerial] Ошибка при закрытии порта: " + e.getMessage());
            }
        }
        activePort = null;
    }

    // --- Внутренний фоновый поток чтения (скрыт от студента) ---
    private void startReadingThread() {
        isRunning = true;
        readerThread = new Thread(() -> {
            StringBuilder lineBuffer = new StringBuilder();
            byte[] buffer = new byte[1024];

            while (isRunning && activePort != null && activePort.isOpen()) {
                int bytesRead = activePort.readBytes(buffer, buffer.length);
                if (bytesRead > 0) {
                    // Преобразуем поступившие байты в текст UTF-8
                    String chunk = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                    lineBuffer.append(chunk);

                    // Извлекаем полные строки по символу \n
                    int newlineIndex;
                    while ((newlineIndex = lineBuffer.indexOf("\n")) != -1) {
                        String completeLine = lineBuffer.substring(0, newlineIndex).trim();
                        lineBuffer.delete(0, newlineIndex + 1);

                        // Если строка не пустая и есть слушатель - передаем строку
                        if (!completeLine.isEmpty() && lineListener != null) {
                            try {
                                lineListener.onLineReceived(completeLine);
                            } catch (Exception ex) {
                                System.err.println("[SimpleSerial] Ошибка в обработчике строки: " + ex.getMessage());
                            }
                        }
                    }
                } else {
                    // Пауза 20 мс, чтобы не нагружать процессор при пустом буфере
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }, "SimpleSerial-ReaderThread");

        // Поток-демон автоматически завершается вместе с закрытием программы
        readerThread.setDaemon(true);
        readerThread.start();
    }
}
