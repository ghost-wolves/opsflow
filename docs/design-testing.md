# OpsFlow Design and Testing Document

## 1. Purpose

OpsFlow is a full-stack incident ticket triage and SLA management system. It is designed to support the full incident lifecycle: requester ticket submission, analyst triage and assignment, status workflow management, SLA monitoring, comments, audit history, operational dashboards, and CSV reporting.

## 2. System Overview

OpsFlow uses a Java/Spring Boot backend, a React/TypeScript frontend, and a PostgreSQL database.

The application supports three main user roles:

- Requester
- Analyst
- Manager

Each role has access to a focused set of workflows based on their responsibilities.

## 3. Architecture Overview

### Backend

The backend is built with Spring Boot and exposes REST APIs for authentication, tickets, assignment, status changes, comments, audit events, dashboards, triage suggestions, and reporting.

### Frontend

The frontend is built with React, TypeScript, and Vite. It provides role-based navigation and pages for ticket creation, ticket lists, analyst queues, ticket detail views, dashboards, and reports.

### Database

PostgreSQL stores users, tickets, comments, and audit events. Flyway manages database migrations.

### Testing

The project includes backend unit tests, backend integration tests, frontend component/page tests, and Playwright end-to-end workflow tests.

## 4. Architecture Decisions

### 4.1 Full-Stack Web Application Architecture

OpsFlow is implemented as a full-stack web application with a separate backend API and frontend client. This separation keeps business logic, persistence, authentication, and workflow enforcement on the backend while allowing the frontend to focus on user experience and role-specific workflows.

### 4.2 Spring Boot Backend

Spring Boot was selected for the backend because it provides a mature ecosystem for REST APIs, dependency injection, validation, security, persistence, testing, and production readiness. The backend owns the core business rules for ticket creation, priority calculation, SLA deadlines, assignment, workflow transitions, comments, audit history, dashboard summaries, and reporting.

### 4.3 React and TypeScript Frontend

React with TypeScript was selected for the frontend to provide a maintainable component-based UI with type safety. The frontend includes separate pages for requester, analyst, and manager workflows. Role-based navigation keeps users focused on the actions that match their responsibilities.

### 4.4 PostgreSQL Database

PostgreSQL was selected as the relational database because the system has structured data with clear relationships among users, tickets, comments, and audit events. Relational constraints help preserve consistency across ticket ownership, assignment, status history, comments, and reporting.

### 4.5 Flyway Database Migrations

Flyway is used to manage database schema changes. This keeps schema creation and updates versioned, repeatable, and consistent across local development, automated tests, and future deployed environments.

### 4.6 REST API Design

The backend exposes REST-style endpoints for authentication, tickets, assignment, status changes, comments, audit trails, dashboards, triage suggestions, and CSV reporting. REST was selected because it is simple, widely understood, easy to test, and well suited for a browser-based frontend.

### 4.7 Role-Based Access

OpsFlow uses role-based access to separate requester, analyst, and manager capabilities. Requesters can submit and view their own tickets, analysts can claim and work tickets, and managers can view broader operational reporting. This supports both security and workflow clarity.

### 4.8 Server-Side Business Rules

Important business rules are enforced on the backend rather than relying only on frontend checks. This includes priority calculation, SLA calculation, allowed status transitions, assignment behavior, and audit event creation. This design prevents users from bypassing workflow rules through direct API calls.

### 4.9 Automated Testing as a Design Requirement

Testing was treated as part of the system design rather than an afterthought. The project includes unit tests for business rules, integration tests for API behavior and authorization, frontend page tests for UI behavior, and Playwright end-to-end tests for complete user workflows.

## 5. Design Patterns

### 5.1 State Pattern for Ticket Status Workflow

OpsFlow uses a State Pattern approach for ticket status transitions. Ticket statuses such as `NEW`, `TRIAGED`, `ASSIGNED`, `IN_PROGRESS`, `WAITING_ON_USER`, `RESOLVED`, and `CLOSED` have defined transition rules.

This avoids scattering status transition logic across controllers or frontend code. Instead, workflow behavior is centralized so the application can consistently determine whether a requested transition is valid.

Benefits:

- Prevents invalid ticket lifecycle changes
- Keeps workflow rules easier to test
- Makes future statuses easier to add
- Keeps controllers focused on request handling instead of workflow logic

### 5.2 Service Layer Pattern

The backend uses service classes to hold business logic outside of controllers. Controllers handle HTTP requests and responses, while services perform operations such as ticket creation, priority calculation, SLA calculation, assignment, status updates, comments, audit events, dashboard summaries, and reporting.

Benefits:

- Separates API concerns from business rules
- Improves testability
- Keeps controllers smaller and easier to understand
- Allows business logic to be reused by multiple endpoints

### 5.3 Repository Pattern

Spring Data JPA repositories provide the persistence layer for users, tickets, comments, and audit events. Repositories abstract database access behind clear interfaces so the rest of the application does not need to directly manage SQL for common operations.

