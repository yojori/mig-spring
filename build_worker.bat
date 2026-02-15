@echo off
echo Building Migration Worker...
cd java\mig-worker

call mvn clean package -DskipTests

if %errorlevel% neq 0 (
    echo.
    echo BUILD FAILED. Please ensure Maven is installed and in your PATH.
    pause
    exit /b %errorlevel%
)
echo.
echo BUILD SUCCESSFUL!
pause
