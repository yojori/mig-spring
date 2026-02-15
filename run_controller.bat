@echo off
echo Starting Migration Controller...
cd java\mig-controller
call mvn spring-boot:run
if %errorlevel% neq 0 (
    echo.
    echo BUILD FAILED. Please ensure Maven is installed and in your PATH.
    pause
    exit /b %errorlevel%
)
pause
