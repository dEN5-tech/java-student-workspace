#include <Wire.h>
#include <LiquidCrystal_I2C.h>

// ========================================================
// Проект 3: Таймер обратного отсчёта с двумя индикаторами
// Курсовой проект для специальности 09.02.01 (Wokwi Backend)
// Автономный драйвер TM1637 без внешних зависимостей!
// ========================================================

LiquidCrystal_I2C lcd(0x27, 16, 2);

// Назначение пинов кнопок (INPUT_PULLUP)
const int PIN_BTN_PLUS   = 2; // Кнопка [+]
const int PIN_BTN_MINUS  = 3; // Кнопка [-]
const int PIN_BTN_START  = 4; // Кнопка [СТАРТ]
const int PIN_BTN_STOP   = 5; // Кнопка [СТОП/СБРОС]

// 4-разрядный семисегментный дисплей TM1637
const int PIN_DISP_CLK   = 6;
const int PIN_DISP_DIO   = 7;

// Индикация тревоги
const int PIN_BUZZER     = 8; // Пьезо-зуммер
const int PIN_LED        = 9; // Красный светодиод окончания отсчета

// Коды цифр 0..9 для 7-сегментника TM1637
const uint8_t digitPatterns[] = {
  0x3f, // 0
  0x06, // 1
  0x5b, // 2
  0x4f, // 3
  0x66, // 4
  0x6d, // 5
  0x7d, // 6
  0x07, // 7
  0x7f, // 8
  0x6f  // 9
};

// Состояния работы
enum State {
  SETTING,    // Настройка времени
  RUNNING,    // Идет обратный отсчет
  PAUSED,     // Пауза
  ALARM       // Время истекло (звук + мигание)
};

State currentState = SETTING;
long targetSeconds = 60;    // По умолчанию 1 минута (01:00)
long remainingSeconds = 60; // Оставшееся время

unsigned long lastTickTime   = 0;
unsigned long lastBtnCheck   = 0;
unsigned long lastSerialSend = 0;
unsigned long lastAlarmBlink = 0;
bool alarmLedState = false;

// Подавление дребезга кнопок
bool lastPlusState  = HIGH;
bool lastMinusState = HIGH;
bool lastStartState = HIGH;
bool lastStopState  = HIGH;

void updateDisplays();
void tmDisplayTime(int mins, int secs);
void handleButtons(unsigned long now);
void resetTimer();

void setup() {
  Serial.begin(9600);
  Serial.setTimeout(50);

  // Защита от зависания шины I2C
  Wire.begin();
  #if defined(WIRE_HAS_TIMEOUT)
  Wire.setWireTimeout(3000, true);
  #endif

  pinMode(PIN_BTN_PLUS,  INPUT_PULLUP);
  pinMode(PIN_BTN_MINUS, INPUT_PULLUP);
  pinMode(PIN_BTN_START, INPUT_PULLUP);
  pinMode(PIN_BTN_STOP,  INPUT_PULLUP);

  pinMode(PIN_DISP_CLK, OUTPUT);
  pinMode(PIN_DISP_DIO, OUTPUT);
  digitalWrite(PIN_DISP_CLK, HIGH);
  digitalWrite(PIN_DISP_DIO, HIGH);

  pinMode(PIN_BUZZER, OUTPUT);
  pinMode(PIN_LED,    OUTPUT);
  digitalWrite(PIN_BUZZER, LOW);
  digitalWrite(PIN_LED,    LOW);

  // Инициализация LCD
  lcd.init();
  lcd.backlight();
  lcd.clear();
  lcd.setCursor(2, 0);
  lcd.print("TIMER SYSTEM");
  lcd.setCursor(3, 1);
  lcd.print("READY 01:00");

  delay(500);
  lcd.clear();
  updateDisplays();

  // Приветственный статус и стартовый пакет телеметрии
  Serial.println(F("SYSTEM_INIT_OK"));
  Serial.println(F("TIME:01:00;STATE:SETTING;REMAINING:60"));
}

