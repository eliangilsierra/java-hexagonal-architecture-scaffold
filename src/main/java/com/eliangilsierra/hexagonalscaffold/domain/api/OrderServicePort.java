package com.eliangilsierra.hexagonalscaffold.domain.api;

import com.eliangilsierra.hexagonalscaffold.domain.model.Order;
import java.math.BigDecimal;

/**
 * Input port: the use cases this service exposes, independent of REST/DTOs.
 */
public interface OrderServicePort {

    Order createOrder(String customerEmail, String productName, Integer quantity, BigDecimal unitPrice);

    Order getOrder(Long orderId);
}
