# ADR-0012: GitHub Actions CI

Status: Accepted

## Context

None of this repo's other branches run tests automatically — every claim about a test passing has, until now, depended on someone actually running `mvn test`/`gradle test` locally.

## Decision

`.github/workflows/ci.yml` runs on every push/PR to `main` or `develop`: checks out the repo, sets up JDK 21, installs Gradle 8.10 via `gradle/actions/setup-gradle` (no wrapper is committed on this branch's build — see ADR-0008 — so Gradle is installed rather than invoking `./gradlew`), and runs `gradle build`, which compiles, runs the unit tests, the `@DataJpaTest`/`@WebMvcTest` slices, and the Testcontainers integration test (GitHub-hosted runners have Docker available out of the box).

## Alternatives considered

- **Commit a Gradle wrapper just to simplify the CI step to `./gradlew build`** — deferred, not rejected: generating one correctly needs a local Gradle install this environment didn't have while building this branch (see ADR-0008). Installing Gradle explicitly in CI is a reasonable stand-in until a wrapper is added.

## Consequences

Gains: every push now gets an automatic, reproducible build+test run, including the real-container integration test — not just a claim in a README.
Costs: without a committed wrapper, the exact Gradle version used to build this project isn't pinned inside the repo itself — it's pinned in this workflow file instead, which is one file to keep in sync rather than the more typical wrapper-enforced consistency.
