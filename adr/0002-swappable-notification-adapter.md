# ADR-0002: Swappable notification adapter (console vs. email)

Status: Accepted

## Context

`NotificationPort` needs at least one real implementation to prove the pattern is more than a diagram. The clearest, most universally understood example of "the domain doesn't care how this happens" is notification: console log during development, email (or SMS, or push) in a real deployment.

## Decision

One port (`domain/spi/NotificationPort`), two adapters (`infraestructure/output/notification/adapter`): `ConsoleNotificationAdapter` (`@Profile("console")`, the default) and `EmailNotificationAdapter` (`@Profile("email")`, simulated — logs what it would send instead of calling a real SMTP server, to keep this scaffold runnable with zero external configuration). Select one with `NOTIFICATION_ADAPTER=console|email`.

## Consequences

Gains: this is the single clearest place in the repo to point at and say "this is what a port buys you" — `domain.useCase.OrderUseCase` never changes no matter which adapter is active.
Costs: `EmailNotificationAdapter` being simulated means it doesn't prove real SMTP integration works — only that the seam is in the right place. Wiring a real `JavaMailSender` there is a same-file change that never touches `domain`.
