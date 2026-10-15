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

echo [RUN] Launching Arduino JavaFX Controller...
"%JAVA_CMD%" --module-path "%ROOT%lib\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base -cp "%ROOT%bin;%ROOT%lib\*" com.example.Launcher

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Application exited with code %ERRORLEVEL%.
    pause
)
