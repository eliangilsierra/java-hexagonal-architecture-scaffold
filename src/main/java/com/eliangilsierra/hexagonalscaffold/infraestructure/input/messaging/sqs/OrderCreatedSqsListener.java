package com.eliangilsierra.hexagonalscaffold.infraestructure.input.messaging.sqs;

import com.eliangilsierra.hexagonalscaffold.domain.api.OrderNotificationServicePort;
import com.eliangilsierra.hexagonalscaffold.infraestructure.output.events.OrderCreatedEvent;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

/**
 * A driving adapter, same role as {@code OrderController} (HTTP) or
 * java21-upgrade's in-process {@code OrderCreatedEventListener}: the trigger
 * is now an SQS message, delivered by SNS fan-out (raw delivery, no SNS
 * envelope — see {@code localstack-init/init-aws.sh} and ADR-0007).
 */
@Component
public class OrderCreatedSqsListener {

    private final OrderNotificationServicePort orderNotificationServicePort;

    public OrderCreatedSqsListener(OrderNotificationServicePort orderNotificationServicePort) {
        this.orderNotificationServicePort = orderNotificationServicePort;
    }

    @SqsListener("${app.messaging.sqs-queue-name}")
    public void onOrderCreated(OrderCreatedEvent event) {
        orderNotificationServicePort.handleOrderCreated(event.orderId());
    }
}
