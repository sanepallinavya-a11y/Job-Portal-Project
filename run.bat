@echo off
setlocal
echo ========================================================
echo    Starting JobPortal Spring Boot Web Application
echo ========================================================

REM Find Maven executable
set MVN_CMD=mvn
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "%USERPROFILE%\.maven\maven-3.9.15\bin\mvn.cmd" (
        set MVN_CMD="%USERPROFILE%\.maven\maven-3.9.15\bin\mvn.cmd"
    ) else (
        echo [ERROR] Maven not found in PATH or at %USERPROFILE%\.maven\maven-3.9.15\bin\mvn.cmd
        exit /b 1
    )
)

echo Using Maven: %MVN_CMD%
echo Application will start at: http://localhost:8080
echo H2 Console at:             http://localhost:8080/h2-console
echo.
%MVN_CMD% spring-boot:run
