package com.eliangilsierra.hexagonalscaffold.infraestructure.output.messaging.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares the topic this scaffold uses. Spring Boot's auto-configured
 * {@code KafkaAdmin} creates it on startup if it doesn't already exist.
 */
@Configuration
public class KafkaConfig {

    public static final String TOPIC = "orders.created";

    @Bean
    public NewTopic ordersCreatedTopic() {
        return TopicBuilder.name(TOPIC).partitions(1).replicas(1).build();
    }
}
