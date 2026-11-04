@echo off
setlocal EnableExtensions
title Raccoon Coefficient - Clean Build Only
echo.
echo =====================================================
echo      RACCOON COEFFICIENT ^| CLEAN BUILD ONLY
echo =====================================================
echo.
set "PROJECT=%~dp0Raccoon-Coefficient"
if not exist "%PROJECT%\gradlew.bat" goto missing
where java >nul 2>&1
if errorlevel 1 goto nojava
if defined JAVA_HOME if not exist "%JAVA_HOME%\bin\java.exe" goto badhome
rem Isolate Gradle from other builds and their wrapper caches.
set "GRADLE_USER_HOME=%PROJECT%\.gradle-rc-cache"
cd /d "%PROJECT%" || goto missing
echo [INFO] Building and running tests with isolated Gradle cache...
call ".\gradlew.bat" --no-daemon --console=plain clean build shadowJar
if errorlevel 1 goto failed
echo.
echo [OK] Clean build and tests PASSED.
echo [OK] Plugin JAR: %PROJECT%\build\libs\raccoon-coefficient-1.0.0.jar
echo [OK] Portable client JAR: %PROJECT%\build\libs\raccoon-coefficient-1.0.0-all.jar
echo [INFO] To launch without Gradle, double-click START-RACCOON.bat.
pause
exit /b 0
:missing
echo [ERROR] Missing build files. Extract the entire ZIP first.
goto stop
:nojava
echo [ERROR] Java 17 missing. Download Temurin 17:
echo https://adoptium.net/temurin/releases/?version=17
goto stop
:badhome
echo [ERROR] JAVA_HOME is invalid: %JAVA_HOME%
goto stop
:failed
echo [ERROR] Build failed. Copy the output above for diagnosis.
:stop
pause
exit /b 1
