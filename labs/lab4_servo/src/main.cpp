#include <Arduino.h>
#include <WiFi.h>
#include <AsyncTCP.h>
#include <ESPAsyncWebServer.h>
#include <ESP32Servo.h>

// ==========================================
// Практическая работа №4: Управление сервоприводом и LED
// ==========================================

// HTTP сервер на порту 8080
AsyncWebServer server(8080);

// Светодиод (GPIO 4)
const int LED_PIN = 4;
bool isLedOn = false;

void setLed(bool state) {
  isLedOn = state;
  digitalWrite(LED_PIN, state ? HIGH : LOW);
  Serial.printf("[HARDWARE] LED (GPIO %d) -> %s\n", LED_PIN, state ? "HIGH (ВКЛ)" : "LOW (ВЫКЛ)");
}

// Сервопривод (GPIO 18)
const int SERVO_PIN = 18;
Servo myServo;
int currentAngle = 90;

void setServoAngle(int angle) {
  currentAngle = constrain(angle, 0, 180);
  myServo.write(currentAngle);
  Serial.printf("[HARDWARE] Сервопривод (GPIO %d) -> %d°\n", SERVO_PIN, currentAngle);
}

void setup() {
  Serial.begin(115200);
  Serial.println("\n==========================================");
  Serial.println("  [ЛАБ 4] ESP32: Сервопривод (GPIO 18) + LED (GPIO 4)");
  Serial.println("==========================================");

  // Настройка заголовков: закрывать сокеты сразу (защита от зависания браузеров)
  DefaultHeaders::Instance().addHeader("Connection", "close");
  DefaultHeaders::Instance().addHeader("Access-Control-Allow-Origin", "*");

  // Шаг 1: Настройка оборудования
  pinMode(LED_PIN, OUTPUT);
  setLed(false);

  myServo.attach(SERVO_PIN);
  setServoAngle(90);

  // Шаг 2: Подключение к Wi-Fi сети Wokwi
  WiFi.mode(WIFI_STA);
  WiFi.begin("Wokwi-GUEST", "", 6);
  Serial.print("[WiFi] Подключение к Wokwi-GUEST");
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(100);
    Serial.print(".");
  }

  Serial.println("\n[WiFi] Подключено! IP: " + WiFi.localIP().toString());

  // Шаг 3: Регистрация REST API маршрутов
  
  // Корневой статус
  server.on("/", HTTP_GET, [](AsyncWebServerRequest *request) {
    request->send(200, "text/plain", "ESP32_READY");
  });

  // Общий статус всех подсистем
  server.on("/status", HTTP_GET, [](AsyncWebServerRequest *request) {
    char response[64];
    snprintf(response, sizeof(response), "STATUS:LED=%s;SERVO=%d", isLedOn ? "ON" : "OFF", currentAngle);
    request->send(200, "text/plain", response);
  });

  // Управление светодиодом
  server.on("/led/on", HTTP_GET, [](AsyncWebServerRequest *request) {
    setLed(true);
    request->send(200, "text/plain", "STATUS:LED_IS_ON");
  });

  server.on("/led/off", HTTP_GET, [](AsyncWebServerRequest *request) {
    setLed(false);
    request->send(200, "text/plain", "STATUS:LED_IS_OFF");
  });

  // Управление сервоприводом через Query-параметр (/servo?angle=N)
  server.on("/servo", HTTP_GET, [](AsyncWebServerRequest *request) {
    if (request->hasParam("angle")) {
      int angle = request->getParam("angle")->value().toInt();
      setServoAngle(angle);
    }
    char response[32];
    snprintf(response, sizeof(response), "STATUS:SERVO=%d", currentAngle);
    request->send(200, "text/plain", response);
  });

  // Обработчик 404
  server.onNotFound([](AsyncWebServerRequest *request) {
    request->send(404, "text/plain", "NOT_FOUND");
  });

  // Шаг 4: Запуск сервера
  server.begin();
  Serial.println("[HTTP] AsyncWebServer запущен на порту 8080!");
}

void loop() {
  delay(100);
}
