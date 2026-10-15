@echo off
chcp 65001 >nul
echo ========================================================
echo  Сборка прошивки Arduino Uno (Вариант 1: Термостат)
echo ========================================================
set CURRENT_DIR=%~dp0
set SKETCH_NAME=Project1_Thermostat

if not exist "%CURRENT_DIR%temp_build" mkdir "%CURRENT_DIR%temp_build\%SKETCH_NAME%"
copy "%CURRENT_DIR%*.ino" "%CURRENT_DIR%temp_build\%SKETCH_NAME%\" >nul 2>&1
copy "%CURRENT_DIR%*.h" "%CURRENT_DIR%temp_build\%SKETCH_NAME%\" >nul 2>&1

arduino-cli compile --fqbn arduino:avr:uno --output-dir "%CURRENT_DIR%." "%CURRENT_DIR%temp_build\%SKETCH_NAME%"
if errorlevel 1 (
    echo [ОШИБКА] Сборка прошивки не удалась. Убедитесь, что установлен arduino-cli.
) else (
    copy "%CURRENT_DIR%%SKETCH_NAME%.ino.hex" "%CURRENT_DIR%firmware.hex" >nul 2>&1
    echo [УСПЕХ] Файлы прошивки .hex и .elf успешно собраны!
)
rmdir /s /q "%CURRENT_DIR%temp_build" >nul 2>&1
pause
