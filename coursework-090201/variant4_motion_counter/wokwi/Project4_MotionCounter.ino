#include <Wire.h>
#include <LiquidCrystal_I2C.h>

// ========================================================
// Проект 4: Счётчик движений с настраиваемым порогом
// Курсовой проект для специальности 09.02.01 (Wokwi Backend)
// ========================================================

LiquidCrystal_I2C lcd(0x27, 16, 2);

// Назначение пинов
const int PIN_PIR        = 2; // PIR датчик движения (OUT)
const int PIN_BTN_PLUS   = 3; // Кнопка [+] (Увеличить порог)
const int PIN_BTN_MINUS  = 4; // Кнопка [-] (Уменьшить порог)
const int PIN_BTN_RESET  = 5; // Кнопка [Сброс счетчика]
const int PIN_LED_RED    = 6; // Красный светодиод (Превышение)
const int PIN_BUZZER     = 7; // Пьезо-зуммер (Тревога)

// Переменные логики
int motionCount = 0;          // Текущее число движений
int threshold   = 10;         // Порог по умолчанию (10)
bool lastPirState = LOW;

// Таймеры
unsigned long lastSerialSend = 0;
unsigned long lastBtnCheck   = 0;
unsigned long lastLcdUpdate  = 0;
unsigned long lastAlarmBeep  = 0;

// Подавление дребезга кнопок
bool lastPlusState  = HIGH;
bool lastMinusState = HIGH;
bool lastResetState = HIGH;

void updateLcd();

void setup() {
  Serial.begin(9600);

  pinMode(PIN_PIR, INPUT);
  pinMode(PIN_BTN_PLUS,  INPUT_PULLUP);
  pinMode(PIN_BTN_MINUS, INPUT_PULLUP);
  pinMode(PIN_BTN_RESET, INPUT_PULLUP);

  pinMode(PIN_LED_RED, OUTPUT);
  pinMode(PIN_BUZZER,  OUTPUT);
  digitalWrite(PIN_LED_RED, LOW);
  noTone(PIN_BUZZER);

  lcd.init();
  lcd.backlight();
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("PIR COUNTER v1.0");
  lcd.setCursor(0, 1);
  lcd.print("WARMING SENSOR..");
  delay(1200);
  lcd.clear();

  updateLcd();
}

void loop() {
  unsigned long now = millis();

  // 1. Опрос кнопок управления
  handleButtons(now);

  // 2. Детекция движения с PIR датчика (отслеживаем фронт LOW -> HIGH)
  bool currentPir = digitalRead(PIN_PIR);
  if (currentPir == HIGH && lastPirState == LOW) {
    motionCount++;
    tone(PIN_BUZZER, 2000, 30); // Короткий щелчок подтверждения
    updateLcd();
  }
  lastPirState = currentPir;

  // 3. Проверка превышения порога
  bool isAlarm = (motionCount > threshold);
  if (isAlarm) {
    digitalWrite(PIN_LED_RED, HIGH);
    // Частый прерывистый писк
    if (now - lastAlarmBeep >= 300) {
      lastAlarmBeep = now;
      tone(PIN_BUZZER, 1600, 100);
    }
  } else {
    digitalWrite(PIN_LED_RED, LOW);
  }

  // 4. Периодическое обновление LCD
  if (now - lastLcdUpdate >= 250) {
    lastLcdUpdate = now;
    updateLcd();
  }

  // 5. Отправка данных в COM-порт (раз в 1 секунду)
  if (now - lastSerialSend >= 1000) {
    lastSerialSend = now;
    // Формат для Java: COUNT:5;LIMIT:10;STATUS:NORMAL
    Serial.print("COUNT:");
    Serial.print(motionCount);
    Serial.print(";LIMIT:");
    Serial.print(threshold);
    Serial.print(";STATUS:");
    Serial.println(isAlarm ? "ALERT" : "NORMAL");
  }

  // 6. Прием команд управления от Java приложения
  if (Serial.available() > 0) {
    String cmd = Serial.readStringUntil('\n');
    cmd.trim();
    if (cmd == "RESET" || cmd == "CMD:RESET") {
      motionCount = 0;
      updateLcd();
      Serial.println("ACK:COUNT_RESET");
    } else if (cmd.startsWith("SET:")) {
      int newThreshold = cmd.substring(4).toInt();
      if (newThreshold > 0 && newThreshold <= 999) {
        threshold = newThreshold;
        updateLcd();
        Serial.print("ACK:THRESHOLD_SET=");
        Serial.println(threshold);
      }
    } else if (cmd == "STATUS") {
      Serial.print("COUNT:");
      Serial.print(motionCount);
      Serial.print(";LIMIT:");
      Serial.print(threshold);
      Serial.print(";STATUS:");
      Serial.println(isAlarm ? "ALERT" : "NORMAL");
    }
  }
}

void handleButtons(unsigned long now) {
  if (now - lastBtnCheck < 50) return;
  lastBtnCheck = now;

  bool btnPlus  = digitalRead(PIN_BTN_PLUS);
  bool btnMinus = digitalRead(PIN_BTN_MINUS);
  bool btnReset = digitalRead(PIN_BTN_RESET);

  // Кнопка [+]
  if (btnPlus == LOW && lastPlusState == HIGH) {
    threshold++;
    if (threshold > 999) threshold = 999;
    updateLcd();
  }
  lastPlusState = btnPlus;

  // Кнопка [-]
  if (btnMinus == LOW && lastMinusState == HIGH) {
    threshold--;
    if (threshold < 1) threshold = 1;
    updateLcd();
  }
  lastMinusState = btnMinus;

  // Кнопка [Сброс]
  if (btnReset == LOW && lastResetState == HIGH) {
    motionCount = 0;
    updateLcd();
  }
  lastResetState = btnReset;
}

void updateLcd() {
  lcd.setCursor(0, 0);
  lcd.print("DVIZH:");
  lcd.print(motionCount);
  lcd.print(" /POR:");
  lcd.print(threshold);
  lcd.print("    ");

  lcd.setCursor(0, 1);
  if (motionCount > threshold) {
    lcd.print("! LIMIT EXCEED !");
  } else {
    lcd.print("STATUS: NORMAL  ");
  }
}
