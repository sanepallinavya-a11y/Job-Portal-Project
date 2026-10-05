@echo off
setlocal
set MVN_CMD=mvn
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "%USERPROFILE%\.maven\maven-3.9.15\bin\mvn.cmd" (
        set MVN_CMD="%USERPROFILE%\.maven\maven-3.9.15\bin\mvn.cmd"
    ) else (
        echo [ERROR] Maven not found.
        exit /b 1
    )
)
%MVN_CMD% %*
