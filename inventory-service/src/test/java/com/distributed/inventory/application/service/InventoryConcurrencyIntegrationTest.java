package com.distributed.inventory.application.service;

import com.distributed.inventory.application.dto.CreateProductCommand;
import com.distributed.inventory.application.dto.ReserveStockCommand;
import com.distributed.inventory.domain.model.Product;
import com.distributed.inventory.domain.repository.ProductRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class InventoryConcurrencyIntegrationTest {

    @Autowired
    private InventoryApplicationService inventoryApplicationService;

    @Autowired
    private ProductRepositoryPort productRepositoryPort;

    @Test
    @DisplayName("Should handle 100 concurrent requests safely without negative stock")
    void shouldHandleConcurrentReservationsSafely() throws InterruptedException {
        // Arrange: Create a product with 10 stock
        UUID productId = UUID.randomUUID();
        inventoryApplicationService.createProduct(new CreateProductCommand(productId, 10));

        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        AtomicInteger successfulReservations = new AtomicInteger(0);
        AtomicInteger rejectedReservations = new AtomicInteger(0);

        // Act: 100 threads try to reserve 1 item each
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    UUID orderId = UUID.randomUUID();
                    var response = inventoryApplicationService.reserveStock(
                            new ReserveStockCommand(orderId, productId, 1)
                    );
                    
                    if (response.status().name().equals("RESERVED")) {
                        successfulReservations.incrementAndGet();
                    } else if (response.status().name().equals("REJECTED")) {
                        rejectedReservations.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // Assert
        // Solo 10 reservas debieron ser exitosas, y 90 rechazadas.
        assertEquals(10, successfulReservations.get(), "Should have exactly 10 successful reservations");
        assertEquals(90, rejectedReservations.get(), "Should have exactly 90 rejected reservations");

        // El stock final debe ser 0
        Product finalProduct = productRepositoryPort.findById(productId).orElseThrow();
        assertEquals(0, finalProduct.getStock(), "Final stock should be 0");
    }
}
