package com.distributed.inventory.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String SAGA_EXCHANGE = "saga.exchange";
    public static final String SAGA_DLX = "saga.dlx";

    public static final String ORDER_CREATED_QUEUE = "inventory.order-created.queue";
    public static final String ORDER_CREATED_DLQ = "inventory.order-created.dlq";

    public static final String ORDER_CANCELLED_QUEUE = "inventory.order-cancelled.queue";
    public static final String ORDER_CANCELLED_DLQ = "inventory.order-cancelled.dlq";

    public static final String ROUTING_KEY_ORDER_CREATED = "order.created";
    public static final String ROUTING_KEY_ORDER_CANCELLED = "order.cancelled";

    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(SAGA_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange sagaDeadLetterExchange() {
        return new TopicExchange(SAGA_DLX, true, false);
    }

    // Dead Letter Queues
    @Bean
    public Queue orderCreatedDlq() {
        return QueueBuilder.durable(ORDER_CREATED_DLQ).build();
    }

    @Bean
    public Binding orderCreatedDlqBinding(Queue orderCreatedDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(orderCreatedDlq).to(sagaDeadLetterExchange).with(ORDER_CREATED_DLQ);
    }

    @Bean
    public Queue orderCancelledDlq() {
        return QueueBuilder.durable(ORDER_CANCELLED_DLQ).build();
    }

    @Bean
    public Binding orderCancelledDlqBinding(Queue orderCancelledDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(orderCancelledDlq).to(sagaDeadLetterExchange).with(ORDER_CANCELLED_DLQ);
    }

    // Main Queues with DLX redirection
    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", SAGA_DLX)
                .withArgument("x-dead-letter-routing-key", ORDER_CREATED_DLQ)
                .build();
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(sagaExchange).with(ROUTING_KEY_ORDER_CREATED);
    }

    @Bean
    public Queue orderCancelledQueue() {
        return QueueBuilder.durable(ORDER_CANCELLED_QUEUE)
                .withArgument("x-dead-letter-exchange", SAGA_DLX)
                .withArgument("x-dead-letter-routing-key", ORDER_CANCELLED_DLQ)
                .build();
    }

    @Bean
    public Binding orderCancelledBinding(Queue orderCancelledQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(orderCancelledQueue).to(sagaExchange).with(ROUTING_KEY_ORDER_CANCELLED);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new SimpleMessageConverter();
    }
}
