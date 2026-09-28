package com.eliangilsierra.hexagonalscaffold.domain.util;

import java.math.BigDecimal;

/**
 * Pure domain logic: no Spring, no persistence, no I/O. Trivial on purpose —
 * the point is that this kind of rule belongs in `domain`, not scattered across
 * a service or a controller.
 */
public final class PriceCalculator {

    private PriceCalculator() {
    }

    public static BigDecimal calculateTotal(Integer quantity, BigDecimal unitPrice) {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
