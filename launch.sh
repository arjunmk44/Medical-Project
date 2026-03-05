#!/bin/bash
# Medical ML Platform — Double-click launcher (Unix/macOS)
echo "🏥 Starting Medical ML Platform..."
cd "$(dirname "$0")"

# Copy .env if not present
if [ ! -f .env ]; then
    cp .env.example .env
    echo "📋 Created .env from .env.example — edit credentials if needed."
fi

docker-compose up -d --build
echo "⏳ Waiting for services to start..."
sleep 20

echo "✅ Platform is running!"
echo "🌐 Opening browser at http://localhost"

if command -v xdg-open &>/dev/null; then
    xdg-open http://localhost
elif command -v open &>/dev/null; then
    open http://localhost
fi

echo ""
echo "Default login:  admin / admin123"
echo "📋 To stop:     docker-compose down"
echo "📋 View logs:   docker-compose logs -f"
