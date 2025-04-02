# Project Setup and Run Guide

This guide will walk you through setting up and running the project using Docker and Docker Compose. The project consists of a backend (Java/Spring Boot), a frontend (SvelteKit/Vite), and a PostgreSQL database.

## Prerequisites

Before you begin, ensure you have the following installed on your system:

- **Docker**: [Install Docker](https://docs.docker.com/get-docker/)
- **Docker Compose**: [Install Docker Compose](https://docs.docker.com/compose/install/)

## First-Time Setup

Follow these steps to set up the project for the first time.

Make sure you are in the correct directory (implementation).

```bash
cd implementation
```

### 1. Install frontend dependencies

Navigate to the frontend directory and install the dependencies:

```bash
cd frontend
npm install
```

Go back to the project directory:

```bash
cd ..
```

### 2. Build and Run the Project

Run the following command to build and start the project using Docker Compose:

```bash
docker-compose up --build
```

This command will:

- Build the Docker images for the backend, frontend, and database.
- Start the containers for all services.
- Expose the backend on port **8080**, the frontend on port **5173**, and the PostgreSQL database on port **5432**.

### 3. Access the Application

Once the containers are running, you can access:

- **Backend**: Open [http://localhost:8080](http://localhost:8080) in your browser.
- **Frontend**: Open [http://localhost:5173](http://localhost:5173) in your browser.
- **PostgreSQL**: Connect via a database client using:
  - Host: `localhost`
  - Port: `5432`
  - Database: `PRJ2-Database-09`
  - User: `PRJ2-User-09`
  - Password: `PRJ2-Password-09`

### 4. Resetting the Database

If you need to reset the database (for example, if schema changes aren't being applied):

```bash
# Stop the containers
docker-compose down

# Remove the volume
docker volume rm implementation_db_data

# Start the containers again
docker-compose up
```

This will force the database initialization scripts to run again.

### 5. Starting with a Fresh Database

If you want to start the application with a completely fresh database (removing all existing data), you have several options:

#### Option 1: Use the provided scripts

For Windows:
```bash
fresh-db.bat
```

For Linux/MacOS:
```bash
chmod +x fresh-db.sh
./fresh-db.sh
```

#### Option 2: Manual steps

```bash
# Stop the containers
docker-compose down

# Remove the volume
docker volume rm implementation_db_data

# Start the containers again
docker-compose up
```

This will ensure that your database is completely reset and the initialization scripts run from scratch.

## Running the Project After Initial Setup

If you have already set up the project and want to run it again, follow these steps.

### 1. Start the Containers

Navigate to the project directory and run:

```bash
docker-compose up
```

This command will start the containers without rebuilding them. If you need to rebuild a specific container, use:

```bash
docker-compose up --build backend
```

or

```bash
docker-compose up --build frontend
```

or

```bash
docker-compose up --build postgres
```

### 2. Access the Application

- **Backend**: Open [http://localhost:8080](http://localhost:8080) in your browser.
- **Frontend**: Open [http://localhost:5173](http://localhost:5173) in your browser.

## Stopping the Project

To stop the running containers, press `Ctrl+C` in the terminal where Docker Compose is running. Alternatively, you can stop the containers using:

```bash
docker-compose down
```

This command will stop and remove the containers.

## Troubleshooting

### 1. Backend Not Accessible

Ensure the backend container is running:

```bash
docker-compose logs backend
```

Check for errors in the logs and fix them.

### 2. Frontend Not Accessible

Ensure the frontend container is running:

```bash
docker-compose logs frontend
```

Check for errors in the logs and fix them.

### 3. Database Connection Issues

Ensure the PostgreSQL container is running:

```bash
docker-compose logs postgres
```

If needed, restart the database service:

```bash
docker-compose restart postgres
```

## Project Structure

The project is structured as follows:

```
project/
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── ...
├── frontend/
│   ├── Dockerfile
│   ├── package.json
│   └── ...
├── database/
│   ├── init.sql (Optional: contains initial database setup scripts)
│   └── ...
└── docker-compose.yml
```

- **Backend**: A Java/Spring Boot application.
- **Frontend**: A SvelteKit/Vite application.
- **PostgreSQL**: A PostgreSQL database with initial data migration scripts.
- **docker-compose.yml**: Defines the services (backend, frontend, and database) and their configurations.

## Additional Commands

### View Logs

To view the logs for a specific service, use:

```bash
docker-compose logs <service-name>
```

Replace `<service-name>` with `backend`, `frontend`, or `postgres`.

### Remove Containers and Volumes

To stop and remove all containers, networks, and volumes, use:

```bash
docker-compose down -v
```

## Support

- If you encounter any issues or have questions, please contact chatGPT.
- If that really doesn't work, you can contact me directly (Furkan Öztürk, Furkan.ozturk@student.fontys.nl).
