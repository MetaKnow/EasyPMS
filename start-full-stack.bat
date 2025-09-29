@echo off
setlocal EnableExtensions

REM ===============================
REM MissoftPMS Full-Stack Starter (ASCII only)
REM ===============================

REM Use ASCII messages only to avoid codepage issues
echo =============================================
echo MissoftPMS Full-Stack Starter
echo =============================================
echo.

REM -------- Paths --------
set "ROOT=%~dp0"

REM -------- Env checks (simple WHERE detection) --------
echo Checking environments...
where java >nul 2>&1 || (echo [ERROR] Java not found in PATH & goto :halt)
echo [OK] Java detected

where mvn >nul 2>&1 || (echo [ERROR] Maven not found in PATH & goto :halt)
echo [OK] Maven detected

where node >nul 2>&1 || (echo [ERROR] Node.js not found in PATH & goto :halt)
echo [OK] Node.js detected

where npm >nul 2>&1 || (echo [ERROR] npm not found in PATH & goto :halt)
echo [OK] npm detected

REM -------- Frontend deps --------
echo Checking frontend dependencies...
if exist "%ROOT%frontend\node_modules" goto :deps_ok

echo Installing frontend dependencies ^(npm ci if lockfile exists^)...
pushd "%ROOT%frontend"
if exist package-lock.json (
    call npm ci
) else (
    call npm install
)
if errorlevel 1 (
    echo [ERROR] Frontend dependencies installation failed
    popd
    goto :halt
)
popd

:deps_ok
echo [OK] Frontend dependencies ready

REM -------- Start services --------
echo.
echo Starting services...
echo Backend: http://localhost:8081
echo Frontend: http://localhost:5173
echo.

REM Start backend (new window)
echo Starting backend service window...
start "MissoftPMS-Backend" cmd /k cd /d "%ROOT%backend" ^&^& mvn spring-boot:run

REM Wait a short time for backend initialization
echo Waiting 10 seconds for backend to initialize...
timeout /t 10 /nobreak >nul

REM Start frontend (new window)
echo Starting frontend service window...
start "MissoftPMS-Frontend" cmd /k cd /d "%ROOT%frontend" ^&^& npm run dev

echo.
echo [OK] Both services launched.
echo Press any key to stop both services and exit...
pause >nul

REM -------- Stop services --------
echo.
echo Stopping services...

REM Try friendly kill by image names (may kill other dev java/node processes)
taskkill /im java.exe /f >nul 2>&1
taskkill /im node.exe /f >nul 2>&1

REM Also close the started windows by title (best effort)
taskkill /fi "WINDOWTITLE eq MissoftPMS-Backend*" /f >nul 2>&1
taskkill /fi "WINDOWTITLE eq MissoftPMS-Frontend*" /f >nul 2>&1

echo [OK] Services stopped. Bye.
echo.

endlocal
exit /b 0

:halt
echo.
echo [HINT] Please ensure Java, Maven, Node.js, and npm are installed and in PATH.
echo [HINT] Run this script from a normal Windows filesystem path (not WSL).
echo Press any key to exit...
pause >nul
endlocal
exit /b 1