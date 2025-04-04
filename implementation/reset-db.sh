#!/bin/bash

# Stop running containers
docker-compose down

# Remove the database volume
docker volume rm implementation_db_data

# Start containers again
docker-compose up
