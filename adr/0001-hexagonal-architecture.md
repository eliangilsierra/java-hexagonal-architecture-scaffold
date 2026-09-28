# ADR-0001: Hexagonal (ports and adapters) architecture

Status: Accepted

## Context

This repo exists to show hexagonal architecture where its cost actually pays off: a service with more than one way in (REST today, potentially events later) and more than one external dependency it shouldn't be coupled to (persistence, notification). Compare with [`java-mvc-architecture-scaffold`](https://github.com/eliangilsierra/java-mvc-architecture-scaffold), where a classic layered architecture is the better trade-off for a simpler CRUD service.

## Decision

- `domain` — framework-free: `model` (plain objects), `api` (input ports), `spi` (output ports), `useCase` (implementations of `api`, depending only on `spi`), `util` (pure logic like `PriceCalculator`).
- `application` — translates between `domain` and the outside world: DTOs, MapStruct mappers, a `handler` that the REST controller calls (never `domain.api` directly from the controller... actually the controller DOES call `application.handler`, which calls `domain.api` — the controller never calls `domain.api` directly).
- `infraestructure` — everything concrete: `input/rest` (the controller), `output/jpa` (the persistence adapter), `output/notification` (two adapters for the same port, see ADR-0002), `configuration` (wires `domain.useCase` with its adapters — the only place the domain package is instantiated with concrete dependencies).

## Consequences

Gains: `domain.useCase.OrderUseCase` is unit-tested with zero Spring context and zero database (see `OrderUseCaseTest`) — mock two small interfaces, done. Swapping the notification mechanism, or the database, never touches business logic.
Costs: for a service this small, it's more files and more indirection than the business logic alone would need — legitimate only because the repo's purpose is to demonstrate the pattern, not to minimize line count.
