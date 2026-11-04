@echo off
setlocal EnableExtensions
title Raccoon Coefficient - Clean Build and Run
echo.
echo ===============================================
echo    RACCOON COEFFICIENT ^| CLEAN BUILD + RUN
echo ===============================================
echo.
set "PROJECT=%~dp0Raccoon-Coefficient"
if not exist "%PROJECT%\gradlew.bat" goto missing
where java >nul 2>&1
if errorlevel 1 goto javaerror
cd /d "%PROJECT%" || goto missing
echo [1/2] Compiling the plugin and running all tests...
call ".\gradlew.bat" --no-daemon --console=plain clean build
if errorlevel 1 goto failed
echo.
echo [2/2] Launching RuneLite development client...
call ".\gradlew.bat" --no-daemon --console=plain run
if errorlevel 1 goto failed
echo [OK] Client closed normally.
pause
exit /b 0
:missing
echo [ERROR] Extract the entire ZIP before starting.
goto stop
:javaerror
echo [ERROR] JDK 17 is required. Install Temurin 17.
goto stop
:failed
echo [ERROR] Check the build/launch error above.
:stop
pause
exit /b 1
