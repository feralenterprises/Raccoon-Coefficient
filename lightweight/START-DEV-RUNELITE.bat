@echo off
setlocal EnableExtensions
title Raccoon Coefficient - RuneLite Developer Client
echo.
echo ===================================================
echo     RACCOON COEFFICIENT ^| RUNELITE DEVELOPMENT
echo     Number go up, brrr.
echo ===================================================
echo.
set "PROJECT=%~dp0Raccoon-Coefficient"
if not exist "%PROJECT%\gradlew.bat" goto missing
where java >nul 2>&1
if errorlevel 1 goto javaerror
cd /d "%PROJECT%" || goto missing
echo [INFO] Launching RuneLite via the Gradle development environment.
echo [INFO] RuneLite is NOT bundled in this download.
echo [INFO] Run only ONE launcher at a time.
echo.
call ".\gradlew.bat" --no-daemon --console=plain run
if errorlevel 1 goto failed
echo.
echo [OK] RuneLite client closed normally.
pause
exit /b 0
:missing
echo [ERROR] Missing project folder / Gradle wrapper. Extract All first.
goto stop
:javaerror
echo [ERROR] JDK 17 is required. Install Temurin 17.
goto stop
:failed
echo [ERROR] Gradle or RuneLite failed. Copy the console output.
:stop
pause
exit /b 1
