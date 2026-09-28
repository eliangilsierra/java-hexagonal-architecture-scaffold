package com.eliangilsierra.hexagonalscaffold.domain.useCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eliangilsierra.hexagonalscaffold.domain.enums.OrderStatus;
import com.eliangilsierra.hexagonalscaffold.domain.model.Order;
import com.eliangilsierra.hexagonalscaffold.domain.spi.NotificationPort;
import com.eliangilsierra.hexagonalscaffold.domain.spi.OrderPersistencePort;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * No Spring context, no database, no mail server: fake/mocked ports are enough
 * to exercise the use case end to end — exactly the point of hexagonal
 * architecture.
 */
@ExtendWith(MockitoExtension.class)
class OrderUseCaseTest {

    @Mock
    private OrderPersistencePort orderPersistencePort;

    @Mock
    private NotificationPort notificationPort;

    @Test
    void createsOrderCalculatesTotalAndNotifies() {
        when(orderPersistencePort.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderUseCase orderUseCase = new OrderUseCase(orderPersistencePort, notificationPort);

        Order order = orderUseCase.createOrder("demo@example.com", "Widget", 3, BigDecimal.valueOf(10));

        assertThat(order.getTotal()).isEqualByComparingTo("30");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NOTIFIED);
        verify(notificationPort).notify(any(Order.class));
    }
}
