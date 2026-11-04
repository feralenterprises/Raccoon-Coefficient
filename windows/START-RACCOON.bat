@echo off
setlocal EnableExtensions
title Raccoon Coefficient - Start RuneLite
echo.
echo =====================================================
echo   RACCOON COEFFICIENT  ^|  ONE-CLICK RUNE LITE START
echo   Number go up, brrr.
echo =====================================================
echo.
set "PROJECT=%~dp0Raccoon-Coefficient"
if not exist "%PROJECT%\gradlew.bat" goto missing
where java >nul 2>&1
if errorlevel 1 goto nojava
if defined JAVA_HOME if not exist "%JAVA_HOME%\bin\java.exe" goto badhome
cd /d "%PROJECT%" || goto missing
echo [INFO] Launching a SEPARATE RuneLite developer client.
echo [INFO] The bundled Raccoon Coefficient plugin loads in that client.
echo [INFO] First run needs internet to download Gradle and RuneLite libraries.
echo [INFO] Java detected:
java -version
echo.
call ".\gradlew.bat" --no-daemon run
set "RC_EXIT=%ERRORLEVEL%"
if not "%RC_EXIT%"=="0" goto failed
echo.
echo [OK] RuneLite client closed normally.
pause
exit /b 0
:missing
echo [ERROR] Project folder or gradlew.bat is missing.
echo Extract the ENTIRE ZIP to a normal folder first.
echo Keep START-RACCOON.bat next to the Raccoon-Coefficient folder.
goto stop
:nojava
echo [ERROR] Java was not found.
echo Install Temurin JDK 17, reopen this BAT, and try again:
echo https://adoptium.net/temurin/releases/?version=17
goto stop
:badhome
echo [ERROR] JAVA_HOME points to a folder without bin\java.exe:
echo %JAVA_HOME%
echo Repair JAVA_HOME to a real JDK 17 installation.
goto stop
:failed
echo.
echo [ERROR] Gradle / RuneLite exited with code %RC_EXIT%.
echo Check the errors above. See README-FIRST.txt for quick fixes.
:stop
echo.
pause
exit /b 1
