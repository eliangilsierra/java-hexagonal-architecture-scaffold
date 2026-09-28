package com.eliangilsierra.hexagonalscaffold.domain.useCase;

import com.eliangilsierra.hexagonalscaffold.common.exception.OrderNotFoundException;
import com.eliangilsierra.hexagonalscaffold.domain.api.OrderServicePort;
import com.eliangilsierra.hexagonalscaffold.domain.enums.OrderStatus;
import com.eliangilsierra.hexagonalscaffold.domain.model.Order;
import com.eliangilsierra.hexagonalscaffold.domain.spi.NotificationPort;
import com.eliangilsierra.hexagonalscaffold.domain.spi.OrderPersistencePort;
import com.eliangilsierra.hexagonalscaffold.domain.util.PriceCalculator;
import java.math.BigDecimal;

/**
 * Orchestrates order creation. Depends only on ports ({@code domain.api},
 * {@code domain.spi}) — never on a JPA repository or a mail client directly.
 * That's what makes this class unit-testable with fake ports, no Spring
 * context and no database required.
 */
public class OrderUseCase implements OrderServicePort {

    private final OrderPersistencePort orderPersistencePort;
    private final NotificationPort notificationPort;

    public OrderUseCase(OrderPersistencePort orderPersistencePort, NotificationPort notificationPort) {
        this.orderPersistencePort = orderPersistencePort;
        this.notificationPort = notificationPort;
    }

    @Override
    public Order createOrder(String customerEmail, String productName, Integer quantity, BigDecimal unitPrice) {
        Order order = Order.builder()
                .customerEmail(customerEmail)
                .productName(productName)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .total(PriceCalculator.calculateTotal(quantity, unitPrice))
                .status(OrderStatus.CREATED)
                .build();

        Order saved = orderPersistencePort.save(order);

        notificationPort.notify(saved);
        saved.setStatus(OrderStatus.NOTIFIED);

        return orderPersistencePort.save(saved);
    }

    @Override
    public Order getOrder(Long orderId) {
        return orderPersistencePort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
