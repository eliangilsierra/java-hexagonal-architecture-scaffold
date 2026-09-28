# ADR-0006: Kafka implementation of `OrderEventPublisherPort`

Status: Accepted

## Context

Same starting point as the RabbitMQ branch's ADR-0005: `java21-upgrade` introduced `domain/spi/OrderEventPublisherPort` specifically so it could be swapped for a real broker without touching `domain.useCase.OrderUseCase`. This branch swaps it for Kafka instead of RabbitMQ — a deliberately independent sibling of `messaging-rabbitmq`, both forking from `java21-upgrade`, so they can be compared side by side.

## Decision

- `infraestructure/output/messaging/kafka/KafkaConfig` declares one topic (`orders.created`) via a `NewTopic` bean, picked up by Spring Boot's auto-configured `KafkaAdmin`.
- `KafkaOrderEventPublisherAdapter` implements `OrderEventPublisherPort`, publishing via `KafkaTemplate`, keyed by the order id (so all records for the same order land on the same partition, preserving order).
- `OrderCreatedKafkaListener` (`@KafkaListener`) replaces the in-process `@EventListener` as the driving adapter that calls `OrderNotificationServicePort`.
- JSON (de)serialization is configured explicitly with a **default value type** (`spring.json.value.default.type`) and `spring.json.use.type.headers: false`, instead of relying on the type header `JsonSerializer` adds by default. A Kafka topic is a contract that can outlive any single producer's Java class names — leaning on Spring-specific type headers ties every future consumer to this app's package structure, which is exactly the kind of coupling a topic shouldn't have.

## Alternatives considered

- **Rely on default type headers** (the simpler config) — rejected for the reason above; it costs three extra lines to avoid a real coupling problem.
- **Avro/Schema Registry** — a more production-realistic choice for schema evolution, deliberately out of scope here: it would add infrastructure (a registry) this scaffold doesn't otherwise need, for a lesson this repo isn't trying to teach.

## Consequences

Gains: identical shape to the RabbitMQ swap — only the output adapter and input listener changed, `domain.useCase.OrderUseCase` untouched. Comparing this branch's diff against `java21-upgrade` with `messaging-rabbitmq`'s diff against the same base is a direct, concrete illustration of "same port, different broker."
Costs: like the RabbitMQ branch, this one needs a running broker to do anything (`docker compose up`) — plain `mvn spring-boot:run` alone won't publish or consume.
