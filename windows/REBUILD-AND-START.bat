@echo off
setlocal EnableExtensions
title Raccoon Coefficient - Clean Build And Start
echo.
echo =====================================================
echo   RACCOON COEFFICIENT  ^|  CLEAN BUILD + RUNELITE
echo =====================================================
echo.
set "PROJECT=%~dp0Raccoon-Coefficient"
if not exist "%PROJECT%\gradlew.bat" goto missing
where java >nul 2>&1
if errorlevel 1 goto nojava
if defined JAVA_HOME if not exist "%JAVA_HOME%\bin\java.exe" goto badhome
cd /d "%PROJECT%" || goto missing
echo [1/2] Compiling the whole standalone plugin and running all unit tests...
call ".\gradlew.bat" --no-daemon clean build
if errorlevel 1 goto failed
echo.
echo [OK] Clean build passed. Compiled JAR:
echo %PROJECT%\build\libs\raccoon-coefficient-1.0.0.jar
echo.
echo [2/2] Opening separate RuneLite developer client...
call ".\gradlew.bat" --no-daemon run
if errorlevel 1 goto failed
echo.
echo [OK] RuneLite client closed normally.
pause
exit /b 0
:missing
echo [ERROR] Missing Raccoon-Coefficient\gradlew.bat.
echo Extract the ENTIRE ZIP before double clicking.
goto stop
:nojava
echo [ERROR] Install Temurin JDK 17 and try again:
echo https://adoptium.net/temurin/releases/?version=17
goto stop
:badhome
echo [ERROR] Invalid JAVA_HOME: %JAVA_HOME%
goto stop
:failed
echo [ERROR] Build or launcher failed. Read error output above.
:stop
pause
exit /b 1
