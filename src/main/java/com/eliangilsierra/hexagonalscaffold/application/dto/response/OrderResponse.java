package com.eliangilsierra.hexagonalscaffold.application.dto.response;

import java.math.BigDecimal;

public record OrderResponse(
        Long id,
        String customerEmail,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal total,
        String status
) {
}
