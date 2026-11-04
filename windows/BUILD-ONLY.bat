@echo off
setlocal EnableExtensions
title Raccoon Coefficient - Clean Build
echo.
echo =====================================================
echo   RACCOON COEFFICIENT  ^|  CLEAN BUILD + TESTS
echo =====================================================
echo.
set "PROJECT=%~dp0Raccoon-Coefficient"
if not exist "%PROJECT%\gradlew.bat" goto missing
where java >nul 2>&1
if errorlevel 1 goto nojava
if defined JAVA_HOME if not exist "%JAVA_HOME%\bin\java.exe" goto badhome
cd /d "%PROJECT%" || goto missing
echo [INFO] Building, compiling and running all tests...
call ".\gradlew.bat" --no-daemon clean build
if errorlevel 1 goto failed
echo.
echo [OK] BUILD SUCCESSFUL. Compiled plugin:
echo %PROJECT%\build\libs\raccoon-coefficient-1.0.0.jar
echo [INFO] This JAR is not a standalone game executable.
echo Use START-RACCOON.bat to run the RuneLite developer client.
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
echo [ERROR] Build failed. Read the error output above.
:stop
pause
exit /b 1
