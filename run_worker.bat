@echo off
set "WORKER_ID=%~1"

cd java\mig-worker

if "%WORKER_ID%"=="" (
    echo Starting Migration Worker [Default ID]...
    call mvn spring-boot:run
) else (
    echo Starting Migration Worker [ID: %WORKER_ID%]...
    call mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dworker.id=%WORKER_ID%"
)

if %errorlevel% neq 0 (
    echo.
    echo BUILD FAILED. Please ensure Maven is installed and in your PATH.
    pause
    exit /b %errorlevel%
)
pause
