@echo off
setlocal EnableExtensions
title Raccoon Coefficient - Clean Rebuild And Start
echo.
echo =====================================================
echo    RACCOON COEFFICIENT ^| CLEAN BUILD + RUNELITE
echo =====================================================
echo.
set "PROJECT=%~dp0Raccoon-Coefficient"
set "CLIENT=%PROJECT%\build\libs\raccoon-coefficient-1.0.0-all.jar"
if not exist "%PROJECT%\gradlew.bat" goto missing
where java >nul 2>&1
if errorlevel 1 goto nojava
if defined JAVA_HOME if not exist "%JAVA_HOME%\bin\java.exe" goto badhome
rem Use a Raccoon-only Gradle cache so other projects cannot interfere.
set "GRADLE_USER_HOME=%PROJECT%\.gradle-rc-cache"
cd /d "%PROJECT%" || goto missing
echo [1/2] Clean compile, test and prepare portable developer client...
echo [INFO] This build uses an isolated Raccoon Gradle cache.
echo [INFO] The first rebuild requires internet for Gradle dependencies.
call ".\gradlew.bat" --no-daemon --console=plain clean build shadowJar
if errorlevel 1 goto failed
if not exist "%CLIENT%" goto failed
echo.
echo [2/2] Launching the newly rebuilt portable RuneLite client...
java -jar "%CLIENT%" --developer-mode --debug
if errorlevel 1 goto launchfailed
echo.
echo [OK] RuneLite closed normally.
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
echo [ERROR] Clean build failed. Copy the error lines above for diagnosis.
goto stop
:launchfailed
echo [ERROR] Portable RuneLite client failed. Copy the error above.
:stop
pause
exit /b 1
