package com.eliangilsierra.hexagonalscaffold.application.handler.impl;

import com.eliangilsierra.hexagonalscaffold.application.dto.request.CreateOrderRequest;
import com.eliangilsierra.hexagonalscaffold.application.dto.response.OrderResponse;
import com.eliangilsierra.hexagonalscaffold.application.handler.OrderHandler;
import com.eliangilsierra.hexagonalscaffold.application.mapper.response.OrderResponseMapper;
import com.eliangilsierra.hexagonalscaffold.domain.api.OrderServicePort;
import org.springframework.stereotype.Component;

@Component
public class OrderHandlerImpl implements OrderHandler {

    private final OrderServicePort orderServicePort;
    private final OrderResponseMapper orderResponseMapper;

    public OrderHandlerImpl(OrderServicePort orderServicePort, OrderResponseMapper orderResponseMapper) {
        this.orderServicePort = orderServicePort;
        this.orderResponseMapper = orderResponseMapper;
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        var order = orderServicePort.createOrder(
                request.customerEmail(), request.productName(), request.quantity(), request.unitPrice());
        return orderResponseMapper.toResponse(order);
    }

    @Override
    public OrderResponse getOrder(Long orderId) {
        return orderResponseMapper.toResponse(orderServicePort.getOrder(orderId));
    }
}
