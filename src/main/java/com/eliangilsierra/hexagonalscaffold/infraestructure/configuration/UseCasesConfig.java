package com.eliangilsierra.hexagonalscaffold.infraestructure.configuration;

import com.eliangilsierra.hexagonalscaffold.domain.api.OrderNotificationServicePort;
import com.eliangilsierra.hexagonalscaffold.domain.api.OrderServicePort;
import com.eliangilsierra.hexagonalscaffold.domain.spi.NotificationPort;
import com.eliangilsierra.hexagonalscaffold.domain.spi.OrderEventPublisherPort;
import com.eliangilsierra.hexagonalscaffold.domain.spi.OrderPersistencePort;
import com.eliangilsierra.hexagonalscaffold.domain.useCase.OrderNotificationUseCase;
import com.eliangilsierra.hexagonalscaffold.domain.useCase.OrderUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The only place that instantiates {@code domain.useCase} with Spring-managed
 * adapters — the domain package itself stays free of framework annotations.
 */
@Configuration
public class UseCasesConfig {

    @Bean
    public OrderServicePort orderServicePort(
            OrderPersistencePort orderPersistencePort, OrderEventPublisherPort orderEventPublisherPort) {
        return new OrderUseCase(orderPersistencePort, orderEventPublisherPort);
    }

    @Bean
    public OrderNotificationServicePort orderNotificationServicePort(
            OrderPersistencePort orderPersistencePort, NotificationPort notificationPort) {
        return new OrderNotificationUseCase(orderPersistencePort, notificationPort);
    }
}
