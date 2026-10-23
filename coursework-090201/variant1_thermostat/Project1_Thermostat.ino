#include <Wire.h>
#include <LiquidCrystal_I2C.h>

// ============================================================================
// Проект 1: Термостат климат-контроля с настраиваемым порогом температуры
// Курсовой проект по специальности 09.02.01 «Компьютерные системы и комплексы»
// ============================================================================

// Дисплей LCD 1602 с адаптером I2C (адрес 0x27)
LiquidCrystal_I2C lcd(0x27, 16, 2);

// Назначение выводов (пинов) платы Arduino Uno
const int PIN_NTC        = A0; // Аналоговый вход датчика температуры
const int PIN_BTN_PLUS   = 2;  // Кнопка увеличения порога [+]
const int PIN_BTN_MINUS  = 3;  // Кнопка уменьшения порога [-]
const int PIN_BTN_MODE   = 4;  // Кнопка смены режима [MODE]
const int PIN_LED_GREEN  = 5;  // Зеленый светодиод (Норма)
const int PIN_LED_RED    = 6;  // Красный светодиод (Тревога)
const int PIN_BUZZER     = 7;  // Пьезоэлектрический излучатель (зуммер)

// Рабочие параметры термостата
float porog = 25.0;            // Установленный порог срабатывания (°C)
bool modeAbove = true;         // true = тревога при Т > порога; false = при Т < порога
unsigned long timerSerial = 0; // Таймер периодической отправки данных (1 раз в сек)

void setup() {
  // Инициализация последовательного порта для связи с АРМ оператора (Java)
  Serial.begin(9600);
  Serial.setTimeout(50); // Минимальный таймаут ожидания для быстрой работы

  // Защита от зависания шины I2C
  Wire.begin();
  #if defined(WIRE_HAS_TIMEOUT)
  Wire.setWireTimeout(3000, true);
  #endif

  // Настройка цифровых входов для кнопок (с внутренней подтяжкой к +5V)
  pinMode(PIN_BTN_PLUS,  INPUT_PULLUP);
  pinMode(PIN_BTN_MINUS, INPUT_PULLUP);
  pinMode(PIN_BTN_MODE,  INPUT_PULLUP);

  // Настройка цифровых выходов светодиодов и зуммера
  pinMode(PIN_LED_GREEN, OUTPUT);
  pinMode(PIN_LED_RED,   OUTPUT);
  pinMode(PIN_BUZZER,    OUTPUT);

  // Инициализация жидкокристаллического индикатора
  lcd.init();
  lcd.backlight();
  lcd.clear();

  // Приветственный статус и стартовый пакет телеметрии
  Serial.println(F("SYSTEM_INIT_OK"));
  Serial.print(F("TEMP:"));
  Serial.print(25.0, 1);
  Serial.print(F(";LIMIT:"));
  Serial.print(porog, 1);
  Serial.print(F(";MODE:"));
  Serial.print(modeAbove ? F("ABOVE") : F("BELOW"));
  Serial.print(F(";STATUS:"));
  Serial.println(F("NORMAL"));
}

void loop() {
  // 1. Чтение аналогового сигнала с термистора NTC
  int raw = analogRead(PIN_NTC);

  // Расчет температуры по линейной пропорции калибровки:
  // При 25 °C датчик выдает 512 единиц (середина шкалы АЦП 0..1023).
  // Изменение на каждые 10 единиц АЦП соответствует смещению на 1 °C.
  float temp = 25.0 + (512 - raw) / 10.0;

  // 2. Обработка нажатий аппаратных кнопок управления
  if (digitalRead(PIN_BTN_PLUS) == LOW) {
    porog += 0.5; // Увеличить порог на 0.5 °C
    delay(150);   // Аппаратная задержка для подавления дребезга
  }

  if (digitalRead(PIN_BTN_MINUS) == LOW) {
    porog -= 0.5; // Уменьшить порог на 0.5 °C
    delay(150);
  }

  if (digitalRead(PIN_BTN_MODE) == LOW) {
    modeAbove = !modeAbove; // Инвертировать логику срабатывания (> / <)
    delay(200);
  }

  // 3. Анализ аварийного состояния
  bool isAlert = false;
  if (modeAbove) {
    isAlert = (temp > porog); // Режим контроля перегрева (T > порога)
  } else {
    isAlert = (temp < porog); // Режим контроля охлаждения (T < порога)
  }

  // 4. Управление светозвуковой сигнализацией
  if (isAlert) {
    digitalWrite(PIN_LED_GREEN, LOW);
    digitalWrite(PIN_LED_RED, HIGH);
    tone(PIN_BUZZER, 1000); // Звуковой сигнал частотой 1 кГц
  } else {
    digitalWrite(PIN_LED_GREEN, HIGH);
    digitalWrite(PIN_LED_RED, LOW);
    noTone(PIN_BUZZER);
  }

  // 5. Вывод текущих данных на дисплей LCD 1602
  lcd.setCursor(0, 0);
  lcd.print("T:");
  lcd.print(temp, 1);
  lcd.print("C P:");
  lcd.print(porog, 1);
  lcd.print("C ");

  lcd.setCursor(0, 1);
  lcd.print(modeAbove ? "Mode:> " : "Mode:< ");
  lcd.print(isAlert ? "ALERT! " : "NORM   ");

  // 6. Отправка пакета телеметрии в COM-порт (раз в секунду)
  if (millis() - timerSerial >= 1000) {
    timerSerial = millis();

    Serial.print("TEMP:");
    Serial.print(temp, 1);

    Serial.print(";LIMIT:");
    Serial.print(porog, 1);

    Serial.print(";MODE:");
    Serial.print(modeAbove ? "ABOVE" : "BELOW");

    Serial.print(";STATUS:");
    Serial.println(isAlert ? "ALERT" : "NORMAL");
  }

  // 7. Прием команд управления от Java-клиента
  if (Serial.available() > 0) {
    String cmd = Serial.readStringUntil('\n');
    cmd.trim();

    if (cmd == "MODE") {
      modeAbove = !modeAbove;
      Serial.print("ACK:MODE=");
      Serial.println(modeAbove ? "ABOVE" : "BELOW");
    }

    if (cmd.startsWith("SET:")) {
      String valuePart = cmd.substring(4);
      porog = valuePart.toFloat();
      Serial.print("ACK:THRESHOLD_SET=");
      Serial.println(porog, 1);
    }
  }
}
