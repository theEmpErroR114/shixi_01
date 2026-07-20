@echo off
chcp 65001 >nul
echo 正在停止课程习题测验系统...

for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    echo 发现进程 PID: %%a，正在终止...
    taskkill /F /PID %%a 2>nul
)

echo 服务已停止。
pause
