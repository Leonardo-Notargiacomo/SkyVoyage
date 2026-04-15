# SkyVoyage — Airline Booking System

Full-stack airline booking platform built by a 4-person team at Fontys University.
Handles real-time flight search, seat booking, customer management, and employee administration with discount and pricing rules.

## What it does

- Search live flights via Amadeus and AviationStack APIs
- Book tickets with passenger details and tariff selection
- Apply time-based discounts to flight prices
- Manage employees and roles (admin vs standard access)
- View booking history and active tickets

## Architecture

```
frontend/          SvelteKit (TypeScript)
backend/
  restapi/         Javalin HTTP layer — 27 REST endpoints
  businesslogic/   Service layer (managers)
  persistence/     PostgreSQL via JDBC
  assembler/       Module wiring, produces runnable JAR
```

Three-tier architecture with strict module boundaries via Java Platform Module System.

## Tech stack

- Backend: Java 21, Javalin, Maven (multi-module)
- Frontend: SvelteKit, TypeScript
- Database: PostgreSQL (10 tables, init scripts included)
- Auth: BCrypt password hashing, role-based access control
- External APIs: Amadeus (flight search), AviationStack (real-time status)
- Infrastructure: Docker Compose (backend + frontend + postgres), GitHub Actions CI

## Running locally

Requirements: Docker

```bash
git clone https://github.com/Leonardo-Notargiacomo/SkyVoyage
cd SkyVoyage/implementation
cp backend/.env.example backend/.env  # add Amadeus and AviationStack API keys
docker compose up
```

- Frontend: http://localhost:5173
- Backend: http://localhost:8080

The compose setup starts PostgreSQL, runs the init SQL, then boots frontend and backend.

## Testing

```bash
cd implementation/backend
mvn verify
```

70 tests across 8 classes covering business logic, persistence, and REST layers (JUnit 5 + Mockito).

## My contributions

Owned the discount and pricing system end-to-end:

- Built `DiscountManager` and `PriceManager` — business logic for time-based discounts and base price calculation
- Implemented `DiscountRepository` for CRUD operations and `DiscountResource` (REST endpoints)
- Integrated discount/price pipeline into both Amadeus and AviationStack clients so returned prices reflect active discounts
- Built the discount management UI page and wired it to the backend
- Wrote DiscountManager tests and validation test cases
