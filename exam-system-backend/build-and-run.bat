@echo off
chcp 65001 >nul
echo ========================================
echo   课程习题测验系统 - Windows Build ^& Run
echo ========================================
echo.

REM 检查 JAVA_HOME
if "%JAVA_HOME%"=="" (
    echo [ERROR] JAVA_HOME 未设置，请确保已安装 JDK 26+
    echo 下载地址: https://adoptium.net/download/
    pause
    exit /b 1
)

echo [1/4] 停止旧服务...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    taskkill /F /PID %%a 2>nul
)

echo [2/4] 构建项目...
call mvn clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] 构建失败，请检查错误信息
    pause
    exit /b 1
)

echo [3/4] 启动服务...
start "" java -jar target\exam-system-backend-1.0.0.jar

echo [4/4] 等待服务启动...
timeout /t 8 /nobreak >nul

echo.
echo ========================================
echo   启动成功！
echo   浏览器打开: http://localhost:8080/login.html
echo   默认账号: admin / 123456
echo.
echo   关闭此窗口不会停止服务。
echo   如需停止，运行 stop.bat 或:
echo   netstat -ano ^| findstr ":8080"
echo   taskkill /F /PID ^<PID^>
echo ========================================
pause
