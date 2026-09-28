package com.eliangilsierra.hexagonalscaffold.infraestructure.output.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.eliangilsierra.hexagonalscaffold.domain.enums.OrderStatus;
import com.eliangilsierra.hexagonalscaffold.domain.model.Order;
import com.eliangilsierra.hexagonalscaffold.infraestructure.output.jpa.mapper.OrderEntityMapperImpl;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * The other end of the test pyramid from {@code OrderUseCaseTest}: this slice
 * actually talks to a database (H2), proving the adapter's SQL/mapping is
 * correct — something a mocked-repository unit test can't tell you.
 */
@DataJpaTest
@Import({OrderPersistenceAdapter.class, OrderEntityMapperImpl.class})
class OrderPersistenceAdapterTest {

    @Autowired
    private OrderPersistenceAdapter orderPersistenceAdapter;

    @Test
    void savesAndRetrievesAnOrder() {
        Order order = Order.builder()
                .customerEmail("demo@example.com")
                .productName("Widget")
                .quantity(2)
                .unitPrice(BigDecimal.TEN)
                .total(BigDecimal.valueOf(20))
                .status(OrderStatus.CREATED)
                .build();

        Order saved = orderPersistenceAdapter.save(order);

        assertThat(saved.getId()).isNotNull();

        Order found = orderPersistenceAdapter.findById(saved.getId()).orElseThrow();
        assertThat(found.getProductName()).isEqualTo("Widget");
        assertThat(found.getTotal()).isEqualByComparingTo("20");
        assertThat(found.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        assertThat(orderPersistenceAdapter.findById(999L)).isEmpty();
    }
}
