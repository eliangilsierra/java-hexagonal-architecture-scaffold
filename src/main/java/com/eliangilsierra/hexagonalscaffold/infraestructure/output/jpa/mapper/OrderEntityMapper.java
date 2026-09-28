package com.eliangilsierra.hexagonalscaffold.infraestructure.output.jpa.mapper;

import com.eliangilsierra.hexagonalscaffold.domain.model.Order;
import com.eliangilsierra.hexagonalscaffold.infraestructure.output.jpa.entity.OrderEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderEntityMapper {

    OrderEntity toEntity(Order order);

    Order toDomain(OrderEntity entity);
}
