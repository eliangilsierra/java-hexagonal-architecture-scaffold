package com.eliangilsierra.hexagonalscaffold.infraestructure.output.notification.adapter;

import com.eliangilsierra.hexagonalscaffold.domain.model.Order;
import com.eliangilsierra.hexagonalscaffold.domain.spi.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * The default adapter: just logs. Good enough for local development and for
 * demonstrating the port without needing any external system configured.
 */
@Component
@Profile("console")
public class ConsoleNotificationAdapter implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(ConsoleNotificationAdapter.class);

    @Override
    public void notify(Order order) {
        log.info("[console-notification] Order #{} confirmed for {} — {} x {} = {}",
                order.getId(), order.getCustomerEmail(), order.getQuantity(), order.getProductName(), order.getTotal());
    }
}
