package com.eliangilsierra.hexagonalscaffold.infraestructure.input.messaging.rabbitmq;

import com.eliangilsierra.hexagonalscaffold.domain.api.OrderNotificationServicePort;
import com.eliangilsierra.hexagonalscaffold.infraestructure.output.events.OrderCreatedEvent;
import com.eliangilsierra.hexagonalscaffold.infraestructure.output.messaging.rabbitmq.RabbitMqConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * A driving adapter, same role as {@code OrderController} (HTTP) or
 * java21-upgrade's {@code OrderCreatedEventListener} (in-process event): the
 * trigger is now a RabbitMQ message. Runs on the listener container's own
 * thread — no {@code @Async} needed, the broker gives us that for free.
 */
@Component
public class OrderCreatedRabbitListener {

    private final OrderNotificationServicePort orderNotificationServicePort;

    public OrderCreatedRabbitListener(OrderNotificationServicePort orderNotificationServicePort) {
        this.orderNotificationServicePort = orderNotificationServicePort;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    public void onOrderCreated(OrderCreatedEvent event) {
        orderNotificationServicePort.handleOrderCreated(event.orderId());
    }
}
