# ADR-0008: Migrate the build from Maven to Gradle

Status: Accepted

## Context

Every other branch of this repo uses Maven. This branch exists purely to demonstrate the other major JVM build tool — it is not trying to teach anything new about the architecture itself.

## Decision

Port `java21-upgrade` to Gradle (Kotlin DSL) with no changes to `src/` at all: same dependencies, same versions, same Spring Boot/dependency-management plugin pairing that Maven's `spring-boot-starter-parent` provided. The Spring Boot Gradle plugin's default `jar` task is disabled (`tasks.named<Jar>("jar") { enabled = false }`) so only the Spring Boot fat jar (`bootJar`) is produced — without this, Gradle's default behavior also builds a dependency-less "plain" jar alongside it, which makes a Dockerfile's `*.jar` copy ambiguous.

## Alternatives considered

- **Gradle Groovy DSL instead of Kotlin DSL** — rejected: Kotlin DSL is what new Spring Boot projects generated via start.spring.io default to today, and it gets IDE autocompletion the Groovy DSL doesn't.
- **Also changing something architectural while migrating the build** — rejected on purpose: mixing "new build tool" with "new design decision" in the same branch would make it impossible to tell, from the diff against `java21-upgrade`, which change caused what.

## Consequences

Gains: a clean, isolated diff against `java21-upgrade` that touches only build files (`pom.xml` → `build.gradle.kts`/`settings.gradle.kts`, `Dockerfile`, `.gitignore`) — nothing under `src/` changed, which is itself the proof that the architecture doesn't care which build tool assembles it.
Costs: no Gradle wrapper (`gradlew`/`gradlew.bat`) is committed in this branch — generating one correctly requires running `gradle wrapper` with an actual Gradle installation, which wasn't available while building this branch. Anyone running this locally needs Gradle installed, or can use the Dockerfile, which builds with a pinned `gradle:8.10-jdk21` image instead.
