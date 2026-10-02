@echo off
setlocal enabledelayedexpansion

set "ROOT=%~dp0"
cd /d "%ROOT%"

if not exist "bin\com\example\Launcher.class" (
    echo [RUN] Compiled binaries not found. Triggering build...
    call "%ROOT%build.bat"
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Build failed!
        pause
        exit /b %ERRORLEVEL%
    )
)

set "JAVA_CMD=java"
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
)

where %JAVA_CMD% >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java runtime not found!
    echo Please make sure JDK 17 or higher is installed and in PATH or JAVA_HOME.
    pause
    exit /b 1
)

set "SDK_PATH=%ROOT%lib\javafx-sdk\lib"
if not exist "%SDK_PATH%" set "SDK_PATH=%ROOT%..\..\..\javafx-client\lib\javafx-sdk\lib"
if not exist "%SDK_PATH%" set "SDK_PATH=%ROOT%..\..\javafx-client\lib\javafx-sdk\lib"

set "UNIREST_JAR=%ROOT%lib\unirest-java-3.14.5-standalone.jar"
if not exist "%UNIREST_JAR%" set "UNIREST_JAR=%ROOT%..\..\..\javafx-client\lib\unirest-java-3.14.5-standalone.jar"
if not exist "%UNIREST_JAR%" set "UNIREST_JAR=%ROOT%..\..\javafx-client\lib\unirest-java-3.14.5-standalone.jar"

echo [RUN] Launching JavaFX Client (Lab 1)...
"%JAVA_CMD%" --enable-native-access=javafx.graphics --module-path "%SDK_PATH%" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.media -cp "%ROOT%bin;%ROOT%lib\*;%UNIREST_JAR%" com.example.Launcher

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Application exited with code %ERRORLEVEL%.
    pause
)
