# java-hexagonal-architecture-scaffold

![CI](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/actions/workflows/ci.yml/badge.svg?branch=main)

Reference scaffold: **hexagonal (ports & adapters) architecture** in Spring Boot, meant to be read as much as run. Dummy domain (order processing) on purpose — the point is the structure, not the business logic. This is the flagship version — the most advanced point this design reaches in the repo. Companion repo: [`java-mvc-architecture-scaffold`](https://github.com/eliangilsierra/java-mvc-architecture-scaffold) (classic layered architecture, for comparison — see [ADR-0001](adr/0001-hexagonal-architecture.md) for when each one earns its cost).

## Other branches, for comparison

This same architecture exists at several earlier points, kept alive on purpose so the evolution stays visible and runnable:

| Branch | What it is |
|---|---|
| [`java17-baseline`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/java17-baseline) | The original scaffold as first built — Java 17, Maven, synchronous in-process notification. Frozen. |
| [`java21-upgrade`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/java21-upgrade) | Java 21, `ProblemDetail` errors, virtual threads, event-driven notification (in-process), Testcontainers. |
| [`messaging-rabbitmq`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/messaging-rabbitmq) | `java21-upgrade` with the event published over RabbitMQ instead of in-process. |
| [`messaging-kafka`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/messaging-kafka) | Same idea, over Kafka. |
| [`messaging-aws-sns-sqs`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/messaging-aws-sns-sqs) | Same idea, over AWS SNS/SQS (via LocalStack), on Maven. |
| [`gradle-migration`](https://github.com/eliangilsierra/java-hexagonal-architecture-scaffold/tree/gradle-migration) | `java21-upgrade`'s exact feature set, rebuilt with Gradle. |

`main` (this branch) combines the Gradle build with the AWS SNS/SQS transport, then adds resilience, tracing, and CI on top — see below.

## Why each folder exists

| Package | Responsibility |
|---|---|
| `domain/model` | Plain objects. No JPA, no Jackson, no Spring. |
| `domain/api` | Input ports: the use cases this service exposes — order creation and, separately, order notification. |
| `domain/spi` | Output ports: what the domain needs from the outside world (persist, notify, publish an event) — as interfaces it defines, not depends on. |
| `domain/useCase` | Implements `domain/api`, depends only on `domain/spi`. Unit-tested with zero Spring context — unchanged by every swap below. |
| `domain/util` | Pure business rules (`PriceCalculator`) — no framework, trivially testable. |
| `application` | DTOs, MapStruct mappers, and a `handler` between the REST controller and `domain/api`. |
| `infraestructure/input/rest` | The REST controller — calls `application.handler`, never `domain.api` directly. |
| `infraestructure/input/messaging/sqs` | A second driving adapter: `@SqsListener` reacts to a message instead of an HTTP request. |
| `infraestructure/output/jpa` | The persistence adapter (implements `OrderPersistencePort`). |
| `infraestructure/output/notification` | Two adapters for the same port — console and email — selected by Spring profile. See [ADR-0002](adr/0002-swappable-notification-adapter.md). |
| `infraestructure/output/messaging/sns` | The event-publishing adapter (implements `OrderEventPublisherPort`) over SNS, wrapped in Resilience4j. See [ADR-0009](adr/0009-aws-sns-sqs-event-publisher.md) and [ADR-0010](adr/0010-resilience4j-for-sns-publishing.md). |
| `infraestructure/configuration` | Wires `domain.useCase` with concrete adapters. The only place the domain is instantiated with real dependencies — untouched by any transport/build-tool swap in this repo. |

## Stack

- Java 21 · Spring Boot 3.3.5 · **Gradle (Kotlin DSL)** — see [ADR-0008](adr/0008-migrate-build-to-gradle.md)
- AWS SNS (publish) → SQS (consume) via Spring Cloud AWS, simulated locally with **LocalStack** — see [ADR-0009](adr/0009-aws-sns-sqs-event-publisher.md)
- **Resilience4j** retry + circuit breaker around SNS publishing — see [ADR-0010](adr/0010-resilience4j-for-sns-publishing.md)
- **OpenTelemetry** tracing (`traceId`/`spanId` correlated across the create → publish → consume → notify flow, exported to logs) — see [ADR-0011](adr/0011-opentelemetry-tracing-to-logs.md)
- **GitHub Actions CI** on every push/PR — see [ADR-0012](adr/0012-github-actions-ci.md)
- Spring Data JPA + H2 in-memory for local dev — Testcontainers (real Postgres + LocalStack) for integration tests, see [ADR-0004](adr/0004-testcontainers-for-integration-tests.md)
- `ProblemDetail` (RFC 7807) error responses
- MapStruct · Bean Validation · springdoc-openapi

## Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/orders` | Create an order (calculates total, persists as `CREATED`, publishes to the SNS topic). Notification happens asynchronously, once SQS delivers the fanned-out message. |
| `GET` | `/orders/{id}` | Get an order. Status flips from `CREATED` to `NOTIFIED` once `OrderCreatedSqsListener` processes the message. |

OpenAPI UI: `/swagger-ui.html`. H2 console: `/h2-console` (JDBC URL `jdbc:h2:mem:orders`). Actuator health: `/actuator/health`.

## Run locally

Requires LocalStack running — the init script (`localstack-init/init-aws.sh`) provisions the topic, queue and subscription automatically:

```bash
docker compose up -d   # starts LocalStack and provisions SNS/SQS
gradle bootRun
```

No local Gradle install? Build the whole app into Docker instead (pinned `gradle:8.10-jdk21` image):

```bash
docker build -t hexagonal-architecture-scaffold .
docker run -p 8080:8080 --network host hexagonal-architecture-scaffold
```

## Swapping the notification adapter

```bash
NOTIFICATION_ADAPTER=email gradle bootRun
```

Nothing in `domain` or `application` changes — only which `@Profile`-annotated adapter gets wired. This is orthogonal to the SNS/SQS transport: one axis is "how does the customer get notified," the other is "how does the event that triggers notification travel."

## Tests

```bash
gradle test
```

The full pyramid:
- `OrderUseCaseTest`, `OrderNotificationUseCaseTest`, `PriceCalculatorTest` — domain logic with mocked ports, zero Spring context.
- `OrderControllerTest` (`@WebMvcTest`) — request validation and response shape, with the domain mocked out.
- `OrderPersistenceAdapterTest` (`@DataJpaTest`) — the persistence adapter against a real (embedded) database.
- `OrderControllerIntegrationTest` — full HTTP flow against a real Postgres **and** LocalStack (both via Testcontainers), asserting the eventually-consistent `CREATED` → `NOTIFIED` transition with Awaitility. Requires Docker.

CI (`.github/workflows/ci.yml`) runs all of it on every push/PR to `main` or `develop`.
