# java-hexagonal-architecture-scaffold

Reference scaffold: **hexagonal (ports & adapters) architecture** in Spring Boot, meant to be read as much as run. Dummy domain (order processing) on purpose — the point is the structure, not the business logic. Companion repo: [`java-mvc-architecture-scaffold`](https://github.com/eliangilsierra/java-mvc-architecture-scaffold) (classic layered architecture, for comparison — see [ADR-0001](adr/0001-hexagonal-architecture.md) for when each one earns its cost).

## Why each folder exists

| Package | Responsibility |
|---|---|
| `domain/model` | Plain objects. No JPA, no Jackson, no Spring. |
| `domain/api` | Input ports: the use cases this service exposes. |
| `domain/spi` | Output ports: what the domain needs from the outside world (persist, notify) — as interfaces it defines, not depends on. |
| `domain/useCase` | Implements `domain/api`, depends only on `domain/spi`. This is the class that's unit-tested with zero Spring context. |
| `domain/util` | Pure business rules (`PriceCalculator`) — no framework, trivially testable. |
| `application` | DTOs, MapStruct mappers, and a `handler` between the REST controller and `domain/api`. |
| `infraestructure/input/rest` | The REST controller — calls `application.handler`, never `domain.api` directly. |
| `infraestructure/output/jpa` | The persistence adapter (implements `OrderPersistencePort`). |
| `infraestructure/output/notification` | **Two adapters for the same port** — console and email — selected by Spring profile. See [ADR-0002](adr/0002-swappable-notification-adapter.md). |
| `infraestructure/configuration` | Wires `domain.useCase.OrderUseCase` with concrete adapters. The only place the domain is instantiated with real dependencies. |

## Stack

- Java 17 · Spring Boot 3.2.4 · Maven
- Spring Data JPA + H2 in-memory (same reasoning as the MVC scaffold: clone and run, no external DB needed)
- MapStruct · Bean Validation · springdoc-openapi

## Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/orders` | Create an order (calculates total, persists, notifies) |
| `GET` | `/orders/{id}` | Get an order |

OpenAPI UI: `/swagger-ui.html`. H2 console: `/h2-console` (JDBC URL `jdbc:h2:mem:orders`).

## Swapping the notification adapter

```bash
# default
mvn spring-boot:run

# simulated email instead of console log
NOTIFICATION_ADAPTER=email mvn spring-boot:run
```

Nothing in `domain` or `application` changes between the two — only which `@Profile`-annotated adapter gets wired.

## Run locally

```bash
mvn spring-boot:run
```

Or with Docker:

```bash
docker build -t hexagonal-architecture-scaffold .
docker run -p 8080:8080 -e NOTIFICATION_ADAPTER=console hexagonal-architecture-scaffold
```

## Tests

```bash
mvn test
```

`OrderUseCaseTest` and `PriceCalculatorTest` exercise the domain with mocked ports and no Spring context at all — the concrete demonstration of what hexagonal buys you.