void loop() {
  unsigned long now = millis();

  // 1. Опрос кнопок
  handleButtons(now);

  // 2. Логика счета секунд
  if (now - lastTickTime >= 1000) {
    lastTickTime = now;

    if (currentState == RUNNING) {
      if (remainingSeconds > 0) {
        remainingSeconds--;
        digitalWrite(PIN_LED, !digitalRead(PIN_LED)); // Мигание светодиодом в процессе
        updateDisplays();
      } else {
        // Время вышло!
        currentState = ALARM;
        digitalWrite(PIN_LED, HIGH);
        updateDisplays();
      }
    }
  }

  // 3. Аварийный режим (время вышло)
  if (currentState == ALARM) {
    if (now - lastAlarmBlink >= 250) {
      lastAlarmBlink = now;
      alarmLedState = !alarmLedState;
      digitalWrite(PIN_LED, alarmLedState ? HIGH : LOW);
      if (alarmLedState) tone(PIN_BUZZER, 1500, 150);
    }
  }

  // 4. Отправка телеметрии в COM-порт (раз в 1 секунду)
  if (now - lastSerialSend >= 1000) {
    lastSerialSend = now;
    int mins = remainingSeconds / 60;
    int secs = remainingSeconds % 60;

    char timeBuf[10];
    sprintf(timeBuf, "%02d:%02d", mins, secs);

    String stStr = "SETTING";
    if (currentState == RUNNING) stStr = "RUN";
    if (currentState == PAUSED)  stStr = "PAUSED";
    if (currentState == ALARM)   stStr = "TIME_OVER";

    // Формат для Java: TIME:00:59;STATE:RUN;REMAINING:59
    Serial.print("TIME:");
    Serial.print(timeBuf);
    Serial.print(";STATE:");
    Serial.print(stStr);
    Serial.print(";REMAINING:");
    Serial.println(remainingSeconds);
  }

  // 5. Прием команд от Java приложения
  if (Serial.available() > 0) {
    String cmd = Serial.readStringUntil('\n');
    cmd.trim();
    if (cmd == "START" || cmd == "CMD:START") {
      if (currentState == SETTING || currentState == PAUSED) {
        currentState = RUNNING;
        updateDisplays();
        Serial.println(F("ACK:TIMER_STARTED"));
      } else if (currentState == ALARM) {
        resetTimer();
        currentState = RUNNING;
        updateDisplays();
        Serial.println(F("ACK:TIMER_STARTED"));
      }
    } else if (cmd == "STOP" || cmd == "CMD:STOP") {
      if (currentState == RUNNING) {
        currentState = PAUSED;
        noTone(PIN_BUZZER);
        digitalWrite(PIN_LED, LOW);
        updateDisplays();
        Serial.println(F("ACK:TIMER_PAUSED"));
      }
    } else if (cmd == "RESET" || cmd == "CMD:RESET") {
      resetTimer();
      Serial.println(F("ACK:TIMER_RESET"));
    } else if (cmd.startsWith("SET:")) {
      long newSec = cmd.substring(4).toInt();
      if (newSec > 0 && newSec <= 3599) {
        targetSeconds = newSec;
        remainingSeconds = targetSeconds;
        currentState = SETTING;
        updateDisplays();
        Serial.print(F("ACK:TIME_SET="));
        Serial.println(newSec);
      }
    }
  }
}

// Опрос кнопок с подавлением дребезга
void handleButtons(unsigned long now) {
  if (now - lastBtnCheck < 50) return;
  lastBtnCheck = now;

  bool btnPlus  = digitalRead(PIN_BTN_PLUS);
  bool btnMinus = digitalRead(PIN_BTN_MINUS);
  bool btnStart = digitalRead(PIN_BTN_START);
  bool btnStop  = digitalRead(PIN_BTN_STOP);

  // Кнопка [+]
  if (btnPlus == LOW && lastPlusState == HIGH) {
    if (currentState == SETTING || currentState == PAUSED) {
      targetSeconds += 10;
      if (targetSeconds > 3599) targetSeconds = 3599; // Лимит 59:59
      remainingSeconds = targetSeconds;
      updateDisplays();
    }
  }
  lastPlusState = btnPlus;

  // Кнопка [-]
  if (btnMinus == LOW && lastMinusState == HIGH) {
    if (currentState == SETTING || currentState == PAUSED) {
      targetSeconds -= 10;
      if (targetSeconds < 10) targetSeconds = 10;
      remainingSeconds = targetSeconds;
      updateDisplays();
    }
  }
  lastMinusState = btnMinus;

  // Кнопка [Старт]
  if (btnStart == LOW && lastStartState == HIGH) {
    if (currentState == SETTING || currentState == PAUSED) {
      currentState = RUNNING;
      updateDisplays();
    } else if (currentState == ALARM) {
      resetTimer();
    }
  }
  lastStartState = btnStart;

  // Кнопка [Стоп]
  if (btnStop == LOW && lastStopState == HIGH) {
    if (currentState == RUNNING) {
      currentState = PAUSED;
      digitalWrite(PIN_LED, LOW);
      updateDisplays();
    } else {
      resetTimer();
    }
  }
  lastStopState = btnStop;
}

