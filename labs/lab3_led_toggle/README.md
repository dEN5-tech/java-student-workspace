# 💡🔄 Лабораторная работа №3: Включение и выключение светодиода на ESP32

- 📄 **Полная инструкция и теория:** см. [МЕТОДИЧКА.md](МЕТОДИЧКА.md)
- ⚙️ **Сборка прошивки ESP32:** запустите `build_firmware.bat`
- 🌐 **Запуск Wokwi Gateway:** запустите `run_gateway.bat`
- 💻 **Запуск JavaFX Клиента:** запустите `run_client.bat`

### REST Эндпоинты
- `GET /status` — `STATUS:LED=OFF` / `STATUS:LED=ON`
- `GET /led/on` — `STATUS:LED_IS_ON` (включить)
- `GET /led/off` — `STATUS:LED_IS_OFF` (выключить)
- `GET /led/toggle` — `STATUS:LED_IS_ON` / `STATUS:LED_IS_OFF` (переключить)