Benefits:

- Centralizes persistence access
- Reduces repetitive database code
- Supports integration testing against PostgreSQL
- Keeps service classes focused on business behavior

### 5.4 Data Transfer Object Pattern

OpsFlow uses request and response objects to separate API payloads from database entities. This prevents frontend clients from depending directly on internal entity structure and allows the backend to control which fields are accepted or returned.

Benefits:

- Protects internal persistence models
- Makes API contracts clearer
- Reduces accidental exposure of internal fields
- Allows frontend-friendly response shapes such as SLA risk and dashboard summaries

### 5.5 Audit Trail Pattern

Important ticket actions create audit events. This provides a historical record of ticket creation, assignment, status changes, comments, and other workflow activity.

Benefits:

- Improves accountability
- Helps managers and analysts understand ticket history
- Supports troubleshooting
- Provides evidence of workflow activity during demonstrations and testing

### 5.6 Scheduled Job Pattern

OpsFlow includes a scheduled SLA breach updater. This background process checks active tickets and marks overdue tickets as breached when their SLA deadline has passed.

Benefits:

- Keeps SLA state current without requiring manual user action
- Separates time-based system behavior from request-based behavior
- Makes SLA monitoring more reliable for dashboards and reports

## 6. Database Design

OpsFlow uses PostgreSQL as the primary relational database. Database schema changes are managed through Flyway migrations so the schema can be recreated consistently in development, testing, CI, and future deployed environments.

### 6.1 Main Tables

| Table | Purpose |
| --- | --- |
| `app_users` | Stores demo users and role information for requesters, analysts, and managers. |
| `tickets` | Stores incident tickets, including requester, assignee, status, priority, SLA deadline, SLA breach state, affected system, and timestamps. |
| `ticket_comments` | Stores user comments attached to tickets, including whether a comment is internal. |
| `ticket_audit_events` | Stores historical workflow events for ticket creation, assignment, status changes, comments, and other important actions. |

### 6.2 User Data

Users are stored separately from tickets so tickets can reference both the requester who opened the ticket and the analyst assigned to work it.

The user table supports role-based workflows through roles such as:

- `REQUESTER`
- `ANALYST`
- `MANAGER`

### 6.3 Ticket Data

The ticket table is the central table in the system. It stores the main operational record for each incident.

Important ticket fields include:

- Ticket number
- Title
- Description
- Affected system
- Impact
- Urgency
- Priority
- Status
- Requester
- Assigned analyst
- SLA deadline
- SLA breached flag
- Created timestamp
- Updated timestamp
- Status-specific timestamps

Priority and SLA values are calculated by backend business logic when a ticket is created. Status and assignment fields are updated through controlled workflow endpoints.

### 6.4 Comment Data

Ticket comments are stored in a separate table instead of being embedded directly in tickets. This allows each ticket to have many comments over time.

Comments include:

- Parent ticket
- Author
- Body text
- Internal/public flag
- Created timestamp

The internal flag supports analyst-only notes while still allowing normal requester-visible communication.

### 6.5 Audit Event Data

Audit events are stored separately from comments because they represent system history rather than user discussion. Audit events provide a timeline of important ticket activity.

Examples include:

- Ticket created
- Ticket assigned
- Ticket claimed
- Status changed
- Comment added
- SLA breached

This design supports traceability, manager review, and demonstration of the incident lifecycle.

### 6.6 Relationships

The main database relationships are:

- One user can request many tickets.
- One analyst can be assigned many tickets.
- One ticket can have many comments.
- One ticket can have many audit events.
- Each comment has one author.
- Each audit event is associated with one ticket.

### 6.7 Data Integrity

The database design supports data integrity through relational references, controlled migrations, and backend validation. The backend prevents invalid workflow changes, while the database preserves relationships among users, tickets, comments, and audit events.

### 6.8 Reporting Support

The database structure supports manager dashboard summaries and CSV reporting by storing structured fields for status, priority, SLA risk, assignment, requester, affected system, and timestamps. These fields allow reports to be generated without parsing unstructured text.

## 7. Testing Strategy

OpsFlow uses a layered testing strategy that verifies business logic, API behavior, frontend behavior, and complete user workflows.

### 7.1 Backend Unit Tests

Backend unit tests verify isolated business rules without requiring the full application stack.

Covered areas include:

- Priority calculation
- SLA deadline calculation
- Status transition rules
- Triage suggestion logic

These tests help ensure that core workflow rules behave consistently as the application changes.

### 7.2 Backend Integration Tests

Backend integration tests verify API behavior using the Spring Boot test framework.

Covered areas include:

- Ticket creation API
- Role-based API access
- Assignment and claim workflows
- Status update workflows
- Comment creation
- Audit event creation and retrieval

These tests confirm that controllers, services, repositories, security configuration, and database behavior work together correctly.

### 7.3 Frontend Page Tests

Frontend tests verify important page-level behavior with React Testing Library and Vitest.

