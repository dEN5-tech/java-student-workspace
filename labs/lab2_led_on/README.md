# 💡 Лабораторная работа №2: Включение светодиода на ESP32

- 📄 **Полная инструкция и теория:** см. [МЕТОДИЧКА.md](МЕТОДИЧКА.md)
- ⚙️ **Сборка прошивки ESP32:** запустите `build_firmware.bat`
- 🌐 **Запуск Wokwi Gateway:** запустите `run_gateway.bat`
- 💻 **Запуск JavaFX Клиента:** запустите `run_client.bat`

### REST Эндпоинты
- `GET /` — `ESP32_READY`
- `GET /status` — `STATUS:LED=OFF` / `STATUS:LED=ON`
- `GET /led/on` — `STATUS:LED_IS_ON` (зажигает светодиод на пине GPIO 4)
