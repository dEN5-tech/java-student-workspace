@echo off
setlocal enabledelayedexpansion

set "ROOT=%~dp0"
cd /d "%ROOT%"

echo [BUILD] Compiling Thermostat JavaFX project with jSerialComm...

set "JAVAC_CMD=javac"
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
)

where %JAVAC_CMD% >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compiler javac not found!
    echo Please make sure JDK 17 or higher is installed and in PATH or JAVA_HOME.
    exit /b 1
)

if not exist "bin" mkdir "bin"
if not exist "bin\com\example" mkdir "bin\com\example"

echo [BUILD] Compiling Java source files...
"%JAVAC_CMD%" -encoding UTF-8 --module-path "lib\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml -cp "lib\*" -d "bin" src\com\example\*.java
set "COMPILE_ERR=%ERRORLEVEL%"

if %COMPILE_ERR% NEQ 0 (
    echo [ERROR] Java compilation failed!
    exit /b %COMPILE_ERR%
)

echo [BUILD] Copying FXML and resources to bin...
copy /y "%ROOT%src\com\example\*.fxml" "%ROOT%bin\com\example\" >nul

echo [BUILD] Success! Output placed in bin directory.
exit /b 0
