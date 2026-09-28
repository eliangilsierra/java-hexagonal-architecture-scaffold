package com.eliangilsierra.hexagonalscaffold.infraestructure.output.messaging.sns;

import com.eliangilsierra.hexagonalscaffold.domain.spi.OrderEventPublisherPort;
import com.eliangilsierra.hexagonalscaffold.infraestructure.output.events.OrderCreatedEvent;
import io.awspring.cloud.sns.core.SnsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Replaces {@code InProcessOrderEventPublisherAdapter} from java21-upgrade:
 * same port ({@link OrderEventPublisherPort}), now backed by an SNS topic
 * instead of an in-process event. {@code domain.useCase.OrderUseCase} did not
 * change to make this swap.
 */
@Component
public class SnsOrderEventPublisherAdapter implements OrderEventPublisherPort {

    private final SnsTemplate snsTemplate;
    private final String topicArn;

    public SnsOrderEventPublisherAdapter(
            SnsTemplate snsTemplate,
            @Value("${app.messaging.sns-topic-arn}") String topicArn) {
        this.snsTemplate = snsTemplate;
        this.topicArn = topicArn;
    }

    @Override
    public void publishOrderCreated(Long orderId) {
        snsTemplate.convertAndSend(topicArn, new OrderCreatedEvent(orderId));
    }
}
