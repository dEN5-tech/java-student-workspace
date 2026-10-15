#include <Wire.h>
#include <LiquidCrystal_I2C.h>

// ============================================================================
// Проект 2: Ультразвуковой дальномер с трёхзонной сигнализацией
// Курсовой проект по специальности 09.02.01 «Компьютерные системы и комплексы»
// ============================================================================

// Дисплей LCD 1602 с адаптером I2C (адрес 0x27)
LiquidCrystal_I2C lcd(0x27, 16, 2);

// Назначение выводов (пинов) платы Arduino Uno
const int PIN_TRIG       = 2; // Выход импульса датчика HC-SR04 (Trig)
const int PIN_ECHO       = 3; // Вход эхо-сигнала датчика HC-SR04 (Echo)
const int PIN_LED_RED    = 4; // Красный светодиод (Зона опасности < 20 см)
const int PIN_LED_YELLOW = 5; // Желтый светодиод (Зона внимания 20..100 см)
const int PIN_LED_GREEN  = 6; // Зеленый светодиод (Безопасная зона > 100 см)
const int PIN_BUZZER     = 7; // Зуммер звуковой сигнализации

// Временные переменные
unsigned long timerSerial = 0; // Таймер отправки данных в COM-порт
unsigned long timerBeep   = 0; // Таймер прерывистого пищания
bool buzzerMuted = false;      // Флаг отключения звука (команда MUTE)

void setup() {
  // Инициализация последовательного порта (9600 бод)
  Serial.begin(9600);
  Serial.setTimeout(50); // Минимальный таймаут чтения команд

  // Настройка выводов ультразвукового датчика
  pinMode(PIN_TRIG, OUTPUT);
  pinMode(PIN_ECHO, INPUT);

  // Настройка выводов светодиодов и зуммера
  pinMode(PIN_LED_RED,    OUTPUT);
  pinMode(PIN_LED_YELLOW, OUTPUT);
  pinMode(PIN_LED_GREEN,  OUTPUT);
  pinMode(PIN_BUZZER,     OUTPUT);

  // Начальное состояние индикаторов
  digitalWrite(PIN_TRIG, LOW);
  digitalWrite(PIN_LED_RED, LOW);
  digitalWrite(PIN_LED_YELLOW, LOW);
  digitalWrite(PIN_LED_GREEN, LOW);
  noTone(PIN_BUZZER);

  // Инициализация экрана LCD
  lcd.init();
  lcd.backlight();
  lcd.clear();
}

void loop() {
  // 1. Посылка ультразвукового импульса длительностью 10 микросекунд
  digitalWrite(PIN_TRIG, LOW);
  delayMicroseconds(2);
  digitalWrite(PIN_TRIG, HIGH);
  delayMicroseconds(10);
  digitalWrite(PIN_TRIG, LOW);

  // 2. Измерение времени возврата отраженного импульса (в микросекундах)
  long duration = pulseIn(PIN_ECHO, HIGH, 30000);

  // Простой расчет расстояния в сантиметрах:
  // Звук проходит 1 см примерно за 58 микросекунд (туда и обратно)
  int distance = 400;
  if (duration > 0) {
    distance = duration / 58;
  }

  // 3. Определение зон дистанции и управление светодиодами
  String zone = "SAFE";

  if (distance < 20) {
    // Зона 1: Опасность (дистанция менее 20 см)
    zone = "DANGER";
    digitalWrite(PIN_LED_RED, HIGH);
    digitalWrite(PIN_LED_YELLOW, LOW);
    digitalWrite(PIN_LED_GREEN, LOW);

    // Непрерывный звуковой сигнал высокой частоты
    if (!buzzerMuted) {
      tone(PIN_BUZZER, 1800);
    } else {
      noTone(PIN_BUZZER);
    }

  } else if (distance <= 100) {
    // Зона 2: Предупреждение (дистанция от 20 до 100 см)
    zone = "WARNING";
    digitalWrite(PIN_LED_RED, LOW);
    digitalWrite(PIN_LED_YELLOW, HIGH);
    digitalWrite(PIN_LED_GREEN, LOW);

    // Прерывистый писк: частота повторения пропорциональна расстоянию
    int interval = distance * 5; // Чем ближе объект, тем чаще сигналы
    if (!buzzerMuted) {
      if (millis() - timerBeep >= interval) {
        timerBeep = millis();
        tone(PIN_BUZZER, 1200, 40);
      }
    } else {
      noTone(PIN_BUZZER);
    }

  } else {
    // Зона 3: Безопасность (дистанция более 100 см)
    zone = "SAFE";
    digitalWrite(PIN_LED_RED, LOW);
    digitalWrite(PIN_LED_YELLOW, LOW);
    digitalWrite(PIN_LED_GREEN, HIGH);
    noTone(PIN_BUZZER);
  }

  // 4. Отображение информации на экране LCD 1602
  lcd.setCursor(0, 0);
  lcd.print("DIST: ");
  if (distance >= 400) {
    lcd.print(">400 cm  ");
  } else {
    lcd.print(distance);
    lcd.print(" cm    ");
  }

  lcd.setCursor(0, 1);
  lcd.print("ZONE: ");
  if (zone == "DANGER")  lcd.print("DANGER! ");
  if (zone == "WARNING") lcd.print("WARNING ");
  if (zone == "SAFE")    lcd.print("SAFE OK ");

  // 5. Отправка пакета телеметрии в COM-порт (раз в секунду)
  if (millis() - timerSerial >= 1000) {
    timerSerial = millis();

    Serial.print("DIST:");
    Serial.print(distance);

    Serial.print(";ZONE:");
    Serial.print(zone);

    Serial.print(";STATUS:");
    Serial.println(buzzerMuted ? "MUTED" : "ACTIVE");
  }

  // 6. Прием управляющих команд от Java-приложения
  if (Serial.available() > 0) {
    String cmd = Serial.readStringUntil('\n');
    cmd.trim();

    if (cmd == "MUTE") {
      buzzerMuted = true;
      noTone(PIN_BUZZER);
      Serial.println("ACK:BUZZER_MUTED");
    } else if (cmd == "UNMUTE") {
      buzzerMuted = false;
      Serial.println("ACK:BUZZER_UNMUTED");
    }
  }

  delay(60); // Небольшая пауза между измерениями
}
