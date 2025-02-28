# Project Setup and Run Guide

This guide will walk you through setting up and running the project using Docker and Docker Compose. The project consists of a backend (Java/Spring Boot) and a frontend (SvelteKit/Vite).

## Prerequisites
Before you begin, ensure you have the following installed on your system:

- **Docker**: [Install Docker](https://docs.docker.com/get-docker/)
- **Docker Compose**: [Install Docker Compose](https://docs.docker.com/compose/install/)

## First-Time Setup
Follow these steps to set up the project for the first time.

make sure you are in the correct directory (implementation).

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

- Build the Docker images for the backend and frontend.
- Start the containers for both services.
- Expose the backend on port **8080** and the frontend on port **5173**.

### 3. Access the Application
Once the containers are running, you can access:

- **Backend**: Open [http://localhost:8080](http://localhost:8080) in your browser.
- **Frontend**: Open [http://localhost:5173](http://localhost:5173) in your browser.

## Running the Project After Initial Setup
If you have already set up the project and want to run it again, follow these steps.

### 1. Start the Containers
Navigate to the project directory and run:

```bash
docker-compose up
```

This command will start the containers without rebuilding them. If you need to rebuild the containers, use:

```bash
docker-compose up --build
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
└── docker-compose.yml
```

- **Backend**: A Java/Spring Boot application.
- **Frontend**: A SvelteKit/Vite application.
- **docker-compose.yml**: Defines the services (backend and frontend) and their configurations.

## Additional Commands

### View Logs
To view the logs for a specific service, use:

```bash
docker-compose logs <service-name>
```

Replace `<service-name>` with `backend` or `frontend`.

### Remove Containers and Volumes
To stop and remove all containers, networks, and volumes, use:

```bash
docker-compose down -v
```

## Support
- If you encounter any issues or have questions, please contact chatGPT. 
- if that really doesn't work, you can contact me directly(Furkan Öztürk, Furkan.ozturk@student.fontys.nl).

