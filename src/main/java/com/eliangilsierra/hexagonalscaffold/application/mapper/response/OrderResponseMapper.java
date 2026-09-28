package com.eliangilsierra.hexagonalscaffold.application.mapper.response;

import com.eliangilsierra.hexagonalscaffold.application.dto.response.OrderResponse;
import com.eliangilsierra.hexagonalscaffold.domain.model.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderResponseMapper {

    OrderResponse toResponse(Order order);
}
