package com.eliangilsierra.hexagonalscaffold.domain.spi;

import com.eliangilsierra.hexagonalscaffold.domain.model.Order;
import java.util.Optional;

/**
 * Output port: how the domain persists an order. Today it's backed by JPA/H2 —
 * the domain and use case never reference that directly.
 */
public interface OrderPersistencePort {

    Order save(Order order);

    Optional<Order> findById(Long orderId);
}
