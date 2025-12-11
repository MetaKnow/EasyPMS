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

REM Prefer embedded JDK 17 if available (set JAVA_HOME & PATH before checks)
set "EMBEDDED_JAVA=%ROOT%tools\jdk\bin\java.exe"
if exist "%EMBEDDED_JAVA%" (
  echo [OK] Using embedded JDK: %EMBEDDED_JAVA%
) else (
  echo [ERROR] Embedded JDK not found at %EMBEDDED_JAVA%
  goto :halt
)

REM -------- Env checks (minimal, rely on embedded JDK) --------
echo Checking environments...
echo [OK] Java provided by embedded JDK

if exist "%ROOT%backend\target\pms-1.0.0.jar" (
  echo [OK] Backend packaged Jar detected
) else (
  echo [ERROR] Backend packaged Jar not found: "%ROOT%backend\target\pms-1.0.0.jar"
  echo [HINT] Please build on a dev machine: mvn -DskipTests package
  goto :halt
)

where node >nul 2>&1 || (echo [WARN] Node.js not found in PATH & set "RUN_FRONTEND=0")
if not defined RUN_FRONTEND echo [OK] Node.js detected

where npm >nul 2>&1 || (echo [WARN] npm not found in PATH & set "RUN_FRONTEND=0")
if not defined RUN_FRONTEND echo [OK] npm detected

REM Skip system Java version checks; always use embedded JDK

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
REM Read ports from pms-config.json via PowerShell (fallback to defaults)
set "BACKEND_PORT="
set "FRONTEND_PORT="
set "BACKEND_HOST="
set "FRONTEND_HOST="
for /f "usebackq tokens=* delims=" %%P in (`powershell -NoProfile -Command "try{$c=Get-Content '%ROOT%pms-config.json' | ConvertFrom-Json; [Console]::WriteLine($c.backend.port)}catch{[Console]::WriteLine(8081)}"`) do set "BACKEND_PORT=%%P"
for /f "usebackq tokens=* delims=" %%P in (`powershell -NoProfile -Command "try{$c=Get-Content '%ROOT%pms-config.json' | ConvertFrom-Json; [Console]::WriteLine($c.frontend.port)}catch{[Console]::WriteLine(5173)}"`) do set "FRONTEND_PORT=%%P"
for /f "usebackq tokens=* delims=" %%H in (`powershell -NoProfile -Command "try{$c=Get-Content '%ROOT%pms-config.json' | ConvertFrom-Json; [Console]::WriteLine($c.backend.host)}catch{[Console]::WriteLine('localhost')}"`) do set "BACKEND_HOST=%%H"
for /f "usebackq tokens=* delims=" %%H in (`powershell -NoProfile -Command "try{$c=Get-Content '%ROOT%pms-config.json' | ConvertFrom-Json; [Console]::WriteLine($c.frontend.host)}catch{[Console]::WriteLine('localhost')}"`) do set "FRONTEND_HOST=%%H"
echo Backend: http://%BACKEND_HOST%:%BACKEND_PORT%
if not defined RUN_FRONTEND (
  echo Frontend: http://%FRONTEND_HOST%:%FRONTEND_PORT%
) else (
  echo [WARN] Frontend will not start (Node/npm not available)
)
echo.

REM Start backend via Maven profile (run-tools-jdk) and frontend in same window
echo Starting backend (mvn -Prun-tools-jdk exec:exec) and frontend in current window...
where mvn >nul 2>&1 || (echo [ERROR] Maven not found in PATH & goto :halt)
REM 同窗并发启动后端
start "" /b cmd /c "cd /d "%ROOT%backend" && mvn -Prun-tools-jdk exec:exec"
REM 等待后端初始化片刻
timeout /t 5 /nobreak >nul
REM 同窗并发启动前端（如Node/npm可用）
if not defined RUN_FRONTEND (
  start "" /b cmd /c "cd /d "%ROOT%frontend" && npm run dev"
) else (
  echo [WARN] npm/node 不可用，前端启动已跳过
)

REM Wait a short time for backend initialization
echo Backend & Frontend started in the same window. Press any key to stop.

REM Start frontend (new window)
REM Frontend start handled by PowerShell above; skip duplicated start.

echo.
echo [OK] Both services launched.
echo Press any key to stop both services and exit...
pause

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
pause
endlocal
exit /b 1
