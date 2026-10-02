@echo off
setlocal enabledelayedexpansion

set "ROOT=%~dp0"
cd /d "%ROOT%"

echo ========================================================
echo   [BUILD] Compiling JavaFX Client (Lab 4)
echo ========================================================
echo.

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

set "SDK_PATH=%ROOT%lib\javafx-sdk\lib"
if not exist "%SDK_PATH%" set "SDK_PATH=%ROOT%..\..\..\javafx-client\lib\javafx-sdk\lib"
if not exist "%SDK_PATH%" set "SDK_PATH=%ROOT%..\..\javafx-client\lib\javafx-sdk\lib"

set "UNIREST_JAR=%ROOT%lib\unirest-java-3.14.5-standalone.jar"
if not exist "%UNIREST_JAR%" set "UNIREST_JAR=%ROOT%..\..\..\javafx-client\lib\unirest-java-3.14.5-standalone.jar"
if not exist "%UNIREST_JAR%" set "UNIREST_JAR=%ROOT%..\..\javafx-client\lib\unirest-java-3.14.5-standalone.jar"

if not exist "bin" mkdir "bin"
if not exist "bin\com\example" mkdir "bin\com\example"
if not exist "bin\assets" mkdir "bin\assets"

echo [BUILD] Compiling Java source files...
"%JAVAC_CMD%" -encoding UTF-8 -cp "%ROOT%lib\*";"%UNIREST_JAR%" --module-path "%SDK_PATH%" --add-modules javafx.controls,javafx.fxml -d "bin" src\com\example\*.java
set "COMPILE_ERR=%ERRORLEVEL%"

if %COMPILE_ERR% NEQ 0 (
    echo [ERROR] Java compilation failed!
    exit /b %COMPILE_ERR%
)

echo [BUILD] Copying FXML, CSS and assets to bin...
copy /y "%ROOT%src\com\example\*.fxml" "%ROOT%bin\com\example\" >nul 2>&1
copy /y "%ROOT%src\com\example\*.css" "%ROOT%bin\com\example\" >nul 2>&1
if exist "%ROOT%src\assets\*" (
    copy /y "%ROOT%src\assets\*" "%ROOT%bin\assets\" >nul 2>&1
)

echo [BUILD] Success! Binaries created in bin/.
exit /b 0
