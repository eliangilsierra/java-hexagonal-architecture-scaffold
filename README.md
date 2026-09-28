# java-hexagonal-architecture-scaffold (Kafka)

Reference scaffold: **hexagonal (ports & adapters) architecture** in Spring Boot, meant to be read as much as run. Dummy domain (order processing) on purpose — the point is the structure, not the business logic. Companion repo: [`java-mvc-architecture-scaffold`](https://github.com/eliangilsierra/java-mvc-architecture-scaffold) (classic layered architecture, for comparison — see [ADR-0001](adr/0001-hexagonal-architecture.md) for when each one earns its cost). The [`java17-baseline`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/java17-baseline) branch holds this same architecture exactly as it was first built; [`java21-upgrade`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/java21-upgrade) is this branch's parent (same design, in-process instead of over Kafka); [`messaging-rabbitmq`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/messaging-rabbitmq) is the sibling branch doing the exact same swap with RabbitMQ instead — worth comparing the two diffs against `java21-upgrade` side by side.

## Why each folder exists

| Package | Responsibility |
|---|---|
| `domain/model` | Plain objects. No JPA, no Jackson, no Spring. |
| `domain/api` | Input ports: the use cases this service exposes — order creation and, separately, order notification. |
| `domain/spi` | Output ports: what the domain needs from the outside world (persist, notify, publish an event) — as interfaces it defines, not depends on. |
| `domain/useCase` | Implements `domain/api`, depends only on `domain/spi`. Unit-tested with zero Spring context — unchanged by the Kafka swap below. |
| `domain/util` | Pure business rules (`PriceCalculator`) — no framework, trivially testable. |
| `application` | DTOs, MapStruct mappers, and a `handler` between the REST controller and `domain/api`. |
| `infraestructure/input/rest` | The REST controller — calls `application.handler`, never `domain.api` directly. |
| `infraestructure/input/messaging/kafka` | A second driving adapter: `@KafkaListener` reacts to a record instead of an HTTP request. Same idea as the REST controller, different trigger. |
| `infraestructure/output/jpa` | The persistence adapter (implements `OrderPersistencePort`). |
| `infraestructure/output/notification` | Two adapters for the same port — console and email — selected by Spring profile. See [ADR-0002](adr/0002-swappable-notification-adapter.md). |
| `infraestructure/output/messaging/kafka` | The event-publishing adapter (implements `OrderEventPublisherPort`) over Kafka, replacing `java21-upgrade`'s in-process one. See [ADR-0006](adr/0006-kafka-event-publisher.md). |
| `infraestructure/configuration` | Wires `domain.useCase` with concrete adapters. The only place the domain is instantiated with real dependencies — **untouched** by this branch's broker swap. |

## Stack

- Java 21 · Spring Boot 3.3.5 · Maven
- Kafka (`spring-kafka`) for the order-created event — see [ADR-0006](adr/0006-kafka-event-publisher.md)
- Spring Data JPA + H2 in-memory for local dev — Testcontainers (real Postgres + real Kafka) for integration tests, see [ADR-0004](adr/0004-testcontainers-for-integration-tests.md)
- `ProblemDetail` (RFC 7807) error responses
- MapStruct · Bean Validation · springdoc-openapi

## Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/orders` | Create an order (calculates total, persists as `CREATED`, publishes to Kafka). Notification happens asynchronously, once the record is consumed. |
| `GET` | `/orders/{id}` | Get an order. Status flips from `CREATED` to `NOTIFIED` once `OrderCreatedKafkaListener` processes the record. |

OpenAPI UI: `/swagger-ui.html`. H2 console: `/h2-console` (JDBC URL `jdbc:h2:mem:orders`).

## Run locally

Requires Kafka running — unlike `java21-upgrade`, this branch needs Docker for the app to actually publish/consume anything:

```bash
docker compose up -d   # starts a single-node Kafka (KRaft mode)
mvn spring-boot:run
```

Or build the whole app into Docker too:

```bash
docker build -t hexagonal-architecture-scaffold-kafka .
docker run -p 8080:8080 --network host hexagonal-architecture-scaffold-kafka
```

## Swapping the notification adapter

```bash
NOTIFICATION_ADAPTER=email mvn spring-boot:run
```

Nothing in `domain` or `application` changes — only which `@Profile`-annotated adapter gets wired. This is orthogonal to the Kafka swap above: one axis is "how does the customer get notified," the other is "how does the event that triggers notification travel."

## Tests

```bash
mvn test
```

- `OrderUseCaseTest`, `OrderNotificationUseCaseTest`, `PriceCalculatorTest` — domain logic with mocked ports, zero Spring context.
- `OrderControllerIntegrationTest` — full HTTP flow against a real Postgres **and** a real Kafka (both via Testcontainers), asserting the eventually-consistent `CREATED` → `NOTIFIED` transition with Awaitility. Requires Docker.
