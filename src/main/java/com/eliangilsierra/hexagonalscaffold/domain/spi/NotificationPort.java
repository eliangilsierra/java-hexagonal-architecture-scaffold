package com.eliangilsierra.hexagonalscaffold.domain.spi;

import com.eliangilsierra.hexagonalscaffold.domain.model.Order;

/**
 * Output port: "tell someone an order was placed." Two adapters implement this
 * — console and email — selected by Spring profile (see ADR-0002). The use case
 * that calls this has no idea which one is active, or how notification actually
 * happens.
 */
public interface NotificationPort {

    void notify(Order order);
}
