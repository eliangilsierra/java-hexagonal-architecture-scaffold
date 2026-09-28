# ADR-0009: AWS SNS/SQS implementation of `OrderEventPublisherPort`, via LocalStack

Status: Accepted

## Context

`java21-upgrade` (this branch's ancestor via `gradle-migration`) introduced `domain/spi/OrderEventPublisherPort` so it could be swapped for a real transport without touching `domain.useCase.OrderUseCase`. `main` is the flagship: among the three broker options demonstrated across this repo's messaging branches (RabbitMQ, Kafka, AWS SNS/SQS), AWS SNS/SQS is the one chosen to represent the current best version.

## Decision

- Publish to an SNS topic (`orders-created-topic`), which fans out to a single SQS queue (`orders-created-queue`) via a subscription with **raw message delivery** (no SNS envelope wrapped around the JSON payload).
- `SnsOrderEventPublisherAdapter` (implements `OrderEventPublisherPort`) uses Spring Cloud AWS's `SnsTemplate`; `OrderCreatedSqsListener` (`@SqsListener`) is the driving adapter that replaces the in-process `@EventListener`.
- **LocalStack**, not real AWS: `localstack-init/init-aws.sh` provisions the topic, queue and subscription, mounted into `docker-compose.yml` for local dev and reused as-is by the Testcontainers-based integration test. Nobody cloning this branch needs an AWS account, credentials, or incurs any cost.
- Unlike the standalone `messaging-aws-sns-sqs` branch, publishing here is wrapped in Resilience4j (see ADR-0010) — the flagship is expected to demonstrate production-shaped concerns, not just the swap itself.

## Alternatives considered

See ADR-0007 in the `messaging-aws-sns-sqs` branch for the full comparison against Kafka, RabbitMQ, and against publishing directly to SQS without the SNS fan-out — the reasoning is unchanged here.

## Consequences

Gains: identical shape to the sibling messaging branches — only the output adapter and input listener changed; `domain.useCase.OrderUseCase` untouched.
Costs: LocalStack's community edition doesn't perfectly replicate every real AWS behavior — a very good approximation for demonstrating the integration, not a substitute for testing against real AWS before a real deployment.
