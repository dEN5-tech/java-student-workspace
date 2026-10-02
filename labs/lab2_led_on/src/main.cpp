#include <Arduino.h>
#include <WiFi.h>
#include <AsyncTCP.h>
#include <ESPAsyncWebServer.h>

// ==========================================
// Практическая работа №2: Включение светодиода
// ==========================================

// Сервер на порту 8080
AsyncWebServer server(8080);

// Светодиод подключен к безопасному пину GPIO 4
const int LED_PIN = 4;
bool isLedOn = false;

void setLed(bool state) {
  isLedOn = state;
  digitalWrite(LED_PIN, state ? HIGH : LOW);
  Serial.printf("[HARDWARE] Светодиод (GPIO %d) -> %s\n", LED_PIN, state ? "HIGH (ВКЛ)" : "LOW (ВЫКЛ)");
}

void setup() {
  Serial.begin(115200);
  Serial.println("\n==========================================");
  Serial.println("  [ЛАБ 2] ESP32: Включение светодиода (GPIO 4)");
  Serial.println("==========================================");

  // Настройка заголовков: закрывать сокеты сразу (защита от зависания браузеров)
  DefaultHeaders::Instance().addHeader("Connection", "close");
  DefaultHeaders::Instance().addHeader("Access-Control-Allow-Origin", "*");

  // Шаг 1: Конфигурация пина светодиода
  pinMode(LED_PIN, OUTPUT);
  setLed(false);

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
  
  // Корневой эндпоинт
  server.on("/", HTTP_GET, [](AsyncWebServerRequest *request) {
    request->send(200, "text/plain", "ESP32_READY");
  });

  // Эндпоинт статуса
  server.on("/status", HTTP_GET, [](AsyncWebServerRequest *request) {
    char response[64];
    snprintf(response, sizeof(response), "STATUS:LED=%s", isLedOn ? "ON" : "OFF");
    request->send(200, "text/plain", response);
  });

  // Эндпоинт включения светодиода
  server.on("/led/on", HTTP_GET, [](AsyncWebServerRequest *request) {
    setLed(true);
    request->send(200, "text/plain", "STATUS:LED_IS_ON");
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
