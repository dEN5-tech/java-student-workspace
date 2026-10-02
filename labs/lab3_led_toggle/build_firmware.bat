@echo off
setlocal
chcp 65001 >nul

set HTTP_PROXY=
set HTTPS_PROXY=
set ALL_PROXY=
set http_proxy=
set https_proxy=
set all_proxy=

echo ========================================================
echo   [BUILD] Compiling ESP32 Firmware (Lab 3) via PlatformIO
echo ========================================================
echo.

set PIO_EXE=%USERPROFILE%\.platformio\penv\Scripts\platformio.exe

if not exist "%PIO_EXE%" (
    echo [ERROR] PlatformIO executable not found at: %PIO_EXE%
    goto error
)

"%PIO_EXE%" run -d "%~dp0." --environment esp32dev

if errorlevel 1 goto error

if not exist "%~dp0build" mkdir "%~dp0build"
copy /y "%~dp0.pio\build\esp32dev\firmware.bin" "%~dp0build\firmware.bin" >nul 2>&1

echo.
echo [SUCCESS] ESP32 firmware compiled successfully!
echo [INFO] Binary path: .pio\build\esp32dev\firmware.bin
goto done

:error
echo.
echo [ERROR] Failed to compile ESP32 project.

:done
echo.
if "%1"=="" pause
