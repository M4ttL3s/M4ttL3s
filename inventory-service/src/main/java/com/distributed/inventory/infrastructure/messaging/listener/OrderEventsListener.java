package com.distributed.inventory.infrastructure.messaging.listener;

import com.distributed.inventory.application.dto.ReservationResponse;
import com.distributed.inventory.application.dto.ReserveStockCommand;
import com.distributed.inventory.application.ports.in.CancelReservationUseCase;
import com.distributed.inventory.application.ports.in.ReserveStockUseCase;
import com.distributed.inventory.domain.model.ReservationStatus;
import com.distributed.inventory.domain.repository.InventoryEventPublisherPort;
import com.distributed.inventory.infrastructure.messaging.event.OrderCancelledEvent;
import com.distributed.inventory.infrastructure.messaging.event.OrderCreatedEvent;
import com.distributed.inventory.infrastructure.messaging.event.OrderItemPayload;
import com.distributed.inventory.infrastructure.persistence.entity.ProcessedEventJpaEntity;
import com.distributed.inventory.infrastructure.persistence.repository.SpringDataProcessedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class OrderEventsListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventsListener.class);

    private final ReserveStockUseCase reserveStockUseCase;
    private final CancelReservationUseCase cancelReservationUseCase;
    private final InventoryEventPublisherPort eventPublisherPort;
    private final SpringDataProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    public OrderEventsListener(ReserveStockUseCase reserveStockUseCase,
                               CancelReservationUseCase cancelReservationUseCase,
                               InventoryEventPublisherPort eventPublisherPort,
                               SpringDataProcessedEventRepository processedEventRepository,
                               ObjectMapper objectMapper) {
        this.reserveStockUseCase = reserveStockUseCase;
        this.cancelReservationUseCase = cancelReservationUseCase;
        this.eventPublisherPort = eventPublisherPort;
        this.processedEventRepository = processedEventRepository;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${inventory.rabbitmq.order-created-queue:inventory.order-created.queue}")
    @Transactional
    public void handleOrderCreated(Message message) {
        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);

            log.info("Processing OrderCreatedEvent for Order ID: {}, Event ID: {}", event.orderId(), event.eventId());

            if (processedEventRepository.existsById(event.eventId())) {
                log.info("Duplicated OrderCreatedEvent ignored. Event ID: {}", event.eventId());
                return;
            }

            for (OrderItemPayload item : event.items()) {
                ReserveStockCommand command = new ReserveStockCommand(
                        event.orderId(),
                        item.productId(),
                        item.quantity()
                );

                ReservationResponse response = reserveStockUseCase.reserveStock(command);

                if (response.status() == ReservationStatus.RESERVED) {
                    log.info("Stock reserved for Order ID: {}. Publishing StockReservedEvent...", event.orderId());
                    eventPublisherPort.publishStockReserved(event.orderId(), item.productId(), item.quantity());
                } else if (response.status() == ReservationStatus.REJECTED) {
                    log.warn("Insufficient stock for Order ID: {}. Publishing StockRejectedEvent...", event.orderId());
                    eventPublisherPort.publishStockRejected(event.orderId(), item.productId(), item.quantity(), "INSUFFICIENT_STOCK");
                } else if (response.status() == ReservationStatus.CANCELLED) {
                    log.info("Sentinel hit: Order ID: {} was previously cancelled. No stock reserved.", event.orderId());
                }
            }

            processedEventRepository.save(new ProcessedEventJpaEntity(event.eventId(), "OrderCreatedEvent", LocalDateTime.now()));

        } catch (Exception ex) {
            log.error("Unexpected error processing OrderCreatedEvent", ex);
            throw new RuntimeException("Critical failure processing order creation message", ex);
        }
    }

    @RabbitListener(queues = "${inventory.rabbitmq.order-cancelled-queue:inventory.order-cancelled.queue}")
    @Transactional
    public void handleOrderCancelled(Message message) {
        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            OrderCancelledEvent event = objectMapper.readValue(payload, OrderCancelledEvent.class);

            log.info("Processing OrderCancelledEvent for Order ID: {}, Event ID: {}", event.orderId(), event.eventId());

            if (processedEventRepository.existsById(event.eventId())) {
                log.info("Duplicated OrderCancelledEvent ignored. Event ID: {}", event.eventId());
                return;
            }

            ReservationResponse response = cancelReservationUseCase.cancelReservation(event.orderId());
            log.info("Cancellation processed for Order ID: {}. Result status: {}", event.orderId(), response.status());

            processedEventRepository.save(new ProcessedEventJpaEntity(event.eventId(), "OrderCancelledEvent", LocalDateTime.now()));

        } catch (Exception ex) {
            log.error("Unexpected error processing OrderCancelledEvent", ex);
            throw new RuntimeException("Critical failure processing order cancellation message", ex);
        }
    }
}
