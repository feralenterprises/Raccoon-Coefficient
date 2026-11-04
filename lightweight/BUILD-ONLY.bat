@echo off
setlocal EnableExtensions
title Raccoon Coefficient - Clean Build
echo.
echo ===========================================
echo     RACCOON COEFFICIENT ^| CLEAN BUILD
echo ===========================================
echo.
set "PROJECT=%~dp0Raccoon-Coefficient"
if not exist "%PROJECT%\gradlew.bat" goto missing
where java >nul 2>&1
if errorlevel 1 goto javaerror
cd /d "%PROJECT%" || goto missing
call ".\gradlew.bat" --no-daemon --console=plain clean build
if errorlevel 1 goto failed
echo.
echo [OK] Build/tests passed. The plugin binary is:
echo Raccoon-Coefficient\build\libs\raccoon-coefficient-1.0.0.jar
pause
exit /b 0
:missing
echo [ERROR] Extract the entire ZIP before starting.
goto stop
:javaerror
echo [ERROR] JDK 17 is required. Install Temurin 17.
goto stop
:failed
echo [ERROR] Check the build error above.
:stop
pause
exit /b 1
