package com.eliangilsierra.hexagonalscaffold.domain.useCase;

import com.eliangilsierra.hexagonalscaffold.common.exception.OrderNotFoundException;
import com.eliangilsierra.hexagonalscaffold.domain.api.OrderServicePort;
import com.eliangilsierra.hexagonalscaffold.domain.enums.OrderStatus;
import com.eliangilsierra.hexagonalscaffold.domain.model.Order;
import com.eliangilsierra.hexagonalscaffold.domain.spi.OrderEventPublisherPort;
import com.eliangilsierra.hexagonalscaffold.domain.spi.OrderPersistencePort;
import com.eliangilsierra.hexagonalscaffold.domain.util.PriceCalculator;
import java.math.BigDecimal;

/**
 * Orchestrates order creation only. Depends only on ports ({@code domain.api},
 * {@code domain.spi}) — never on a JPA repository or an event broker directly.
 * That's what makes this class unit-testable with fake ports, no Spring
 * context and no database required.
 *
 * As of this branch, creating an order and notifying about it are two separate
 * events: this use case saves the order as {@code CREATED} and publishes an
 * event — it does not wait for, or know about, notification succeeding. See
 * {@link OrderNotificationUseCase} for the other half, and ADR-0003 for why.
 */
public class OrderUseCase implements OrderServicePort {

    private final OrderPersistencePort orderPersistencePort;
    private final OrderEventPublisherPort orderEventPublisherPort;

    public OrderUseCase(OrderPersistencePort orderPersistencePort, OrderEventPublisherPort orderEventPublisherPort) {
        this.orderPersistencePort = orderPersistencePort;
        this.orderEventPublisherPort = orderEventPublisherPort;
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
        orderEventPublisherPort.publishOrderCreated(saved.getId());

        return saved;
    }

    @Override
    public Order getOrder(Long orderId) {
        return orderPersistencePort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
