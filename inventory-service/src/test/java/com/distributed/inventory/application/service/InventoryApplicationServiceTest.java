package com.distributed.inventory.application.service;

import com.distributed.inventory.application.dto.ReservationResponse;
import com.distributed.inventory.application.dto.ReserveStockCommand;
import com.distributed.inventory.domain.model.Reservation;
import com.distributed.inventory.domain.model.ReservationStatus;
import com.distributed.inventory.domain.repository.ProductRepositoryPort;
import com.distributed.inventory.domain.repository.ReservationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryApplicationServiceTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private ReservationRepositoryPort reservationRepositoryPort;

    private InventoryApplicationService service;

    @BeforeEach
    void setUp() {
        service = new InventoryApplicationService(productRepositoryPort, reservationRepositoryPort);
    }

    @Test
    @DisplayName("Should successfully reserve stock when stock is available")
    void shouldReserveStockSuccessfully() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        ReserveStockCommand command = new ReserveStockCommand(orderId, productId, 2);

        when(reservationRepositoryPort.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(productRepositoryPort.existsById(productId)).thenReturn(true);
        when(productRepositoryPort.deductStockAtomically(productId, 2)).thenReturn(1);
        when(reservationRepositoryPort.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        ReservationResponse response = service.reserveStock(command);

        assertEquals(ReservationStatus.RESERVED, response.status());
        verify(productRepositoryPort).deductStockAtomically(productId, 2);
    }

    @Test
    @DisplayName("Should reject reservation when stock is insufficient")
    void shouldRejectReservationWhenInsufficientStock() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        ReserveStockCommand command = new ReserveStockCommand(orderId, productId, 2);

        when(reservationRepositoryPort.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(productRepositoryPort.existsById(productId)).thenReturn(true);
        when(productRepositoryPort.deductStockAtomically(productId, 2)).thenReturn(0);
        when(reservationRepositoryPort.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        ReservationResponse response = service.reserveStock(command);

        assertEquals(ReservationStatus.REJECTED, response.status());
        verify(productRepositoryPort).deductStockAtomically(productId, 2);
    }

    @Test
    @DisplayName("Should handle sentinel cancel pending race condition")
    void shouldHandleSentinelCancelPending() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        ReserveStockCommand command = new ReserveStockCommand(orderId, productId, 2);

        Reservation sentinel = Reservation.createCancelPending(orderId);
        when(reservationRepositoryPort.findByOrderId(orderId)).thenReturn(Optional.of(sentinel));
        when(reservationRepositoryPort.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        ReservationResponse response = service.reserveStock(command);

        assertEquals(ReservationStatus.CANCELLED, response.status());
        verify(productRepositoryPort, never()).deductStockAtomically(any(), anyInt());
    }

    @Test
    @DisplayName("Should create sentinel on cancel when reservation doesn't exist yet")
    void shouldCreateSentinelOnCancel() {
        UUID orderId = UUID.randomUUID();

        when(reservationRepositoryPort.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(reservationRepositoryPort.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        ReservationResponse response = service.cancelReservation(orderId);

        assertEquals(ReservationStatus.CANCEL_PENDING, response.status());
        verify(productRepositoryPort, never()).restoreStockAtomically(any(), anyInt());
    }
}
