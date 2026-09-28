# java-hexagonal-architecture-scaffold (RabbitMQ)

Reference scaffold: **hexagonal (ports & adapters) architecture** in Spring Boot, meant to be read as much as run. Dummy domain (order processing) on purpose — the point is the structure, not the business logic. Companion repo: [`java-mvc-architecture-scaffold`](https://github.com/eliangilsierra/java-mvc-architecture-scaffold) (classic layered architecture, for comparison — see [ADR-0001](adr/0001-hexagonal-architecture.md) for when each one earns its cost). The [`java17-baseline`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/java17-baseline) branch holds this same architecture exactly as it was first built; [`java21-upgrade`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/java21-upgrade) is this branch's parent, with the same event-driven design running in-process instead of over RabbitMQ.

## Why each folder exists

| Package | Responsibility |
|---|---|
| `domain/model` | Plain objects. No JPA, no Jackson, no Spring. |
| `domain/api` | Input ports: the use cases this service exposes — order creation and, separately, order notification. |
| `domain/spi` | Output ports: what the domain needs from the outside world (persist, notify, publish an event) — as interfaces it defines, not depends on. |
| `domain/useCase` | Implements `domain/api`, depends only on `domain/spi`. Unit-tested with zero Spring context — unchanged by the RabbitMQ swap below. |
| `domain/util` | Pure business rules (`PriceCalculator`) — no framework, trivially testable. |
| `application` | DTOs, MapStruct mappers, and a `handler` between the REST controller and `domain/api`. |
| `infraestructure/input/rest` | The REST controller — calls `application.handler`, never `domain.api` directly. |
| `infraestructure/input/messaging/rabbitmq` | A second driving adapter: `@RabbitListener` reacts to a message instead of an HTTP request. Same idea as the REST controller, different trigger. |
| `infraestructure/output/jpa` | The persistence adapter (implements `OrderPersistencePort`). |
| `infraestructure/output/notification` | Two adapters for the same port — console and email — selected by Spring profile. See [ADR-0002](adr/0002-swappable-notification-adapter.md). |
| `infraestructure/output/messaging/rabbitmq` | The event-publishing adapter (implements `OrderEventPublisherPort`) over RabbitMQ, replacing `java21-upgrade`'s in-process one. See [ADR-0005](adr/0005-rabbitmq-event-publisher.md). |
| `infraestructure/configuration` | Wires `domain.useCase` with concrete adapters. The only place the domain is instantiated with real dependencies — **untouched** by this branch's broker swap. |

## Stack

- Java 21 · Spring Boot 3.3.5 · Maven
- RabbitMQ (`spring-boot-starter-amqp`) for the order-created event — see [ADR-0005](adr/0005-rabbitmq-event-publisher.md)
- Spring Data JPA + H2 in-memory for local dev — Testcontainers (real Postgres + real RabbitMQ) for integration tests, see [ADR-0004](adr/0004-testcontainers-for-integration-tests.md)
- `ProblemDetail` (RFC 7807) error responses
- MapStruct · Bean Validation · springdoc-openapi

## Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/orders` | Create an order (calculates total, persists as `CREATED`, publishes to RabbitMQ). Notification happens asynchronously, once the message is consumed. |
| `GET` | `/orders/{id}` | Get an order. Status flips from `CREATED` to `NOTIFIED` once `OrderCreatedRabbitListener` processes the message. |

OpenAPI UI: `/swagger-ui.html`. H2 console: `/h2-console` (JDBC URL `jdbc:h2:mem:orders`). RabbitMQ management UI: `http://localhost:15672` (guest/guest).

## Run locally

Requires RabbitMQ running — unlike `java21-upgrade`, this branch needs Docker for the app to actually publish/consume anything:

```bash
docker compose up -d   # starts RabbitMQ
mvn spring-boot:run
```

Or build the whole app into Docker too:

```bash
docker build -t hexagonal-architecture-scaffold-rabbitmq .
docker run -p 8080:8080 --network host hexagonal-architecture-scaffold-rabbitmq
```

## Swapping the notification adapter

```bash
NOTIFICATION_ADAPTER=email mvn spring-boot:run
```

Nothing in `domain` or `application` changes — only which `@Profile`-annotated adapter gets wired. This is orthogonal to the RabbitMQ swap above: one axis is "how does the customer get notified," the other is "how does the event that triggers notification travel."

## Tests

```bash
mvn test
```

- `OrderUseCaseTest`, `OrderNotificationUseCaseTest`, `PriceCalculatorTest` — domain logic with mocked ports, zero Spring context.
- `OrderControllerIntegrationTest` — full HTTP flow against a real Postgres **and** a real RabbitMQ (both via Testcontainers), asserting the eventually-consistent `CREATED` → `NOTIFIED` transition with Awaitility. Requires Docker.
