#include <Arduino.h>
#include <WiFi.h>
#include <AsyncTCP.h>
#include <ESPAsyncWebServer.h>

// ==========================================
// Практическая работа №1: Проверка подключения и Ping
// ==========================================

// Асинхронный HTTP сервер на порту 8080
AsyncWebServer server(8080);

void setup() {
  Serial.begin(115200);
  Serial.println("\n==========================================");
  Serial.println("  [ЛАБ 1] ESP32: Проверка связи и Ping");
  Serial.println("==========================================");

  // Шаг 1: Подключение к виртуальной Wi-Fi сети Wokwi
  WiFi.mode(WIFI_STA);
  WiFi.begin("Wokwi-GUEST", "", 6);
  Serial.print("[WiFi] Подключение к Wokwi-GUEST");
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(100);
    Serial.print(".");
  }

  Serial.println("\n[WiFi] Успешно подключено!");
  Serial.print("[WiFi] Локальный IP адрес: ");
  Serial.println(WiFi.localIP());

  // Шаг 2: Регистрация REST маршрутов
  
  // Корневой эндпоинт - готовность устройства
  server.on("/", HTTP_GET, [](AsyncWebServerRequest *request) {
    request->send(200, "text/plain", "ESP32_READY");
  });

  // Эндпоинт пинга
  server.on("/ping", HTTP_GET, [](AsyncWebServerRequest *request) {
    request->send(200, "text/plain", "PONG");
  });

  // Эндпоинт статуса устройства и времени непрерывной работы
  server.on("/status", HTTP_GET, [](AsyncWebServerRequest *request) {
    char response[64];
    unsigned long uptimeSec = millis() / 1000;
    snprintf(response, sizeof(response), "STATUS:ONLINE;UPTIME=%lus", uptimeSec);
    request->send(200, "text/plain", response);
  });

  // Шаг 3: Запуск сервера
  server.begin();
  Serial.println("[HTTP] AsyncWebServer запущен на порту 8080!");
}

void loop() {
  // Асинхронный сервер работает в фоне FreeRTOS задач
  delay(100);
}
