#ifndef SIMPLE_ARDUINO_COMM_H
#define SIMPLE_ARDUINO_COMM_H

#include <Arduino.h>

/**
 * ============================================================================
 * Класс-модуль: SimpleArduinoComm (C++ для Arduino)
 * Специальность: 09.02.01 «Компьютерные системы и комплексы»
 * 
 * Назначение:
 * Простой и надежный модуль обмена данными с ПК (Java / C#) через Serial-порт
 * или сетевой сокет Wokwi (tcp:4000).
 * 
 * Преимущества для курсового проекта:
 * 1. 100% НЕБЛОКИРУЮЩИЙ опрос: в отличие от Serial.readStringUntil(),
 *    этот модуль не замораживает работу кнопок, дисплея и датчиков.
 * 2. Автоматически склеивает приходящие символы в целую строку до '\n'.
 * 3. Поддерживает как события (коллбеки), так и прямую проверку команд.
 * 4. Защищен от переполнения оперативной памяти (SRAM).
 * ============================================================================
 */
class SimpleArduinoComm {
public:
    // Тип функции для обработки входящей команды
    typedef void (*CommandHandler)(String command);

    /**
     * Конструктор: по умолчанию работает со стандартным портом Serial
     */
    SimpleArduinoComm(Stream& stream = Serial) 
        : _stream(&stream), _handler(nullptr), _hasCommand(false) {
        _rxBuffer.reserve(96);
    }

    /**
     * 1. Инициализация порта на заданной скорости (например, 9600)
     */
    void begin(unsigned long baudRate = 9600) {
        if (_stream == &Serial) {
            Serial.begin(baudRate);
        }
        _rxBuffer = "";
        _hasCommand = false;
        _lastCommand = "";
    }

    /**
     * 2. Задать функцию, которая будет автоматически вызываться при поступлении команды от ПК.
     * Пример:
     * comm.onCommand([](String cmd) {
     *     if (cmd == "MODE") { ... }
     * });
     */
    void onCommand(CommandHandler handler) {
        _handler = handler;
    }

    /**
     * 3. Опрос входящих байт. Обязательно вызывается в loop()!
     * Не содержит задержек (delay), поэтому выполняется мгновенно.
     */
    void update() {
        while (_stream->available() > 0) {
            char c = (char)_stream->read();

            // Конец строки (\n или \r) означает, что команда полностью принята
            if (c == '\n' || c == '\r') {
                if (_rxBuffer.length() > 0) {
                    _lastCommand = _rxBuffer;
                    _hasCommand = true;

                    // Если зарегистрирован обработчик - вызываем его
                    if (_handler != nullptr) {
                        _handler(_rxBuffer);
                    }
                    _rxBuffer = "";
                }
            } else {
                // Накапливаем символы в буфер с ограничением размера
                if (_rxBuffer.length() < 96) {
                    _rxBuffer += c;
                }
            }
        }
    }

    /**
     * 4. Проверить, пришла ли новая команда (для ручного опроса в loop)
     */
    bool hasCommand() const {
        return _hasCommand;
    }

    /**
     * 5. Считать последнюю команду вручную
     */
    String readCommand() {
        _hasCommand = false;
        String cmd = _lastCommand;
        _lastCommand = "";
        return cmd;
    }

    /**
     * 6. Отправить текстовую строку в сторону ПК (автоматически добавляет \n)
     * Пример: comm.send("TEMP:24.5;LIMIT:25.0;STATUS:NORMAL");
     */
    void send(const String& message) {
        _stream->println(message);
    }

    /**
     * 7. Отправить пару «ключ:значение»
     * Пример: comm.sendValue("TEMP", 24.5, 1);  -> отправит "TEMP:24.5\n"
     */
    void sendValue(const String& key, float value, int decimals = 1) {
        _stream->print(key);
        _stream->print(':');
        _stream->println(value, decimals);
    }

    /**
     * 8. Отправить ответную квитанцию подтверждения (ACK)
     * Пример: comm.sendAck("THRESHOLD_SET", 28.0); -> "ACK:THRESHOLD_SET=28.0\n"
     */
    void sendAck(const String& paramName, float value) {
        _stream->print(F("ACK:"));
        _stream->print(paramName);
        _stream->print('=');
        _stream->println(value, 1);
    }

    void sendAck(const String& paramName, const String& value) {
        _stream->print(F("ACK:"));
        _stream->print(paramName);
        _stream->print('=');
        _stream->println(value);
    }

private:
    Stream* _stream;
    String _rxBuffer;
    String _lastCommand;
    bool _hasCommand;
    CommandHandler _handler;
};

#endif // SIMPLE_ARDUINO_COMM_H
