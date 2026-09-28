package com.eliangilsierra.hexagonalscaffold.domain.api;

/**
 * Input port: "react to an order having been created." Whatever triggers this
 * — an in-process event listener today, a {@code @RabbitListener}/
 * {@code @KafkaListener}/SQS poller in the messaging branches — is a driving
 * adapter, exactly like {@code OrderController} is for HTTP.
 */
public interface OrderNotificationServicePort {

    void handleOrderCreated(Long orderId);
}
