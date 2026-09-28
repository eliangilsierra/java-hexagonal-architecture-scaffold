package com.eliangilsierra.hexagonalscaffold.application.handler;

import com.eliangilsierra.hexagonalscaffold.application.dto.request.CreateOrderRequest;
import com.eliangilsierra.hexagonalscaffold.application.dto.response.OrderResponse;

/**
 * Sits between the REST controller and the domain port: translates DTOs to/from
 * domain calls. The controller never touches {@code domain.api} directly.
 */
public interface OrderHandler {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrder(Long orderId);
}
