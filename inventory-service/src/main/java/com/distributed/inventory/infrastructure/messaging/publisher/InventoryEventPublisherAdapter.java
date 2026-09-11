package com.distributed.inventory.infrastructure.messaging.publisher;

import com.distributed.inventory.domain.repository.InventoryEventPublisherPort;
import com.distributed.inventory.infrastructure.messaging.event.StockRejectedEvent;
import com.distributed.inventory.infrastructure.messaging.event.StockReservedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class InventoryEventPublisherAdapter implements InventoryEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventPublisherAdapter.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${inventory.rabbitmq.exchange:saga.exchange}")
    private String exchange;

    @Value("${inventory.rabbitmq.routing-key-reserved:inventory.stock.reserved}")
    private String routingKeyReserved;

    @Value("${inventory.rabbitmq.routing-key-rejected:inventory.stock.rejected}")
    private String routingKeyRejected;

    public InventoryEventPublisherAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishStockReserved(UUID orderId, UUID productId, int quantity) {
        StockReservedEvent event = new StockReservedEvent(
                UUID.randomUUID(),
                orderId,
                productId,
                quantity,
                LocalDateTime.now()
        );

        log.info("Publishing StockReservedEvent for Order ID: {}, Event ID: {}", orderId, event.eventId());
        rabbitTemplate.convertAndSend(exchange, routingKeyReserved, event);
    }

    @Override
    public void publishStockRejected(UUID orderId, UUID productId, int quantity, String reason) {
        StockRejectedEvent event = new StockRejectedEvent(
                UUID.randomUUID(),
                orderId,
                productId,
                quantity,
                reason,
                LocalDateTime.now()
        );

        log.info("Publishing StockRejectedEvent for Order ID: {}, Event ID: {}, Reason: {}", orderId, event.eventId(), reason);
        rabbitTemplate.convertAndSend(exchange, routingKeyRejected, event);
    }
}
