package com.eliangilsierra.hexagonalscaffold.infraestructure.output.notification.adapter;

import com.eliangilsierra.hexagonalscaffold.domain.model.Order;
import com.eliangilsierra.hexagonalscaffold.domain.spi.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Simulated email adapter — logs what an email would contain instead of calling
 * a real SMTP server, so this scaffold runs with zero external configuration.
 * Wiring a real {@code JavaMailSender} here would be a change confined entirely
 * to this one class: {@link com.eliangilsierra.hexagonalscaffold.domain.useCase.OrderUseCase}
 * would not need to change at all. That containment is the whole point of the
 * port (see ADR-0002).
 */
@Component
@Profile("email")
public class EmailNotificationAdapter implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationAdapter.class);

    @Override
    public void notify(Order order) {
        log.info("[email-notification] (simulated) Sending email to {} — subject: 'Your order #{} for {} is confirmed'",
                order.getCustomerEmail(), order.getId(), order.getProductName());
    }
}
