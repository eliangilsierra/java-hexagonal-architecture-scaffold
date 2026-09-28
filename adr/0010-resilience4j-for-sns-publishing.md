# ADR-0010: Resilience4j retry + circuit breaker around SNS publishing

Status: Accepted

## Context

None of this repo's other branches handle broker failures at all — a RabbitMQ/Kafka/SNS outage in those branches means the publish call throws and the HTTP request fails, even though the order itself already saved successfully. That's an honest limitation appropriate for demonstrating the messaging swap in isolation, but `main` is meant to show what "production-shaped" looks like on top of that.

## Decision

Wrap `SnsOrderEventPublisherAdapter.publishOrderCreated` with `@Retry(name = "snsPublisher")` (3 attempts, exponential backoff starting at 200ms) and `@CircuitBreaker(name = "snsPublisher", fallbackMethod = "publishFallback")` (opens after 50% failures in a 10-call sliding window, stays open 15s). The fallback logs the failure — order creation already succeeded and was returned to the caller before this method runs (see ADR-0003), so a publish failure here means "notification will be late," not "the order is lost."

## Alternatives considered

- **Let the exception propagate to the HTTP response** — rejected: `OrderUseCase.createOrder` already returned successfully before the event publisher is even involved in a real failure path (publishing happens synchronously within the same request in the current wiring, but conceptually represents a downstream concern the client shouldn't be blocked by); surfacing a 500 to the client because a fire-and-forget notification failed would be a worse failure mode than a delayed notification.
- **A transactional outbox table instead of a logged fallback** — the textbook-correct answer for "never lose an event," genuinely out of scope for this scaffold: it would require a background poller/publisher process and its own failure handling, which is a lesson in itself, not an incremental addition to this one.

## Consequences

Gains: a sustained SNS outage stops generating cascading retries once the breaker opens, instead of every single order creation independently retrying against a broker that's already down.
Costs: the fallback's logged failure is the scaffold's honest, minimal stand-in for "don't lose the event" — a real production system handling this seriously would need the outbox pattern mentioned above, or an equivalent.
