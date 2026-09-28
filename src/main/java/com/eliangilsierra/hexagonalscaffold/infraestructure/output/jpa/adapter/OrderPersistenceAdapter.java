package com.eliangilsierra.hexagonalscaffold.infraestructure.output.jpa.adapter;

import com.eliangilsierra.hexagonalscaffold.domain.model.Order;
import com.eliangilsierra.hexagonalscaffold.domain.spi.OrderPersistencePort;
import com.eliangilsierra.hexagonalscaffold.infraestructure.output.jpa.mapper.OrderEntityMapper;
import com.eliangilsierra.hexagonalscaffold.infraestructure.output.jpa.repository.OrderJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class OrderPersistenceAdapter implements OrderPersistencePort {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderEntityMapper orderEntityMapper;

    public OrderPersistenceAdapter(OrderJpaRepository orderJpaRepository, OrderEntityMapper orderEntityMapper) {
        this.orderJpaRepository = orderJpaRepository;
        this.orderEntityMapper = orderEntityMapper;
    }

    @Override
    public Order save(Order order) {
        var saved = orderJpaRepository.save(orderEntityMapper.toEntity(order));
        return orderEntityMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        return orderJpaRepository.findById(orderId).map(orderEntityMapper::toDomain);
    }
}
