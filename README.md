# java-hexagonal-architecture-scaffold (AWS SNS/SQS)

Reference scaffold: **hexagonal (ports & adapters) architecture** in Spring Boot, meant to be read as much as run. Dummy domain (order processing) on purpose — the point is the structure, not the business logic. Companion repo: [`java-mvc-architecture-scaffold`](https://github.com/eliangilsierra/java-mvc-architecture-scaffold) (classic layered architecture, for comparison — see [ADR-0001](adr/0001-hexagonal-architecture.md) for when each one earns its cost). The [`java17-baseline`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/java17-baseline) branch holds this same architecture exactly as it was first built; [`java21-upgrade`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/java21-upgrade) is this branch's parent (same design, in-process instead of over SNS/SQS); [`messaging-rabbitmq`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/messaging-rabbitmq) and [`messaging-kafka`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/messaging-kafka) are sibling branches doing the exact same swap with a different broker — worth comparing the three diffs against `java21-upgrade` side by side.

## Why each folder exists

| Package | Responsibility |
|---|---|
| `domain/model` | Plain objects. No JPA, no Jackson, no Spring. |
| `domain/api` | Input ports: the use cases this service exposes — order creation and, separately, order notification. |
| `domain/spi` | Output ports: what the domain needs from the outside world (persist, notify, publish an event) — as interfaces it defines, not depends on. |
| `domain/useCase` | Implements `domain/api`, depends only on `domain/spi`. Unit-tested with zero Spring context — unchanged by the SNS/SQS swap below. |
| `domain/util` | Pure business rules (`PriceCalculator`) — no framework, trivially testable. |
| `application` | DTOs, MapStruct mappers, and a `handler` between the REST controller and `domain/api`. |
| `infraestructure/input/rest` | The REST controller — calls `application.handler`, never `domain.api` directly. |
| `infraestructure/input/messaging/sqs` | A second driving adapter: `@SqsListener` reacts to a message instead of an HTTP request. Same idea as the REST controller, different trigger. |
| `infraestructure/output/jpa` | The persistence adapter (implements `OrderPersistencePort`). |
| `infraestructure/output/notification` | Two adapters for the same port — console and email — selected by Spring profile. See [ADR-0002](adr/0002-swappable-notification-adapter.md). |
| `infraestructure/output/messaging/sns` | The event-publishing adapter (implements `OrderEventPublisherPort`) over SNS, replacing `java21-upgrade`'s in-process one. See [ADR-0007](adr/0007-aws-sns-sqs-event-publisher.md). |
| `infraestructure/configuration` | Wires `domain.useCase` with concrete adapters. The only place the domain is instantiated with real dependencies — **untouched** by this branch's transport swap. |

## Stack

- Java 21 · Spring Boot 3.3.5 · Maven
- AWS SNS (publish) → SQS (consume), via **Spring Cloud AWS** (`spring-cloud-aws-starter-sns`/`-sqs`) — see [ADR-0007](adr/0007-aws-sns-sqs-event-publisher.md)
- **LocalStack** simulates SNS/SQS locally — no AWS account, credentials or cost needed to clone and run this branch
- Spring Data JPA + H2 in-memory for local dev — Testcontainers (real Postgres + LocalStack) for integration tests, see [ADR-0004](adr/0004-testcontainers-for-integration-tests.md)
- `ProblemDetail` (RFC 7807) error responses
- MapStruct · Bean Validation · springdoc-openapi

## Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/orders` | Create an order (calculates total, persists as `CREATED`, publishes to the SNS topic). Notification happens asynchronously, once SQS delivers the fanned-out message. |
| `GET` | `/orders/{id}` | Get an order. Status flips from `CREATED` to `NOTIFIED` once `OrderCreatedSqsListener` processes the message. |

OpenAPI UI: `/swagger-ui.html`. H2 console: `/h2-console` (JDBC URL `jdbc:h2:mem:orders`).

## Run locally

Requires LocalStack running — unlike `java21-upgrade`, this branch needs Docker for the app to actually publish/consume anything. The init script (`localstack-init/init-aws.sh`) creates the topic, queue and subscription automatically:

```bash
docker compose up -d   # starts LocalStack and provisions SNS/SQS
mvn spring-boot:run
```

Or build the whole app into Docker too:

```bash
docker build -t hexagonal-architecture-scaffold-aws .
docker run -p 8080:8080 --network host hexagonal-architecture-scaffold-aws
```

## Swapping the notification adapter

```bash
NOTIFICATION_ADAPTER=email mvn spring-boot:run
```

Nothing in `domain` or `application` changes — only which `@Profile`-annotated adapter gets wired. This is orthogonal to the SNS/SQS swap above: one axis is "how does the customer get notified," the other is "how does the event that triggers notification travel."

## Tests

```bash
mvn test
```

- `OrderUseCaseTest`, `OrderNotificationUseCaseTest`, `PriceCalculatorTest` — domain logic with mocked ports, zero Spring context.
- `OrderControllerIntegrationTest` — full HTTP flow against a real Postgres **and** LocalStack (both via Testcontainers, reusing the same `localstack-init/init-aws.sh`), asserting the eventually-consistent `CREATED` → `NOTIFIED` transition with Awaitility. Requires Docker.
