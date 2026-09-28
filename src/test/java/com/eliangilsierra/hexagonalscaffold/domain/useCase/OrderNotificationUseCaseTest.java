package com.eliangilsierra.hexagonalscaffold.domain.useCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eliangilsierra.hexagonalscaffold.common.exception.OrderNotFoundException;
import com.eliangilsierra.hexagonalscaffold.domain.enums.OrderStatus;
import com.eliangilsierra.hexagonalscaffold.domain.model.Order;
import com.eliangilsierra.hexagonalscaffold.domain.spi.NotificationPort;
import com.eliangilsierra.hexagonalscaffold.domain.spi.OrderPersistencePort;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderNotificationUseCaseTest {

    @Mock
    private OrderPersistencePort orderPersistencePort;

    @Mock
    private NotificationPort notificationPort;

    @Test
    void notifiesAndMarksOrderAsNotified() {
        Order order = Order.builder()
                .id(1L)
                .customerEmail("demo@example.com")
                .productName("Widget")
                .quantity(1)
                .unitPrice(BigDecimal.TEN)
                .total(BigDecimal.TEN)
                .status(OrderStatus.CREATED)
                .build();
        when(orderPersistencePort.findById(1L)).thenReturn(Optional.of(order));

        new OrderNotificationUseCase(orderPersistencePort, notificationPort).handleOrderCreated(1L);

        verify(notificationPort).notify(order);
        verify(orderPersistencePort).save(order);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NOTIFIED);
    }

    @Test
    void throwsWhenOrderDoesNotExist() {
        when(orderPersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                new OrderNotificationUseCase(orderPersistencePort, notificationPort).handleOrderCreated(99L))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
