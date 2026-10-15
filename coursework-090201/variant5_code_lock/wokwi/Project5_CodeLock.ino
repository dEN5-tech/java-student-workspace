#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <IRremote.h>

// ========================================================
// Проект 5: Кодовый замок с ИК-пультом (СКУД)
// Курсовой проект для специальности 09.02.01 (Wokwi Backend)
// ========================================================

LiquidCrystal_I2C lcd(0x27, 16, 2);

// Назначение пинов
const int PIN_IR         = 2; // ИК-приемник TSOP (DAT)
const int PIN_LED_GREEN  = 3; // Зеленый светодиод (Доступ разрешен)
const int PIN_LED_RED    = 4; // Красный светодиод (Доступ запрещен)
const int PIN_BUZZER     = 5; // Пьезо-зуммер

// Пароль по умолчанию (из задания)
String secretCode = "1234";
String inputCode  = "";

// Таймеры для сброса экрана
unsigned long resultShowTime = 0;
bool showingResult = false;

void resetInputScreen();
char mapIrCommand(uint32_t cmd);

void setup() {
  Serial.begin(9600);

  // Инициализация ИК-приемника
  IrReceiver.begin(PIN_IR, false);

  pinMode(PIN_LED_GREEN, OUTPUT);
  pinMode(PIN_LED_RED,   OUTPUT);
  pinMode(PIN_BUZZER,    OUTPUT);

  digitalWrite(PIN_LED_GREEN, LOW);
  digitalWrite(PIN_LED_RED,   LOW);
  noTone(PIN_BUZZER);

  lcd.init();
  lcd.backlight();
  lcd.clear();
  lcd.setCursor(2, 0);
  lcd.print("ACCESS CONTROL");
  lcd.setCursor(3, 1);
  lcd.print("SYSTEM v1.0");
  delay(1200);
  lcd.clear();

  resetInputScreen();
}

void loop() {
  unsigned long now = millis();

  // 1. Автоматический сброс после показа результата (через 2 секунды)
  if (showingResult && (now - resultShowTime >= 2000)) {
    showingResult = false;
    resetInputScreen();
  }

  // 2. Прием сигналов с ИК-пульта
  if (!showingResult && IrReceiver.decode()) {
    uint32_t cmd = IrReceiver.decodedIRData.command;
    char key = mapIrCommand(cmd);

    if (key >= '0' && key <= '9') {
      tone(PIN_BUZZER, 2400, 30); // Щелчок при нажатии
      inputCode += key;

      // Обновляем звездочки на экране
      lcd.setCursor(5 + (inputCode.length() - 1), 1);
      lcd.print("*");

      // Проверка при наборе 4 цифр
      if (inputCode.length() >= 4) {
        showingResult = true;
        resultShowTime = now;

        if (inputCode == secretCode) {
          // УСПЕХ: Код верный
          digitalWrite(PIN_LED_GREEN, HIGH);
          digitalWrite(PIN_LED_RED,   LOW);
          tone(PIN_BUZZER, 2000, 200);

          lcd.setCursor(0, 0);
          lcd.print("STATUS: GRANTED ");
          lcd.setCursor(0, 1);
          lcd.print("KOD VERNYI!     ");

          // Отправка в COM-порт для журнала Java
          Serial.print("EVENT:AUTH_OK;CODE:");
          Serial.print(inputCode);
          Serial.println(";STATUS:GRANTED");
        } else {
          // ОШИБКА: Код неверный
          digitalWrite(PIN_LED_GREEN, LOW);
          digitalWrite(PIN_LED_RED,   HIGH);
          tone(PIN_BUZZER, 800, 800);

          lcd.setCursor(0, 0);
          lcd.print("STATUS: DENIED  ");
          lcd.setCursor(0, 1);
          lcd.print("KOD NE VERNYI!  ");

          // Отправка в COM-порт для журнала Java
          Serial.print("EVENT:AUTH_FAIL;CODE:");
          Serial.print(inputCode);
          Serial.println(";STATUS:DENIED");
        }
      }
    } else if (key == 'C') {
      // Кнопка [C] на пульте — сброс набора
      resetInputScreen();
    }

    IrReceiver.resume(); // Готовность к следующему сигналу
  }

  // 3. Прием команд управления от Java приложения (удаленный доступ)
  if (Serial.available() > 0) {
    String cmd = Serial.readStringUntil('\n');
    cmd.trim();

    if (cmd == "UNLOCK" || cmd == "CMD:UNLOCK") {
      showingResult = true;
      resultShowTime = now;
      digitalWrite(PIN_LED_GREEN, HIGH);
      digitalWrite(PIN_LED_RED,   LOW);
      tone(PIN_BUZZER, 2200, 300);

      lcd.setCursor(0, 0);
      lcd.print("REMOTE UNLOCK   ");
      lcd.setCursor(0, 1);
      lcd.print("DOOR OPENED     ");

      Serial.println("EVENT:REMOTE_UNLOCK;STATUS:GRANTED");
    } else if (cmd.startsWith("SET_CODE:")) {
      String newCode = cmd.substring(9);
      if (newCode.length() == 4) {
        secretCode = newCode;
        Serial.print("ACK:NEW_CODE_SET=");
        Serial.println(secretCode);
      }
    } else if (cmd == "STATUS") {
      Serial.print("SYSTEM:READY;DOOR:LOCKED;CODE_LEN:4\n");
    }
  }
}

// Сброс экрана к начальному вводу
void resetInputScreen() {
  inputCode = "";
  digitalWrite(PIN_LED_GREEN, LOW);
  digitalWrite(PIN_LED_RED,   LOW);
  noTone(PIN_BUZZER);

  lcd.setCursor(0, 0);
  lcd.print("VVEDITE KOD:    ");
  lcd.setCursor(0, 1);
  lcd.print("PIN: [ _ _ _ _ ]");
}

// Преобразование кодов кнопок Wokwi IR Remote
char mapIrCommand(uint32_t cmd) {
  switch (cmd) {
    case 104: return '0';
    case 48:  return '1';
    case 24:  return '2';
    case 122: return '3';
    case 16:  return '4';
    case 56:  return '5';
    case 90:  return '6';
    case 66:  return '7';
    case 74:  return '8';
    case 82:  return '9';
    case 176: return 'C'; // Clear
    default:  return 0;
  }
}
