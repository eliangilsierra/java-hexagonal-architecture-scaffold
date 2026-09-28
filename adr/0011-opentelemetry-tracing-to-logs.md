# ADR-0011: OpenTelemetry tracing, exported to logs

Status: Accepted

## Context

None of this repo's other branches have any distributed tracing — with a REST call, an async SNS publish and an SQS-triggered consumer all involved in a single logical "create an order" flow, correlating log lines across that flow by eye is already hard with just two hops, and would get worse in a real multi-service deployment.

## Decision

Add `micrometer-tracing-bridge-otel` + `opentelemetry-exporter-logging`, and put `traceId`/`spanId` into every log line's format (`logging.pattern.level` in `application.yml`). Spring Boot auto-instruments the HTTP request and the SQS listener invocation, so a single `traceId` ties together the "create" log lines and the "notify" log lines that happen moments later on a different thread.

## Alternatives considered

- **A real tracing backend (Jaeger, Zipkin, Tempo) via an OTLP exporter** — rejected for this scaffold specifically: it would mean another container in `docker-compose.yml` just to look at spans, for a repo whose job is to demonstrate architecture, not an observability stack. Swapping the logging exporter for an OTLP one pointed at a real collector is a one-dependency, one-property change — deliberately left as the obvious next step rather than built in.

## Consequences

Gains: `grep <traceId>` across logs reconstructs the full create → publish → consume → notify flow for one order, with zero additional infrastructure to run.
Costs: this is genuinely a stand-in for real distributed tracing — no trace visualization, no span timing charts, no cross-service propagation beyond what fits in a log file. Real production tracing needs a real backend.
