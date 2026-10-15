@echo off
setlocal enabledelayedexpansion
set "ROOT=%~dp0"
cd /d "%ROOT%"
echo [BUILD] Compiling JavaFX starter project...
set "JAVAC_CMD=javac"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
if not exist "bin\com\example" mkdir "bin\com\example"
"%JAVAC_CMD%" -encoding UTF-8 --module-path "lib\javafx-sdk\lib;..\..\lib\javafx-sdk\lib;..\..\..\02_LIBS_AND_SDK\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml -cp "lib\*;..\..\lib\*;src" -d "bin" src\com\example\*.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    exit /b %ERRORLEVEL%
)
copy /y "%ROOT%src\com\example\*.fxml" "%ROOT%bin\com\example\" >nul
echo [BUILD] Success! Output placed in bin directory.
exit /b 0