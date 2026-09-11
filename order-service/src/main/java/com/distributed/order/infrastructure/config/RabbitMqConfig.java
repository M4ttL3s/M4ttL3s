package com.distributed.order.infrastructure.config;

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

    public static final String ORDER_STOCK_RESPONSE_QUEUE = "order.stock-response.queue";
    public static final String ORDER_STOCK_RESPONSE_DLQ = "order.stock-response.dlq";

    public static final String ROUTING_KEY_STOCK_RESPONSE = "inventory.stock.#";

    @Bean
    public TopicExchange sagaExchange() {
        return new TopicExchange(SAGA_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange sagaDeadLetterExchange() {
        return new TopicExchange(SAGA_DLX, true, false);
    }

    @Bean
    public Queue orderStockResponseDlq() {
        return QueueBuilder.durable(ORDER_STOCK_RESPONSE_DLQ).build();
    }

    @Bean
    public Binding orderStockResponseDlqBinding(Queue orderStockResponseDlq, TopicExchange sagaDeadLetterExchange) {
        return BindingBuilder.bind(orderStockResponseDlq).to(sagaDeadLetterExchange).with(ORDER_STOCK_RESPONSE_DLQ);
    }

    @Bean
    public Queue orderStockResponseQueue() {
        return QueueBuilder.durable(ORDER_STOCK_RESPONSE_QUEUE)
                .withArgument("x-dead-letter-exchange", SAGA_DLX)
                .withArgument("x-dead-letter-routing-key", ORDER_STOCK_RESPONSE_DLQ)
                .build();
    }

    @Bean
    public Binding orderStockResponseBinding(Queue orderStockResponseQueue, TopicExchange sagaExchange) {
        return BindingBuilder.bind(orderStockResponseQueue).to(sagaExchange).with(ROUTING_KEY_STOCK_RESPONSE);
    }

    /**
     * Publicador serializa a JSON sin acoplamiento de tipo Java (__TypeId__)
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        template.setMessageConverter(converter);
        return template;
    }

    /**
     * Consumidor neutro que recibe payloads puros para desacoplar microservicios independientes
     */
    @Bean
    public MessageConverter messageConverter() {
        return new SimpleMessageConverter();
    }
}
