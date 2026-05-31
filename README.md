# OpsFlow

[![Backend CI](https://github.com/ghost-wolves/opsflow/actions/workflows/backend.yml/badge.svg)](https://github.com/ghost-wolves/opsflow/actions/workflows/backend.yml)
[![Frontend CI](https://github.com/ghost-wolves/opsflow/actions/workflows/frontend.yml/badge.svg)](https://github.com/ghost-wolves/opsflow/actions/workflows/frontend.yml)

OpsFlow is a full-stack incident ticket triage and SLA management system for support teams that need structured intake, role-based workflows, SLA tracking, and operational reporting.

## Live Demo

- Application: https://opsflow-two.vercel.app
- Backend health check: https://opsflow-backend-22hn.onrender.com/api/health
- Project board: https://github.com/users/ghost-wolves/projects/1/views/1

## Demo Accounts

| Role | Email | Password |
| --- | --- | --- |
| Requester | requester@opsflow.demo | password123 |
| Analyst | analyst@opsflow.demo | password123 |
| Manager | manager@opsflow.demo | password123 |

## Overview

OpsFlow supports the full incident lifecycle from ticket submission through triage, assignment, status updates, comments, audit history, SLA monitoring, and reporting.

The application includes three role-based experiences:

- Requesters can submit incidents, view their tickets, and add comments.
- Analysts can review queues, claim tickets, update ticket status, and communicate with requesters.
- Managers can monitor operational health through dashboards, all-ticket views, SLA risk indicators, and CSV reporting.

## Features

- Incident ticket creation and tracking
- Role-based authentication and authorization
- Requester, analyst, and manager workflows
- Priority calculation based on impact and urgency
- SLA due-date calculation and SLA risk tracking
- Scheduled SLA breach updates
- Rule-based triage suggestions
- Analyst queue and ticket claiming
- Manager dashboard with operational metrics
- All-tickets manager view
- Ticket comments and internal notes
- Ticket audit trail
- CSV export for reporting
- Automated backend, frontend, and end-to-end tests
- CI workflows with GitHub Actions
- Deployed frontend, backend, and PostgreSQL database

## Tech Stack

### Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven
- JUnit

### Frontend

- React
- TypeScript
- Vite
- Vitest
- React Testing Library
- Playwright

### DevOps and Deployment

- Docker
- GitHub Actions
- Neon PostgreSQL
- Render
- Vercel

## Architecture

OpsFlow uses a full-stack web architecture:

- The React frontend is deployed on Vercel.
- The Spring Boot backend is deployed on Render.
- The PostgreSQL database is hosted on Neon.
- Flyway manages database schema migrations.
- GitHub Actions runs automated CI checks for backend and frontend changes.

The backend exposes REST endpoints for authentication, tickets, assignments, comments, audit events, dashboards, and reports. The frontend communicates with the backend through a configurable API base URL.

## Local Development Setup

### Prerequisites

- Java 21
- Node.js
- Docker Desktop
- Git

### 1. Clone the repository

```bash
git clone https://github.com/ghost-wolves/opsflow.git
cd opsflow
```

### 2. Start PostgreSQL

```bash
docker compose up -d
```

The local database runs on port `5433` with the database name `opsflow`.

### 3. Run the backend

```bash
cd backend

export JAVA_HOME="/c/Program Files/Java/jdk-21"
export PATH="$JAVA_HOME/bin:$PATH"

./mvnw spring-boot:run
```

The backend runs at:

```text
http://localhost:8080
```

### 4. Run the frontend

In a separate terminal:

```bash
cd frontend
npm install
npm run dev
```

The frontend runs at:

```text
http://localhost:5173
```

## Testing

### Backend tests

```bash
cd backend

export JAVA_HOME="/c/Program Files/Java/jdk-21"
export PATH="$JAVA_HOME/bin:$PATH"

./mvnw test
```

### Frontend tests

```bash
cd frontend
npm test
```

### End-to-end tests

```bash
cd frontend
npm run test:e2e
```

## Production Configuration

The backend expects production configuration through environment variables:

```text
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=<production JDBC URL>
SPRING_DATASOURCE_USERNAME=<database username>
SPRING_DATASOURCE_PASSWORD=<database password>
APP_CORS_ALLOWED_ORIGINS=<deployed frontend URL>
```

The frontend expects the deployed backend URL through:

```text
VITE_API_BASE_URL=<deployed backend URL>
```

## Known Limitations and Future Improvements

- Demo authentication is intentionally simple and designed for project demonstration.
- Passwords should be migrated to fully hashed credentials before real-world use.
- The rule-based triage engine could be expanded with machine learning or AI-assisted classification.
- Notification support could be added for SLA warnings, assignment changes, and requester updates.
- File attachments could be added for ticket evidence and screenshots.
- More advanced reporting filters could be added for date ranges, analysts, priorities, and SLA outcomes.
- The UI could be expanded with richer visual charts and saved manager report views.

## Screenshots

Screenshots can be stored in:

```text
docs/screenshots
```

## Repository Structure

```text
opsflow/
├── backend/              # Spring Boot API
├── frontend/             # React + TypeScript frontend
├── docs/                 # Documentation and screenshots
├── planning/             # Project planning artifacts
├── .github/workflows/    # CI workflows
└── docker-compose.yml    # Local PostgreSQL setup
```
