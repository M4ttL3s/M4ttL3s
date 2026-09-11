package com.distributed.order.infrastructure.messaging.publisher;

import com.distributed.order.domain.model.Order;
import com.distributed.order.domain.repository.OrderEventPublisherPort;
import com.distributed.order.infrastructure.messaging.event.OrderCancelledEvent;
import com.distributed.order.infrastructure.messaging.event.OrderCreatedEvent;
import com.distributed.order.infrastructure.messaging.event.OrderItemPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class OrderEventPublisherAdapter implements OrderEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisherAdapter.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${order.rabbitmq.exchange:saga.exchange}")
    private String exchange;

    @Value("${order.rabbitmq.routing-key-created:order.created}")
    private String routingKeyCreated;

    @Value("${order.rabbitmq.routing-key-cancelled:order.cancelled}")
    private String routingKeyCancelled;

    public OrderEventPublisherAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishOrderCreated(Order order) {
        List<OrderItemPayload> itemPayloads = order.getItems().stream()
                .map(item -> new OrderItemPayload(item.getProductId(), item.getQuantity(), item.getUnitPrice()))
                .toList();

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                itemPayloads,
                LocalDateTime.now()
        );

        log.info("Publishing OrderCreatedEvent for Order ID: {}, Event ID: {}", order.getId(), event.eventId());
        rabbitTemplate.convertAndSend(exchange, routingKeyCreated, event);
    }

    @Override
    public void publishOrderCancelled(UUID orderId) {
        OrderCancelledEvent event = new OrderCancelledEvent(
                UUID.randomUUID(),
                orderId,
                LocalDateTime.now()
        );

        log.info("Publishing OrderCancelledEvent for Order ID: {}, Event ID: {}", orderId, event.eventId());
        rabbitTemplate.convertAndSend(exchange, routingKeyCancelled, event);
    }
}
