package com.eliangilsierra.hexagonalscaffold.domain.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PriceCalculatorTest {

    @Test
    void multipliesUnitPriceByQuantity() {
        BigDecimal total = PriceCalculator.calculateTotal(3, BigDecimal.valueOf(9.5));

        assertThat(total).isEqualByComparingTo("28.5");
    }
}
