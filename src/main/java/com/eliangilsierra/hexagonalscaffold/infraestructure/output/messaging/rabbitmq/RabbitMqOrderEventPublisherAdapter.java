package com.eliangilsierra.hexagonalscaffold.infraestructure.output.messaging.rabbitmq;

import com.eliangilsierra.hexagonalscaffold.domain.spi.OrderEventPublisherPort;
import com.eliangilsierra.hexagonalscaffold.infraestructure.output.events.OrderCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Replaces {@code InProcessOrderEventPublisherAdapter} from java21-upgrade:
 * same port ({@link OrderEventPublisherPort}), now backed by a real broker.
 * {@code domain.useCase.OrderUseCase}, which calls this port, did not change
 * at all to make this swap.
 */
@Component
public class RabbitMqOrderEventPublisherAdapter implements OrderEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqOrderEventPublisherAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishOrderCreated(Long orderId) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.ROUTING_KEY, new OrderCreatedEvent(orderId));
    }
}
