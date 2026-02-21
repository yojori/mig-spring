@echo off
echo Building Migration Common...
cd java\mig-common
call mvn install
if %errorlevel% neq 0 (
    echo.
    echo MIGRATION-COMMON BUILD FAILED.
    pause
    exit /b %errorlevel%
)

cd ..\mig-controller
echo Starting Migration Controller...
call mvn spring-boot:run
if %errorlevel% neq 0 (
    echo.
    echo MIGRATION-CONTROLLER FAILED.
    pause
    exit /b %errorlevel%
)
pause
