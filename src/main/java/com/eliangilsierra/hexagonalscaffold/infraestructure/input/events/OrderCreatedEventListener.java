package com.eliangilsierra.hexagonalscaffold.infraestructure.input.events;

import com.eliangilsierra.hexagonalscaffold.domain.api.OrderNotificationServicePort;
import com.eliangilsierra.hexagonalscaffold.infraestructure.output.events.OrderCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * A driving adapter, exactly like {@code OrderController} — the trigger here
 * is an in-process event instead of an HTTP request. {@code @Async} runs it on
 * a virtual thread (see {@code application.yml}'s
 * {@code spring.threads.virtual.enabled}), which is what makes
 * {@code POST /orders} return with status {@code CREATED} rather than
 * blocking until notification finishes — see ADR-0003.
 */
@Component
public class OrderCreatedEventListener {

    private final OrderNotificationServicePort orderNotificationServicePort;

    public OrderCreatedEventListener(OrderNotificationServicePort orderNotificationServicePort) {
        this.orderNotificationServicePort = orderNotificationServicePort;
    }

    @Async
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        orderNotificationServicePort.handleOrderCreated(event.orderId());
    }
}
