package com.distributed.inventory.domain.model;

import com.distributed.inventory.domain.exception.InventoryDomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    @DisplayName("Should create product with valid initial stock")
    void shouldCreateProductSuccessfully() {
        UUID id = UUID.randomUUID();
        Product product = Product.create(id, 100);

        assertEquals(id, product.getId());
        assertEquals(100, product.getStock());
    }

    @Test
    @DisplayName("Should reject negative initial stock")
    void shouldRejectNegativeStock() {
        UUID id = UUID.randomUUID();
        assertThrows(InventoryDomainException.class, () -> Product.create(id, -5));
    }

    @Test
    @DisplayName("Should handle sentinel reservation transitions correctly")
    void shouldHandleSentinelState() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        // 1. Cancel arrived first -> Sentinel CANCEL_PENDING
        Reservation sentinel = Reservation.createCancelPending(orderId);
        assertEquals(ReservationStatus.CANCEL_PENDING, sentinel.getStatus());
        assertEquals(0, sentinel.getQuantity());
        assertNull(sentinel.getProductId());

        // 2. Delayed Create arrives -> Sentinel marks CANCELLED without touching stock
        sentinel.markCancelledFromPending(productId, 5);
        assertEquals(ReservationStatus.CANCELLED, sentinel.getStatus());
        assertEquals(5, sentinel.getQuantity());
        assertEquals(productId, sentinel.getProductId());
    }
}
