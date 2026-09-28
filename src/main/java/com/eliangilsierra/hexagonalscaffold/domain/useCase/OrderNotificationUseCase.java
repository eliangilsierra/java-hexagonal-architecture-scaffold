package com.eliangilsierra.hexagonalscaffold.domain.useCase;

import com.eliangilsierra.hexagonalscaffold.common.exception.OrderNotFoundException;
import com.eliangilsierra.hexagonalscaffold.domain.api.OrderNotificationServicePort;
import com.eliangilsierra.hexagonalscaffold.domain.enums.OrderStatus;
import com.eliangilsierra.hexagonalscaffold.domain.model.Order;
import com.eliangilsierra.hexagonalscaffold.domain.spi.NotificationPort;
import com.eliangilsierra.hexagonalscaffold.domain.spi.OrderPersistencePort;

/**
 * The other side of the event: whatever adapter received "an order was
 * created" calls this, which does the actual notifying and flips the order to
 * {@code NOTIFIED}. Split from {@link OrderUseCase} on purpose — order
 * creation and order notification are two different triggers now, not one
 * synchronous call chain.
 */
public class OrderNotificationUseCase implements OrderNotificationServicePort {

    private final OrderPersistencePort orderPersistencePort;
    private final NotificationPort notificationPort;

    public OrderNotificationUseCase(OrderPersistencePort orderPersistencePort, NotificationPort notificationPort) {
        this.orderPersistencePort = orderPersistencePort;
        this.notificationPort = notificationPort;
    }

    @Override
    public void handleOrderCreated(Long orderId) {
        Order order = orderPersistencePort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        notificationPort.notify(order);

        order.setStatus(OrderStatus.NOTIFIED);
        orderPersistencePort.save(order);
    }
}
