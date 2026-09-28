# ADR-0004: Testcontainers (real Postgres) for integration tests, H2 stays for local dev

Status: Accepted

## Context

`java17-baseline` runs entirely on H2 (see its ADR-0002) so the app starts with zero external dependencies. That's still the right default for `mvn spring-boot:run`. But H2's SQL dialect isn't identical to a real production database, and "all our tests pass on H2" has burned teams before when the real database behaves differently.

## Decision

Keep H2 as the default runtime datasource. Add one integration test (`OrderControllerIntegrationTest`) that uses Testcontainers to run against a real, disposable Postgres container instead, overriding the datasource via `@DynamicPropertySource`. It exercises the full HTTP flow, including the asynchronous notification introduced in ADR-0003 (asserted with Awaitility rather than a fixed `Thread.sleep`).

## Alternatives considered

- **Switch the whole app to Postgres, drop H2 entirely** — rejected for this scaffold specifically: it would mean `mvn spring-boot:run` needs Docker just to try the code out, which contradicts the "clone and run" goal from ADR-0002. A real service (see the messaging branches, which do add a docker-compose for their broker) would make a different call here.
- **Fixed `Thread.sleep` instead of Awaitility** — rejected: sleeping a fixed duration is either too slow (wastes CI time) or flaky (too short on a loaded machine); polling with a timeout is the standard fix.

## Consequences

Gains: at least one test proves the app behaves correctly against the same kind of database it would run against in production, and proves the eventually-consistent flow actually completes, not just that the code compiles.
Costs: running this one test requires Docker locally or in CI — everything else in this repo does not.
