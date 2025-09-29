@echo off
chcp 65001 > nul
echo =============================================
echo MissoftPMS 开发环境快速启动
echo =============================================
echo.

echo 服务地址:
echo    后端: http://localhost:8081
echo    前端: http://localhost:5173
echo.

echo 启动后端...
start "Backend" cmd /k "cd /d "%~dp0backend" && mvn spring-boot:run"

echo 等待3秒...
timeout /t 3 /nobreak >nul

echo 启动前端...
start "Frontend" cmd /k "cd /d "%~dp0frontend" && npm run dev"

echo.
echo [成功] 启动完成！
echo [提示] 关闭此窗口不会停止服务
echo [提示] 要停止服务请关闭对应的后端和前端窗口
echo.
pause