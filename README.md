# Booking System — Microservices Portfolio Project

A learning-focused pet project: a hotel booking system built from scratch on a microservices architecture. The goal wasn't just "working code" — it was deliberately applying core patterns for inter-service communication, security, and data consistency in a distributed system.

## Architecture

```
┌──────────────┐      REST (sync)      ┌───────────────┐
│    Client    │ ──────────────────────▶ API endpoints  │
└──────────────┘                        └───────────────┘

  User Service (8081)        Hotel Service (8082)
  ┌─────────────┐            ┌─────────────┐
  │  user_db    │            │  hotel_db   │
  └─────────────┘            └─────────────┘
        ▲                          ▲
        │ issues JWT                │ RestClient (sync)
        │                          │
  Booking Service (8083) ──────────┘
  ┌─────────────┐
  │ booking_db  │
  └──────┬──────┘
         │ Kafka: payment-requests (async)
         ▼
  Payment Service (8084)
  ┌─────────────┐
  │ payment_db  │
  └──────┬──────┘
         │ Kafka: payment-results (async)
         ▼
  Booking Service (updates booking status)
         │
         │ Kafka: payment-results (async)
         ▼
  Notification Service (8085, no own DB, stateless)
```

**5 independent services**, each its own Spring Boot project (Maven), each with its own database (**database-per-service**):

| Service | Port | Database (port) | Purpose |
|---|---|---|---|
| User Service | 8081 | `user_db` (5433) | Registration, login, JWT issuing (email, role, userId) |
| Hotel Service | 8082 | `hotel_db` (5434) | Manages hotels and rooms (Hotel → Room) |
| Booking Service | 8083 | `booking_db` (5435) | Creates bookings, owns room availability |
| Payment Service | 8084 | `payment_db` (5436) | Simulated payment processing |
| Notification Service | 8085 | — | Logs notifications (stateless consumer) |

## Key Architectural Decisions

- **Database-per-service** — each service owns its own database, no direct access to another service's tables. For example, room availability lives in `Booking Service`, not `Hotel Service`, because booking is what actually manages occupancy.
- **Synchronous communication (REST)** — Booking → Hotel via `RestClient`, used when an immediate response is required (e.g. verifying a room exists and its price before creating a booking).
- **Asynchronous communication (Kafka, Saga pattern)** — Booking ↔ Payment ↔ Notification communicate via events rather than direct calls, where fault tolerance and time decoupling between services matter:
    - `payment-requests` — Booking Service publishes a payment request
    - `payment-results` — Payment Service publishes the result; both Booking and Notification consume it
    - Implements a **choreography-based Saga**: instead of a distributed transaction, compensating logic is used (if payment fails, the booking status changes to `PAYMENT_FAILED` rather than rolling back a physical transaction across services).
- **Race condition protection** — booking creation uses a `PESSIMISTIC_WRITE` lock at the database level to prevent double-booking the same room for the same dates under concurrent requests.
- **Dual-write problem** — a deliberate design choice: saving the booking to the database and publishing the Kafka event are done separately (Kafka outside the transaction), with an explicit understanding of the risks and trade-offs involved.

## Security

- **JWT** — issued by User Service, carries email, role, and userId; verified by other services via a shared HMAC secret (symmetric, `HS256`).
- Custom `JwtAuthFilter` in every protected service.
- `AuthenticationEntryPoint` — returns proper `401 Unauthorized` instead of a bare `403`.
- `AccessDeniedHandler` — returns proper `403 Forbidden` for authenticated users lacking permissions.
- **Role-based authorization** — some endpoints are restricted to specific roles (e.g. creating a hotel/room).
- **Object-level authorization** — e.g. `GET /bookings/{id}` verifies the booking belongs to the requesting user (protection against enumeration/IDOR).

## DevOps

- **Docker multi-stage build** — a dedicated `Dockerfile` for each of the 5 services.
- **`docker-compose.yml`** — spins up the whole system with one command: 4 PostgreSQL instances (one per service that needs a DB), Kafka (KRaft mode, no ZooKeeper), and all 5 Spring Boot applications.
- Configuration is parameterized via environment variables (`DB_HOST`, `KAFKA_HOST`, `JWT_SECRET`, `HOTEL_SERVICE_HOST`, etc.) using the `${VAR:default}` syntax in `application.yml` — this allows the project to run both locally (with `localhost` and distinct DB ports) and fully inside Docker Compose (with container DNS names).
- ✅ Final end-to-end test passed: `docker compose up --build` brings up the entire system, and the full flow — register → login → create hotel/room → create booking → Kafka Saga (payment-requests → payment-results) → booking confirmed → notification — worked correctly.

## Tech Stack

- **Java / Spring Boot 4.1.0** (Maven)
- **Spring Security** (JWT, custom filters)
- **Spring Data JPA / Hibernate**
- **Spring Kafka**
- **PostgreSQL 16**
- **Apache Kafka 3.9.0** (KRaft mode)
- **Docker / Docker Compose**

## Known Limitations & Technical Debt

Deliberately deferred improvements — understanding what to do next matters more than implementing all of it in a pet project:

1. **`GET /users/{id}` for service-to-service calls** — no dedicated internal endpoint with API-key protection for inter-service calls to User Service yet.
2. **Service Discovery** — service URLs are currently hardcoded/passed via environment variables instead of being resolved dynamically (Eureka/Consul).
3. **RS256 instead of a shared HMAC secret** — all services currently use one symmetric secret to verify JWTs; production systems typically move to asymmetric keys (User Service signs with a private key, others verify with the public key).
4. **Secrets in `docker-compose.yml`** — `JWT_SECRET` is hardcoded in the file for simplicity; in a real project it should come from a `.env` file (not committed to git) or a secrets manager.
5. **`depends_on` without healthchecks** — Docker Compose only checks "container started," not "application ready to accept connections." Full reliability requires `healthcheck`s (especially for the databases and Kafka).
6. **Dead Letter Topic (DLT)** — if a Kafka consumer fails to deserialize/process a message, it can currently get "stuck"/lost instead of being routed to a separate topic for later inspection.
7. **Contract testing, unit/integration tests** — not yet written for these services (though there's prior experience with JUnit5/Mockito/MockMvc from an earlier monolithic project that could be carried over).

## Out of Scope (Deliberately)

These topics were discussed conceptually but not implemented — a separate stage of growth if needed later:

- Kubernetes / Helm / container orchestration (the project stops at `docker-compose`)
- Service Mesh (Istio)
- Centralized logging/monitoring (ELK)
- OPA (Open Policy Agent) — custom role logic inside the JWT is used instead
- API Gateway as a separate component (Kong) — discussed conceptually, not implemented
- CI/CD pipeline for this project
- Reactive Programming (WebFlux) — classic blocking Spring MVC is used instead

## Running the Project

```bash
docker compose up --build
```

Brings up all 5 services, 4 databases, and Kafka on a shared Docker network. The first run may take a few minutes (building the Maven images).

Ports for testing via Postman/curl:
- `POST http://localhost:8081/users/register`, `/users/login`
- `POST http://localhost:8082/hotels`, `/rooms`
- `POST http://localhost:8083/bookings`, `GET /bookings/{id}`