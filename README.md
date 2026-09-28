# java-hexagonal-architecture-scaffold (Gradle)

Reference scaffold: **hexagonal (ports & adapters) architecture** in Spring Boot, meant to be read as much as run. Dummy domain (order processing) on purpose — the point is the structure, not the business logic. Companion repo: [`java-mvc-architecture-scaffold`](https://github.com/eliangilsierra/java-mvc-architecture-scaffold) (classic layered architecture, for comparison — see [ADR-0001](adr/0001-hexagonal-architecture.md) for when each one earns its cost). The [`java17-baseline`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/java17-baseline) branch holds this same architecture exactly as it was first built; [`java21-upgrade`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/java21-upgrade) is this branch's parent — identical `src/`, Maven instead of Gradle. See [ADR-0008](adr/0008-migrate-build-to-gradle.md).

## Why each folder exists

| Package | Responsibility |
|---|---|
| `domain/model` | Plain objects. No JPA, no Jackson, no Spring. |
| `domain/api` | Input ports: the use cases this service exposes — order creation and, separately, order notification. |
| `domain/spi` | Output ports: what the domain needs from the outside world (persist, notify, publish an event) — as interfaces it defines, not depends on. |
| `domain/useCase` | Implements `domain/api`, depends only on `domain/spi`. Unit-tested with zero Spring context. |
| `domain/util` | Pure business rules (`PriceCalculator`) — no framework, trivially testable. |
| `application` | DTOs, MapStruct mappers, and a `handler` between the REST controller and `domain/api`. |
| `infraestructure/input/rest` | The REST controller — calls `application.handler`, never `domain.api` directly. |
| `infraestructure/input/events` | A second driving adapter: reacts to an in-process event instead of an HTTP request. Same idea as the REST controller, different trigger. |
| `infraestructure/output/jpa` | The persistence adapter (implements `OrderPersistencePort`). |
| `infraestructure/output/notification` | Two adapters for the same port — console and email — selected by Spring profile. See [ADR-0002](adr/0002-swappable-notification-adapter.md). |
| `infraestructure/output/events` | The event-publishing adapter (implements `OrderEventPublisherPort`) — in-process here; see the [`messaging-rabbitmq`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/messaging-rabbitmq), [`messaging-kafka`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/messaging-kafka) and [`messaging-aws-sns-sqs`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/messaging-aws-sns-sqs) branches for real-broker swaps of this same port. |
| `infraestructure/configuration` | Wires `domain.useCase` with concrete adapters. The only place the domain is instantiated with real dependencies. |

## Stack

- Java 21 · Spring Boot 3.3.5 · **Gradle (Kotlin DSL)** — see [ADR-0008](adr/0008-migrate-build-to-gradle.md)
- Spring Data JPA + H2 in-memory for local dev — Testcontainers (real Postgres) for integration tests, see [ADR-0004](adr/0004-testcontainers-for-integration-tests.md)
- Virtual threads (`spring.threads.virtual.enabled`) power the asynchronous notification listener
- `ProblemDetail` (RFC 7807) error responses
- MapStruct · Bean Validation · springdoc-openapi

## Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/orders` | Create an order (calculates total, persists as `CREATED`, publishes an event). Notification happens asynchronously — see [ADR-0003](adr/0003-event-driven-notification.md). |
| `GET` | `/orders/{id}` | Get an order. Status flips from `CREATED` to `NOTIFIED` moments after creation, once the async listener runs. |

OpenAPI UI: `/swagger-ui.html`. H2 console: `/h2-console` (JDBC URL `jdbc:h2:mem:orders`).

## Swapping the notification adapter

```bash
# default
gradle bootRun

# simulated email instead of console log
NOTIFICATION_ADAPTER=email gradle bootRun
```

Nothing in `domain` or `application` changes between the two — only which `@Profile`-annotated adapter gets wired.

## Run locally

Requires a local Gradle install (no wrapper is committed in this branch — see [ADR-0008](adr/0008-migrate-build-to-gradle.md)):

```bash
gradle bootRun
```

Or with Docker (builds with a pinned `gradle:8.10-jdk21` image, no local Gradle needed):

```bash
docker build -t hexagonal-architecture-scaffold-gradle .
docker run -p 8080:8080 -e NOTIFICATION_ADAPTER=console hexagonal-architecture-scaffold-gradle
```

## Tests

```bash
gradle test
```

- `OrderUseCaseTest`, `OrderNotificationUseCaseTest`, `PriceCalculatorTest` — domain logic with mocked ports, zero Spring context.
- `OrderControllerIntegrationTest` — full HTTP flow against a real Postgres (Testcontainers), asserting the eventually-consistent `CREATED` → `NOTIFIED` transition with Awaitility. Requires Docker.