void resetTimer() {
  currentState = SETTING;
  remainingSeconds = targetSeconds;
  noTone(PIN_BUZZER);
  digitalWrite(PIN_LED, LOW);
  updateDisplays();
}

// Драйвер TM1637 (I2C-подобный протокол)
void tmStart() {
  digitalWrite(PIN_DISP_CLK, HIGH);
  digitalWrite(PIN_DISP_DIO, HIGH);
  delayMicroseconds(5);
  digitalWrite(PIN_DISP_DIO, LOW);
}

void tmStop() {
  digitalWrite(PIN_DISP_CLK, LOW);
  digitalWrite(PIN_DISP_DIO, LOW);
  delayMicroseconds(5);
  digitalWrite(PIN_DISP_CLK, HIGH);
  digitalWrite(PIN_DISP_DIO, HIGH);
}

void tmWriteByte(uint8_t b) {
  for (int i = 0; i < 8; i++) {
    digitalWrite(PIN_DISP_CLK, LOW);
    digitalWrite(PIN_DISP_DIO, (b & 0x01) ? HIGH : LOW);
    delayMicroseconds(5);
    digitalWrite(PIN_DISP_CLK, HIGH);
    delayMicroseconds(5);
    b >>= 1;
  }
  // ACK
  digitalWrite(PIN_DISP_CLK, LOW);
  pinMode(PIN_DISP_DIO, INPUT);
  delayMicroseconds(5);
  digitalWrite(PIN_DISP_CLK, HIGH);
  delayMicroseconds(5);
  pinMode(PIN_DISP_DIO, OUTPUT);
}

void tmDisplayTime(int mins, int secs) {
  uint8_t d1 = digitPatterns[(mins / 10) % 10];
  uint8_t d2 = digitPatterns[mins % 10] | 0x80; // 0x80 включает двоеточие ":"
  uint8_t d3 = digitPatterns[(secs / 10) % 10];
  uint8_t d4 = digitPatterns[secs % 10];

  tmStart();
  tmWriteByte(0x40); // Автоинкремент адреса
  tmStop();

  tmStart();
  tmWriteByte(0xC0); // Начальный адрес сегментов
  tmWriteByte(d1);
  tmWriteByte(d2);
  tmWriteByte(d3);
  tmWriteByte(d4);
  tmStop();

  tmStart();
  tmWriteByte(0x88 | 0x07); // Включить дисплей, максимальная яркость
  tmStop();
}

// Вывод на LCD1602 и семисегментник TM1637
void updateDisplays() {
  int mins = remainingSeconds / 60;
  int secs = remainingSeconds % 60;

  // 1. Обновление TM1637
  tmDisplayTime(mins, secs);

  // 2. Обновление LCD
  lcd.setCursor(0, 0);
  lcd.print("TIME: ");
  if (mins < 10) lcd.print("0");
  lcd.print(mins);
  lcd.print(":");
  if (secs < 10) lcd.print("0");
  lcd.print(secs);
  lcd.print("      ");

  lcd.setCursor(0, 1);
  if (currentState == SETTING) lcd.print("STATUS: SETTING ");
  if (currentState == RUNNING) lcd.print("STATUS: RUNNING ");
  if (currentState == PAUSED)  lcd.print("STATUS: PAUSED  ");
  if (currentState == ALARM)   lcd.print("!! TIME OVER! !!");
}
