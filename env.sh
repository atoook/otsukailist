#!/bin/bash

# otsukailist 環境管理スクリプト
# Usage: ./env.sh [dev|test|ci] [up|down|logs|status]

set -e

ENV=${1:-dev}
ACTION=${2:-up}
DB_DIR="./db"

if [ ! -d "$DB_DIR" ]; then
    echo "Error: db directory not found. Please run this script from the project root."
    exit 1
fi

case $ENV in
    dev)
        COMPOSE_FILE="docker-compose.dev.yml"
        PORT="3306"
        ;;
    test)
        COMPOSE_FILE="docker-compose.test.yml"
        PORT="3307"
        ;;
    ci)
        COMPOSE_FILE="docker-compose.ci.yml"
        PORT="3306"
        ;;
    *)
        echo "Error: Unknown environment '$ENV'. Use: dev, test, or ci"
        echo "Usage: $0 [dev|test|ci] [up|down|logs|status]"
        exit 1
        ;;
esac

cd $DB_DIR

case $ACTION in
    up)
        echo "Starting $ENV environment..."
        docker compose -f "$COMPOSE_FILE" up -d
        echo "Database is starting on port $PORT"
        
        if [ "$ENV" = "test" ]; then
            echo "Waiting for test database to be ready..."
            sleep 10
        fi
        
        echo "$ENV environment is ready!"
        ;;
    down)
        echo "Stopping $ENV environment..."
        docker compose -f "$COMPOSE_FILE" down
        echo "$ENV environment stopped."
        ;;
    logs)
        echo "Showing $ENV environment logs..."
        docker compose -f "$COMPOSE_FILE" logs -f
        ;;
    status)
        echo "Status of $ENV environment:"
        docker compose -f "$COMPOSE_FILE" ps
        ;;
    *)
        echo "Error: Unknown action '$ACTION'. Use: up, down, logs, or status"
        echo "Usage: $0 [dev|test|ci] [up|down|logs|status]"
        exit 1
        ;;
esac