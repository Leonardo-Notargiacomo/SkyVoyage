#!/bin/bash

echo "Starting with a fresh database..."

# Stop running containers
docker-compose down

# Remove the database volume if it exists
docker volume rm implementation_db_data 2>/dev/null

# Start with a fresh database
FRESH_DB=fresh_db docker-compose up
