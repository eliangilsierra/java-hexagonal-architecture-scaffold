package com.eliangilsierra.hexagonalscaffold.domain.useCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eliangilsierra.hexagonalscaffold.domain.enums.OrderStatus;
import com.eliangilsierra.hexagonalscaffold.domain.model.Order;
import com.eliangilsierra.hexagonalscaffold.domain.spi.OrderEventPublisherPort;
import com.eliangilsierra.hexagonalscaffold.domain.spi.OrderPersistencePort;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * No Spring context, no database, no broker: fake/mocked ports are enough to
 * exercise the use case end to end. As of this branch, creating an order only
 * saves it and publishes an event — it no longer notifies synchronously (see
 * {@link OrderNotificationUseCaseTest} for that half).
 */
@ExtendWith(MockitoExtension.class)
class OrderUseCaseTest {

    @Mock
    private OrderPersistencePort orderPersistencePort;

    @Mock
    private OrderEventPublisherPort orderEventPublisherPort;

    @Test
    void createsOrderCalculatesTotalAndPublishesEventWithoutNotifying() {
        when(orderPersistencePort.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(42L);
            return order;
        });

        OrderUseCase orderUseCase = new OrderUseCase(orderPersistencePort, orderEventPublisherPort);

        Order order = orderUseCase.createOrder("demo@example.com", "Widget", 3, BigDecimal.valueOf(10));

        assertThat(order.getTotal()).isEqualByComparingTo("30");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        verify(orderEventPublisherPort).publishOrderCreated(eq(42L));
    }
}
