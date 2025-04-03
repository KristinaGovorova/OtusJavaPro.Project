set -e

echo "Останавливаю текущие контейнеры..."
docker compose down

echo "Билд новых образов..."
docker compose build

echo "Запускаю контейнеры..."
docker compose up -d

echo "Деплой завершен!"
