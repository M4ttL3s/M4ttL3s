package com.distributed.order.application.service;

import com.distributed.order.application.dto.CreateOrderCommand;
import com.distributed.order.application.dto.OrderItemCommand;
import com.distributed.order.application.dto.OrderResponse;
import com.distributed.order.domain.exception.DuplicateIdempotencyKeyException;
import com.distributed.order.domain.model.IdempotencyRecord;
import com.distributed.order.domain.model.Order;
import com.distributed.order.domain.model.OrderStatus;
import com.distributed.order.domain.repository.IdempotencyKeyRepositoryPort;
import com.distributed.order.domain.repository.OrderEventPublisherPort;
import com.distributed.order.domain.repository.OrderRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private IdempotencyKeyRepositoryPort idempotencyKeyRepositoryPort;

    @Mock
    private OrderEventPublisherPort orderEventPublisherPort;

    private OrderApplicationService service;

    @BeforeEach
    void setUp() {
        service = new OrderApplicationService(
                orderRepositoryPort,
                idempotencyKeyRepositoryPort,
                orderEventPublisherPort
        );
    }

    @Test
    @DisplayName("Should create order and publish event successfully")
    void shouldCreateOrderSuccessfully() {
        // Arrange
        UUID productId = UUID.randomUUID();
        CreateOrderCommand command = new CreateOrderCommand(
                "cust-1",
                List.of(new OrderItemCommand(productId, 2, new BigDecimal("10.0"))),
                "idem-key-1"
        );

        when(idempotencyKeyRepositoryPort.findByKey("idem-key-1")).thenReturn(Optional.empty());

        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return order; // Retornamos el mismo objeto con ID generado
        });

        // Act
        OrderResponse response = service.createOrder(command);

        // Assert
        assertNotNull(response.id());
        assertEquals("cust-1", response.customerId());
        assertEquals(OrderStatus.PENDING, response.status());

        verify(orderRepositoryPort, times(1)).save(any(Order.class));
        verify(idempotencyKeyRepositoryPort, times(1)).save(any(IdempotencyRecord.class));
        verify(orderEventPublisherPort, times(1)).publishOrderCreated(any(Order.class));
    }

    @Test
    @DisplayName("Should return existing order when idempotency key is present")
    void shouldReturnExistingOrderForIdempotencyKey() {
        // Arrange
        UUID existingOrderId = UUID.randomUUID();
        IdempotencyRecord record = new IdempotencyRecord("idem-key-2", existingOrderId, 201, "OK", LocalDateTime.now());

        CreateOrderCommand command = new CreateOrderCommand(
                "cust-1",
                List.of(new OrderItemCommand(UUID.randomUUID(), 1, new BigDecimal("10.0"))),
                "idem-key-2"
        );

        when(idempotencyKeyRepositoryPort.findByKey("idem-key-2")).thenReturn(Optional.of(record));

        Order existingOrder = Order.create("cust-1", List.of(new com.distributed.order.domain.model.OrderItem(null, UUID.randomUUID(), 1, new BigDecimal("10.0"))));
        when(orderRepositoryPort.findById(existingOrderId)).thenReturn(Optional.of(existingOrder));

        // Act
        OrderResponse response = service.createOrder(command);

        // Assert
        assertNotNull(response);
        verify(orderRepositoryPort, never()).save(any(Order.class));
        verify(orderEventPublisherPort, never()).publishOrderCreated(any(Order.class));
    }

    @Test
    @DisplayName("Should cancel order successfully")
    void shouldCancelOrderSuccessfully() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order order = Order.create("cust-1", List.of(new com.distributed.order.domain.model.OrderItem(null, UUID.randomUUID(), 1, new BigDecimal("10.0"))));
        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(order);

        // Act
        OrderResponse response = service.cancelOrder(orderId);

        // Assert
        assertEquals(OrderStatus.CANCELLED, response.status());
        verify(orderRepositoryPort, times(1)).save(order);
        verify(orderEventPublisherPort, times(1)).publishOrderCancelled(orderId);
    }
}
