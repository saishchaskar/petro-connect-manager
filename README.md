# PetroConnect

PetroConnect is a full-stack petrol-station management application for recording shift-wise sales, reconciling collections, tracking inventory readings, and analysing fuel revenue.

The platform lets a station manager configure dispensing units and nozzles, complete Day/Night Daily Sales Reports (DSRs), carry forward pump readings automatically, generate monthly reports, and view fuel-sales trends.

## Features

- Station registration and manager login with BCrypt password hashing and session-based authentication
- Protected React routes and centralized API error handling
- Configurable dispensing units (DUs), petrol/diesel nozzles, and shift schedules
- Day/Night shift accounting with nozzle opening and closing readings, test samples, and fuel rates
- Live calculation of reading difference, net sale, fuel revenue, DU totals, and product totals
- Automatic carry-forward of closing readings to the next shift's opening readings
- Debounced auto-save, invalid-reading validation, and completed-shift locking
- Petrol and diesel cash reconciliation for online payments, cards, credit, coins, and denomination counts
- Fuel dip and inventory fields for density, temperature, stock/sales, and tanker loads
- Monthly consolidated sales reports with fuel-wise volume, rate, amount, and monthly totals
- Interactive date-range analytics for fuel volume, revenue, fuel mix, and performance trends
- Downloadable PDF monthly sales reports
- PostgreSQL JSONB persistence for flexible station configurations and DSR payloads
- Flyway database migrations, request/response logging, log rotation, and Docker support

## Tech Stack

| Area | Technologies |
| --- | --- |
| Frontend | React 18, TypeScript, Vite, Ant Design, React Router, Axios, Recharts |
| Backend | Java 21, Spring Boot 3, Spring Security, Spring Data JPA, Hibernate |
| Database | PostgreSQL, JSONB, Flyway |
| Reporting | jsPDF, jsPDF AutoTable |
| Deployment | Docker, Nginx |

## Project Structure

```text
petrol-station-connect/
├── backend/                 # Spring Boot REST API (this repository)
│   ├── src/main/java/
│   │   └── com/deccan/petroconnect/
│   │       ├── controllers/ # Auth, station configuration, DSR APIs
│   │       ├── services/    # Business rules and reporting logic
│   │       ├── repositories/
│   │       └── entities/
│   └── src/main/resources/
│       └── db/migration/    # Flyway migration scripts
└── frontend/                # React application (companion repository)
    └── src/
        ├── pages/           # Login, DSR, reports, analytics, configuration
        ├── components/
        └── services/        # API clients
```

## Prerequisites

- Java 21+
- Maven 3.9+ (or the included Maven wrapper)
- PostgreSQL 14+
- Node.js 20+ and npm (to run the frontend)

## Local Setup

### 1. Create the database

Create a PostgreSQL database:

```sql
CREATE DATABASE petroconnectdb;
```

The default backend configuration uses these local credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/petroconnectdb
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Update `src/main/resources/application.properties` if your local PostgreSQL credentials differ. Flyway applies the required schema migrations when the backend starts.

### 2. Run the backend

From this directory:

```bash
./mvnw spring-boot:run
```

The API starts at `http://localhost:8080`.

### 3. Run the frontend

In the companion frontend repository:

```bash
npm install
npm run start
```

Open `http://localhost:5173` in a browser.

## API Overview

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Register a station and manager account |
| `POST` | `/api/auth/login` | Authenticate a manager |
| `GET` | `/api/station/config?username={username}` | Fetch station configuration |
| `PUT` | `/api/station/config?username={username}` | Save station configuration |
| `GET` | `/api/dsr?date={date}&shift={shift}` | Fetch a DSR shift |
| `POST` | `/api/dsr` | Create or update a DSR shift |
| `GET` | `/api/dsr/exists?date={date}&shift={shift}` | Check whether a shift exists |
| `GET` | `/api/dsr/consolidated?month={month}&year={year}` | Get the monthly consolidated report |
| `GET` | `/api/dsr/analytics?startDate={date}&endDate={date}` | Get date-range sales analytics |

## Docker

Build and run the backend image:

```bash
docker build -t petroconnect-backend .
docker run --rm -p 8080:8080 petroconnect-backend
```

The frontend has its own multi-stage Dockerfile that builds the Vite application and serves it with Nginx.

## Data Model Notes

- A station is associated with one manager account.
- Station setup is stored as JSONB so the number and types of nozzles can vary by station.
- Each DSR is unique per date and shift, and its detailed accounting payload is stored as JSONB.
- Analytics aggregate nozzle-level JSONB data into petrol and diesel volume and revenue totals.

## Future Enhancements

- Role-based access control for managers, cashiers, and administrators
- Persistent/distributed session storage for multi-instance deployments
- Automated tests for shift calculations and reporting queries
- CI/CD pipeline and environment-based configuration

