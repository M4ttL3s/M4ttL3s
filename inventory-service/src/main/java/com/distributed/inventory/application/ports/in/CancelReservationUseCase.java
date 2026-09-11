package com.distributed.inventory.application.ports.in;

import com.distributed.inventory.application.dto.ReservationResponse;

import java.util.UUID;

public interface CancelReservationUseCase {
    ReservationResponse cancelReservation(UUID orderId);
}
