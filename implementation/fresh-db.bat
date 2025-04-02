@echo off
echo Starting with a fresh database...

REM Stop any running containers
docker-compose down

REM Remove the database volume if it exists
docker volume rm implementation_db_data 2>nul

REM Start with a fresh database
set FRESH_DB=fresh_db
docker-compose up
