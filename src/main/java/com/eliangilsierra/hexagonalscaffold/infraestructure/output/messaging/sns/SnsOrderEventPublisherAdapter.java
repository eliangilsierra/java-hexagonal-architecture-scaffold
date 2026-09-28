package com.eliangilsierra.hexagonalscaffold.infraestructure.output.messaging.sns;

import com.eliangilsierra.hexagonalscaffold.domain.spi.OrderEventPublisherPort;
import com.eliangilsierra.hexagonalscaffold.infraestructure.output.events.OrderCreatedEvent;
import io.awspring.cloud.sns.core.SnsTemplate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Replaces the in-process adapter from java21-upgrade: same port
 * ({@link OrderEventPublisherPort}), now backed by an SNS topic.
 * {@code domain.useCase.OrderUseCase} did not change to make this swap.
 *
 * Unlike the plain {@code messaging-aws-sns-sqs} branch, publishing here is
 * wrapped in a retry + circuit breaker (see ADR-0010): a transient SNS/network
 * hiccup gets retried automatically, and a sustained outage trips the breaker
 * so the app stops hammering a broker that's already down, instead of piling
 * up failing calls.
 */
@Component
public class SnsOrderEventPublisherAdapter implements OrderEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(SnsOrderEventPublisherAdapter.class);

    private final SnsTemplate snsTemplate;
    private final String topicArn;

    public SnsOrderEventPublisherAdapter(
            SnsTemplate snsTemplate,
            @Value("${app.messaging.sns-topic-arn}") String topicArn) {
        this.snsTemplate = snsTemplate;
        this.topicArn = topicArn;
    }

    @Retry(name = "snsPublisher")
    @CircuitBreaker(name = "snsPublisher", fallbackMethod = "publishFallback")
    @Override
    public void publishOrderCreated(Long orderId) {
        snsTemplate.convertAndSend(topicArn, new OrderCreatedEvent(orderId));
    }

    /**
     * Resilience4j calls this when retries are exhausted or the circuit is
     * open. Order creation itself already succeeded and was returned to the
     * caller (see ADR-0003) — the failure here only means notification will
     * be late, not that the order is lost. A stricter system would persist
     * this as an outbox row to retry later; logging it is this scaffold's
     * honest, minimal stand-in for that.
     */
    private void publishFallback(Long orderId, Exception exception) {
        log.error("Failed to publish OrderCreatedEvent for order {} after retries: {}",
                orderId, exception.getMessage());
    }
}
