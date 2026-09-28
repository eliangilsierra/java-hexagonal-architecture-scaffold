package com.eliangilsierra.hexagonalscaffold.domain.spi;

/**
 * Output port: "broadcast that an order was created" — nothing more specific
 * than that. Today it's implemented in-process (Spring's own event bus); the
 * messaging branches of this repo (rabbitmq / kafka / aws-sns-sqs) each swap
 * this single adapter for a real broker without touching the use case that
 * calls it.
 */
public interface OrderEventPublisherPort {

    void publishOrderCreated(Long orderId);
}
