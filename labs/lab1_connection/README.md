# 🚀 Лабораторная работа №1: Проверка подключения и Ping

Быстрый старт и структура проекта:

- 📄 **Полная инструкция и теория:** см. [МЕТОДИЧКА.md](МЕТОДИЧКА.md)
- ⚙️ **Сборка прошивки ESP32:** запустите `build_firmware.bat`
- 🌐 **Запуск Wokwi Gateway:** запустите `run_gateway.bat`
- 💻 **Запуск JavaFX Клиента:** запустите `run_client.bat`

### REST Эндпоинты
- `GET /` — `ESP32_READY`
- `GET /ping` — `PONG`
- `GET /status` — `STATUS:ONLINE;UPTIME=...`
