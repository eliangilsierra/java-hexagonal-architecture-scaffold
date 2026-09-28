package com.eliangilsierra.hexagonalscaffold.domain.model;

import com.eliangilsierra.hexagonalscaffold.domain.enums.OrderStatus;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Plain domain model — no JPA, no Jackson, no framework annotations. It has no
 * idea it will end up in a database or that a notification gets sent about it.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Long id;
    private String customerEmail;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal total;
    private OrderStatus status;
}
