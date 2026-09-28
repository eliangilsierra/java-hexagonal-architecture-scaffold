# ADR-0007: AWS SNS/SQS implementation of `OrderEventPublisherPort`, via LocalStack

Status: Accepted

## Context

Same starting point as ADR-0005 (RabbitMQ) and ADR-0006 (Kafka): `java21-upgrade` introduced `domain/spi/OrderEventPublisherPort` so it could be swapped for a real transport without touching `domain.useCase.OrderUseCase`. This branch swaps it for AWS SNS (publish) fanning out to SQS (consume) — a third independent sibling of `java21-upgrade`, alongside `messaging-rabbitmq` and `messaging-kafka`.

## Decision

- Publish to an SNS topic (`orders-created-topic`), which fans out to a single SQS queue (`orders-created-queue`) via a subscription — not publishing directly to SQS. This is the standard AWS pattern when more than one consumer might eventually care about the same event; a direct SNS→SQS-only setup would work today but the fan-out shape costs nothing extra to set up now.
- The subscription uses **raw message delivery**: without it, SNS wraps the payload in its own envelope JSON when delivering to SQS, and the consumer would need to unwrap that envelope before parsing the actual event — pure incidental complexity for a single-consumer case like this one.
- `SnsOrderEventPublisherAdapter` (implements `OrderEventPublisherPort`) uses **Spring Cloud AWS**'s `SnsTemplate`, not the raw AWS SDK — consistent with using `RabbitTemplate`/`KafkaTemplate` in the sibling branches rather than their respective raw clients.
- `OrderCreatedSqsListener` (`@SqsListener`, Spring Cloud AWS) replaces the in-process `@EventListener`.
- **LocalStack**, not real AWS: `localstack-init/init-aws.sh` creates the topic, queue and subscription on startup, mounted into `docker-compose.yml` for local dev and reused as-is by the Testcontainers-based integration test (`LocalStackContainer` copies the same script in). Nobody reviewing or running this branch needs an AWS account, credentials, or incurs any cost.

## Alternatives considered

- **Publish directly to SQS, skip SNS** — rejected: it's simpler, but throws away the fan-out capability for no real savings here, and doesn't demonstrate the pattern most real systems actually use AWS messaging for.
- **Real AWS instead of LocalStack** — rejected for this scaffold: it would mean the repo can't be cloned and run without the reviewer's own AWS account and credentials, and could incur cost just from someone trying it out. A real service — as opposed to a portfolio scaffold — would make a different call here.

## Consequences

Gains: same shape as the RabbitMQ and Kafka swaps — only the output adapter and input listener changed; `domain.useCase.OrderUseCase` untouched across all three messaging branches. Comparing the three diffs against `java21-upgrade` side by side is the whole point of this repo's branch structure.
Costs: LocalStack's community edition doesn't perfectly replicate every real AWS behavior (IAM enforcement, some service limits, exact latency characteristics) — it's a very good approximation for demonstrating the integration, not a substitute for testing against real AWS before a real production deployment.
