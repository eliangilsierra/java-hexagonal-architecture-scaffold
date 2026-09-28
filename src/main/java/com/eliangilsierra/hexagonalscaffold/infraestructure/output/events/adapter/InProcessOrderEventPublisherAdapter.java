package com.eliangilsierra.hexagonalscaffold.infraestructure.output.events.adapter;

import com.eliangilsierra.hexagonalscaffold.domain.spi.OrderEventPublisherPort;
import com.eliangilsierra.hexagonalscaffold.infraestructure.output.events.OrderCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * The default (and, in this branch, only) implementation of
 * {@link OrderEventPublisherPort}: publishes an in-process Spring
 * {@code ApplicationEvent} instead of talking to a real broker. In the
 * messaging branches of this repo, this class is what gets replaced —
 * {@code domain.useCase.OrderUseCase} that calls the port never changes.
 */
@Component
public class InProcessOrderEventPublisherAdapter implements OrderEventPublisherPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    public InProcessOrderEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publishOrderCreated(Long orderId) {
        applicationEventPublisher.publishEvent(new OrderCreatedEvent(orderId));
    }
}
