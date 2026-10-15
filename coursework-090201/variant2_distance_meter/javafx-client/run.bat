@echo off
setlocal enabledelayedexpansion
set "ROOT=%~dp0"
cd /d "%ROOT%"
if not exist "bin\com\example\Launcher.class" call "%ROOT%build.bat"
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
set "JAVA_CMD=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
echo [RUN] Launching JavaFX client...
"%JAVA_CMD%" --module-path "lib\javafx-sdk\lib;..\..\lib\javafx-sdk\lib;..\..\..\02_LIBS_AND_SDK\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base -Djava.library.path="lib\javafx-sdk\bin;..\..\lib\javafx-sdk\bin;..\..\..\02_LIBS_AND_SDK\javafx-sdk\bin" -cp "%ROOT%bin;%ROOT%lib\*;%ROOT%..\..\lib\*" com.example.Launcher