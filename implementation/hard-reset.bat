@echo off
echo Performing a hard reset of this project's containers and volumes...

REM Stop and remove containers, networks, and VOLUMES for this project only
docker-compose down -v

REM Remove any specific volumes that might not have been caught
docker volume rm implementation_db_data implementation_fresh_db 2>nul

REM Start with a clean slate
echo Starting with completely fresh containers and database...
docker-compose up --build
