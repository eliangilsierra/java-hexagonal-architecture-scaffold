package com.eliangilsierra.hexagonalscaffold.infraestructure.output.messaging.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the exchange/queue/binding this scaffold uses, and a JSON message
 * converter — Spring Boot's auto-configured {@code RabbitTemplate} and
 * {@code @RabbitListener} container factory both pick it up automatically, so
 * nothing here needs to construct those beans by hand.
 */
@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "orders.exchange";
    public static final String QUEUE = "orders.created.queue";
    public static final String ROUTING_KEY = "orders.created";

    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderCreatedQueue()).to(ordersExchange()).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
