@echo off
setlocal EnableExtensions
title Raccoon Coefficient - One Click RuneLite
echo.
echo =====================================================
echo     RACCOON COEFFICIENT ^| ONE-CLICK RUNELITE
echo     Number go up, brrr.
echo =====================================================
echo.
set "CLIENT=%~dp0PORTABLE-CLIENT\raccoon-coefficient-1.0.0-all.jar"
if not exist "%CLIENT%" goto missing
where java >nul 2>&1
if errorlevel 1 goto nojava
if defined JAVA_HOME if not exist "%JAVA_HOME%\bin\java.exe" goto badhome
echo [INFO] Java:
java -version
echo.
echo [INFO] Opening RuneLite with Raccoon Coefficient...
echo [INFO] No Gradle installation or build required for this button.
echo [INFO] RuneLite may download its normal game resources when starting.
echo.
java -ea -jar "%CLIENT%" --developer-mode --debug
set "RC_EXIT=%ERRORLEVEL%"
if not "%RC_EXIT%"=="0" goto failed
echo.
echo [OK] RuneLite client closed.
pause
exit /b 0
:missing
echo [ERROR] Portable client JAR is missing.
echo Extract the entire package ZIP before double clicking.
echo Expected: %CLIENT%
goto stop
:nojava
echo [ERROR] Java 17 not found. Download Temurin 17:
echo https://adoptium.net/temurin/releases/?version=17
goto stop
:badhome
echo [ERROR] JAVA_HOME is invalid: %JAVA_HOME%
goto stop
:failed
echo.
echo [ERROR] Portable RuneLite failed with code %RC_EXIT%.
echo Copy the error output above for a targeted fix.
:stop
pause
exit /b 1
