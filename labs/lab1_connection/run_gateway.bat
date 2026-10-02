@echo off
setlocal
chcp 65001 >nul

set GW_EXE=%~dp0wokwigw.exe
if not exist "%GW_EXE%" set GW_EXE=%~dp0..\..\wokwigw.exe

if not exist "%GW_EXE%" (
    echo [ERROR] wokwigw.exe not found!
    pause
    exit /b 1
)

echo [GATEWAY] Starting Wokwi IoT Gateway forwarding localhost:4000 to ESP32:8080...
"%GW_EXE%" --forward 4000:10.13.37.2:8080
