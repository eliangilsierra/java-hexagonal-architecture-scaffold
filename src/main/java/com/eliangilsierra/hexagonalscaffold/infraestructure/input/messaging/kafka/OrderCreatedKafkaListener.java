package com.eliangilsierra.hexagonalscaffold.infraestructure.input.messaging.kafka;

import com.eliangilsierra.hexagonalscaffold.domain.api.OrderNotificationServicePort;
import com.eliangilsierra.hexagonalscaffold.infraestructure.output.events.OrderCreatedEvent;
import com.eliangilsierra.hexagonalscaffold.infraestructure.output.messaging.kafka.KafkaConfig;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * A driving adapter, same role as {@code OrderController} (HTTP) or
 * java21-upgrade's in-process {@code OrderCreatedEventListener}: the trigger
 * is now a Kafka record. Runs on the listener container's own consumer
 * thread — no {@code @Async} needed.
 */
@Component
public class OrderCreatedKafkaListener {

    private final OrderNotificationServicePort orderNotificationServicePort;

    public OrderCreatedKafkaListener(OrderNotificationServicePort orderNotificationServicePort) {
        this.orderNotificationServicePort = orderNotificationServicePort;
    }

    @KafkaListener(topics = KafkaConfig.TOPIC)
    public void onOrderCreated(OrderCreatedEvent event) {
        orderNotificationServicePort.handleOrderCreated(event.orderId());
    }
}