Covered areas include:

- Login page behavior
- Ticket creation page behavior
- Ticket list page behavior
- Manager dashboard behavior

These tests help confirm that core UI pages render correctly and respond to user interactions.

### 7.4 End-to-End Tests

Playwright end-to-end tests verify full workflows through the browser.

Covered workflows include:

- Requester logs in, creates a ticket, and views it
- Analyst claims a ticket, updates status, and adds an internal comment
- Manager views dashboard data, reviews all tickets, opens ticket detail, and downloads a CSV report

These tests provide confidence that the integrated frontend, backend, and database support the main user journeys.

### 7.5 Continuous Integration

GitHub Actions runs automated checks for the backend and frontend.

The backend CI workflow:

- Starts a PostgreSQL service container
- Sets up Java 21
- Runs Maven tests

The frontend CI workflow:

- Sets up Node.js
- Installs dependencies with `npm ci`
- Runs frontend tests
- Builds the frontend

### 7.6 Manual Verification

Manual testing is also used during development to verify user experience, layout, navigation, and demo readiness. Manual checks are especially useful for workflows involving multiple roles, CSV download behavior, visual SLA indicators, and role-based navigation.

### 7.7 Testing Rationale

The project uses multiple levels of testing because each level catches a different class of problem:

- Unit tests catch business-rule defects quickly.
- Integration tests catch API, database, and authorization defects.
- Frontend tests catch page rendering and interaction defects.
- End-to-end tests catch full workflow defects across the complete stack.
- Manual testing catches usability and presentation issues that automated tests may not detect.

## 8. Deployment Recommendation and Cost

OpsFlow is designed to be deployed as a small full-stack web application using managed cloud services. The recommended deployment approach is to keep the frontend, backend, and database as separate deployable components.

### 8.1 Recommended Deployment Topology

The recommended deployment topology is:

- React frontend hosted as a static web application
- Spring Boot backend hosted as a web service
- PostgreSQL hosted as a managed database
- Environment variables used for runtime configuration
- GitHub Actions used for automated test verification before deployment

This separation keeps the application simple to operate while still matching a realistic production-style architecture.

### 8.2 Frontend Deployment

The React frontend can be deployed to a static hosting platform such as Vercel, Netlify, Render Static Sites, or a similar service.

The frontend deployment should:

- Build the React app with `npm run build`
- Serve the generated static assets
- Configure the backend API base URL as an environment variable
- Use HTTPS
- Restrict production API calls to the deployed backend URL

### 8.3 Backend Deployment

The Spring Boot backend can be deployed to a platform that supports Java web services or Docker containers.

The backend deployment should:

- Run with Java 21
- Connect to the managed PostgreSQL database through environment variables
- Configure allowed frontend origins for CORS
- Use production-safe secrets instead of local development credentials
- Expose REST API endpoints over HTTPS
- Run Flyway migrations during startup or release deployment

### 8.4 Database Deployment

PostgreSQL should be deployed as a managed database for the hosted version of OpsFlow. A managed database reduces operational work and avoids requiring manual server administration.

The database deployment should provide:

- Persistent storage
- Secure connection string
- Database username and password managed as secrets
- Backups or restore options when available
- Network access limited to the backend service when supported

### 8.5 Environment Variables

The deployed backend should use environment variables for configuration values such as:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- Frontend origin allowed for CORS
- Any future authentication or production secrets

The frontend should use an environment variable for the deployed backend API URL.

### 8.6 Cost Recommendation

For a demonstration or portfolio deployment, OpsFlow should use free or low-cost managed services where possible. A reasonable target cost is approximately $0 to $10 per month for a small demo deployment, depending on provider choice and usage.

A practical low-cost setup is:

- Static frontend hosting on a free personal/project tier
- Backend web service on a free or low-cost tier
- Managed PostgreSQL on a free or low-cost tier

For a more production-oriented deployment, the expected cost may increase based on uptime requirements, database persistence, backups, memory, CPU, logging, and traffic.

### 8.7 Cost Considerations

Cost depends on:

- Whether free tiers are available and appropriate
- Whether the backend sleeps when idle
- Database storage limits
- Database backup and restore requirements
- Monthly traffic
- Build minutes
- Log retention
- Custom domain requirements
- Production uptime expectations

Provider pricing changes over time, so final deployment costs should be verified directly with the selected hosting providers before launch.

### 8.8 Deployment Recommendation

For the project demonstration, the recommended approach is:

- Deploy the frontend to a static hosting service
- Deploy the backend as a Java web service or Docker-based service
- Deploy PostgreSQL using a managed database provider
- Store secrets as provider-managed environment variables
- Add the deployed frontend URL to the README after smoke testing
- Keep local Docker Compose support for development

This approach provides a realistic deployment architecture while keeping operational complexity and cost low.

## 9. Known Limitations

To be expanded during final project polish.

## 10. Future Improvements

To be expanded during final project polish.
