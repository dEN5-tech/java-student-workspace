@echo off
setlocal enabledelayedexpansion
set "ROOT=%~dp0"
cd /d "%ROOT%"
if not exist "bin\com\example\Launcher.class" call "%ROOT%build.bat"
set "JAVA_CMD=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
echo [RUN] Launching JavaFX client...
"%JAVA_CMD%" --module-path "%ROOT%lib\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base -cp "%ROOT%bin;%ROOT%lib\*" com.example.Launcher
