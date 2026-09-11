package com.distributed.order.infrastructure.messaging.listener;

import com.distributed.order.application.ports.in.ConfirmOrderUseCase;
import com.distributed.order.application.ports.in.RejectOrderUseCase;
import com.distributed.order.infrastructure.persistence.entity.ProcessedEventJpaEntity;
import com.distributed.order.infrastructure.persistence.repository.SpringDataProcessedEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class InventoryResponseListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryResponseListener.class);

    private final ConfirmOrderUseCase confirmOrderUseCase;
    private final RejectOrderUseCase rejectOrderUseCase;
    private final SpringDataProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    public InventoryResponseListener(ConfirmOrderUseCase confirmOrderUseCase,
                                     RejectOrderUseCase rejectOrderUseCase,
                                     SpringDataProcessedEventRepository processedEventRepository,
                                     ObjectMapper objectMapper) {
        this.confirmOrderUseCase = confirmOrderUseCase;
        this.rejectOrderUseCase = rejectOrderUseCase;
        this.processedEventRepository = processedEventRepository;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${order.rabbitmq.stock-response-queue:order.stock-response.queue}")
    @Transactional
    public void onInventoryStockResponse(Message message) {
        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(payload);

            UUID eventId = UUID.fromString(root.get("eventId").asText());
            UUID orderId = UUID.fromString(root.get("orderId").asText());

            if (processedEventRepository.existsById(eventId)) {
                log.info("Evento duplicado omitido. Event ID: {}", eventId);
                return;
            }

            boolean isRejected = root.has("reason") && !root.get("reason").isNull();

            if (isRejected) {
                String reason = root.get("reason").asText();
                log.warn("Reserva rechazada para Order ID: {}. Causa: {}", orderId, reason);
                rejectOrderUseCase.rejectOrder(orderId, reason);
                processedEventRepository.save(new ProcessedEventJpaEntity(eventId, "StockRejectedEvent", LocalDateTime.now()));
            } else {
                log.info("Stock reservado exitosamente. Confirmando Order ID: {}", orderId);
                confirmOrderUseCase.confirmOrder(orderId);
                processedEventRepository.save(new ProcessedEventJpaEntity(eventId, "StockReservedEvent", LocalDateTime.now()));
            }

        } catch (Exception ex) {
            log.error("Fallo inesperado al procesar respuesta de inventario", ex);
            throw new RuntimeException("Error crítico procesando mensaje de inventario", ex);
        }
    }
}
