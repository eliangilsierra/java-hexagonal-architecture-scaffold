# ADR-0005: RabbitMQ implementation of `OrderEventPublisherPort`

Status: Accepted

## Context

`java21-upgrade` introduced `domain/spi/OrderEventPublisherPort` with a single, in-process implementation (`InProcessOrderEventPublisherAdapter` + a Spring `@EventListener`) specifically so a real broker could replace it later without touching `domain.useCase.OrderUseCase`. This branch is that replacement.

## Decision

- `infraestructure/output/messaging/rabbitmq/RabbitMqConfig` declares one topic exchange (`orders.exchange`), one queue (`orders.created.queue`) and the binding between them, plus a `Jackson2JsonMessageConverter` so messages travel as JSON, not Java serialization.
- `RabbitMqOrderEventPublisherAdapter` implements `OrderEventPublisherPort` by publishing to that exchange via the auto-configured `RabbitTemplate`.
- `OrderCreatedRabbitListener` (`@RabbitListener`) replaces the in-process `@EventListener` — it's the new driving adapter that calls `OrderNotificationServicePort`.
- `AsyncConfig` and the manual `@Async` annotation from `java21-upgrade` are removed: RabbitMQ's listener container already runs consumption off the HTTP request thread, so the virtual-thread workaround this scaffold needed in-process is no longer necessary here.

## Alternatives considered

- **Keep `@Async` around the RabbitMQ listener "just in case"** — rejected: it would add a layer of indirection with no purpose, since `@RabbitListener` methods already run on the listener container's own thread pool.
- **Direct exchange instead of topic exchange** — a topic exchange was chosen even though only one routing key exists today, so adding a second event type later doesn't require re-architecting the exchange.

## Consequences

Gains: the swap touched exactly the two files ADR-0003 said it would (`domain.useCase.OrderUseCase` is unchanged), plus it deleted code (`AsyncConfig`) instead of only adding it — evidence the abstraction was pulling its weight.
Costs: this branch now requires a running RabbitMQ to do anything useful (`docker compose up`) — `mvn spring-boot:run` alone starts the app, but publishing/consuming won't work without the broker. Earlier branches ran with zero external dependencies; this one doesn't, and that's an honest trade-off of choosing to demonstrate a real broker.
