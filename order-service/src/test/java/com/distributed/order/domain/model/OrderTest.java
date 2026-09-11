package com.distributed.order.domain.model;

import com.distributed.order.domain.exception.OrderDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    @DisplayName("Should create order with PENDING status and correct total amount")
    void shouldCreateOrderSuccessfully() {
        UUID prodId = UUID.randomUUID();
        OrderItem item = new OrderItem(null, prodId, 2, new BigDecimal("49.99"));

        Order order = Order.create("cust-123", List.of(item));

        assertNotNull(order.getId());
        assertEquals("cust-123", order.getCustomerId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(new BigDecimal("99.98"), order.getTotalAmount());
        assertEquals(1, order.getItems().size());
    }

    @Test
    @DisplayName("Should cancel pending order successfully")
    void shouldCancelPendingOrder() {
        UUID prodId = UUID.randomUUID();
        OrderItem item = new OrderItem(null, prodId, 1, new BigDecimal("100.00"));
        Order order = Order.create("cust-123", List.of(item));

        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when cancelling already cancelled order")
    void shouldThrowWhenCancellingTwice() {
        UUID prodId = UUID.randomUUID();
        OrderItem item = new OrderItem(null, prodId, 1, new BigDecimal("100.00"));
        Order order = Order.create("cust-123", List.of(item));
        order.cancel();

        assertThrows(OrderDomainException.class, order::cancel);
    }
}
