#!/bin/bash

echo "Performing a hard reset of all containers and volumes..."

# Stop and remove ALL containers, networks, and VOLUMES
docker-compose down -v

# Extra safety - explicitly prune volumes that might be dangling
docker volume rm implementation_db_data implementation_fresh_db 2>nul

# Start with a clean slate
echo "Starting with completely fresh containers and database..."
docker-compose up --build
