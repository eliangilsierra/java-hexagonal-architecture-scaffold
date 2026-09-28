package com.eliangilsierra.hexagonalscaffold.infraestructure.output.jpa.repository;

import com.eliangilsierra.hexagonalscaffold.infraestructure.output.jpa.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
}
