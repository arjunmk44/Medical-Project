@echo off
echo ========================================
echo   Medical ML Platform - Launcher
echo ========================================
cd /d "%~dp0"

if not exist .env (
    copy .env.example .env
    echo Created .env from .env.example
)

echo Starting services with Docker Compose...
docker-compose up -d --build

echo Waiting for services to start...
timeout /t 25 /nobreak >nul

echo.
echo Platform is running!
start http://localhost

echo.
echo Default login:  admin / admin123
echo To stop:        docker-compose down
echo View logs:      docker-compose logs -f
echo.
pause
