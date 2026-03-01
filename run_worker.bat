@echo off
chcp 65001 > nul
set "WORKER_ID=%~1"

echo Building Migration Common...
cd java\mig-common
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo.
    echo MIGRATION-COMMON BUILD FAILED.
    pause
    exit /b %errorlevel%
)

cd ..\mig-worker
echo Building Migration Worker...
call mvn clean compile -DskipTests
if %errorlevel% neq 0 (
    echo.
    echo MIGRATION-WORKER BUILD FAILED.
    pause
    exit /b %errorlevel%
)

if "%WORKER_ID%"=="" (
    echo Starting Migration Worker [Default ID]...
    call mvn clean spring-boot:run -Dfile.encoding=UTF-8
) else (
    echo Starting Migration Worker [ID: %WORKER_ID%]...
    call mvn clean spring-boot:run -Dfile.encoding=UTF-8 -Dspring-boot.run.jvmArguments="-Dworker.id=%WORKER_ID%"
)

if %errorlevel% neq 0 (
    echo.
    echo RUN FAILED.
    pause
    exit /b %errorlevel%
)
pause
