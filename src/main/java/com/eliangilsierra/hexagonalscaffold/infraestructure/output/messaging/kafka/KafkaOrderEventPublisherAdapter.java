package com.eliangilsierra.hexagonalscaffold.infraestructure.output.messaging.kafka;

import com.eliangilsierra.hexagonalscaffold.domain.spi.OrderEventPublisherPort;
import com.eliangilsierra.hexagonalscaffold.infraestructure.output.events.OrderCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Replaces {@code InProcessOrderEventPublisherAdapter} from java21-upgrade:
 * same port ({@link OrderEventPublisherPort}), now backed by Kafka instead of
 * an in-process event. {@code domain.useCase.OrderUseCase} did not change to
 * make this swap. The order id is used as the record key so all events for
 * the same order land on the same partition, in order.
 */
@Component
public class KafkaOrderEventPublisherAdapter implements OrderEventPublisherPort {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public KafkaOrderEventPublisherAdapter(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishOrderCreated(Long orderId) {
        kafkaTemplate.send(KafkaConfig.TOPIC, orderId.toString(), new OrderCreatedEvent(orderId));
    }
}
