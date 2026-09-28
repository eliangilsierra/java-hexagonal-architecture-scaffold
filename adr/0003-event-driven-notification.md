# ADR-0003: Order creation and notification decoupled via an in-process event

Status: Accepted

## Context

`java17-baseline` notified the customer synchronously, inside `OrderUseCase.createOrder`: create → save → notify → mark `NOTIFIED` → save again, all in one call. That's fine for a single-process demo, but it doesn't foreshadow anything about how a real message broker (RabbitMQ, Kafka, SNS/SQS — the point of this repo's other branches) would actually be wired in: with a real broker, whatever consumes "an order was created" might not even be this process.

## Decision

Split the flow into two use cases behind two ports:
- `domain/api/OrderServicePort` (`OrderUseCase`) — creates and persists the order as `CREATED`, then calls the new output port `domain/spi/OrderEventPublisherPort` and returns immediately.
- `domain/api/OrderNotificationServicePort` (`OrderNotificationUseCase`) — reacts to "an order was created," notifies, and flips the order to `NOTIFIED`.

In this branch, the wiring between them is in-process: `infraestructure/output/events/adapter/InProcessOrderEventPublisherAdapter` publishes a Spring `ApplicationEvent`, and `infraestructure/input/events/OrderCreatedEventListener` (`@Async`, running on a virtual thread per `spring.threads.virtual.enabled`) picks it up and calls `OrderNotificationServicePort`. `POST /orders` now returns with the order still `CREATED` — the client sees eventual consistency, not a lie about synchronous completion.

## Alternatives considered

- **Keep it synchronous, add the port anyway** — rejected: it would hide the real behavior change a broker introduces (latency, out-of-order delivery, the caller not knowing when/if notification happened) until the messaging branches, instead of forcing that thinking now.

## Consequences

Gains: `OrderUseCase` and `OrderNotificationUseCase` are independently testable with fake ports; the seam that the messaging branches replace (`InProcessOrderEventPublisherAdapter` + `OrderCreatedEventListener`) is exactly the same shape a `RabbitListener`/`KafkaListener`/SQS poller will have.
Costs: a client polling `GET /orders/{id}` right after `POST /orders` may still see `CREATED` for a brief moment — genuinely eventually consistent, which is more realistic but also more to explain than "it's done when the response comes back."
