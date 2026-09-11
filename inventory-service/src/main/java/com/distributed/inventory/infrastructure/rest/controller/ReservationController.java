package com.distributed.inventory.infrastructure.rest.controller;

import com.distributed.inventory.application.dto.ReservationResponse;
import com.distributed.inventory.application.dto.ReserveStockCommand;
import com.distributed.inventory.application.ports.in.CancelReservationUseCase;
import com.distributed.inventory.application.ports.in.ReserveStockUseCase;
import com.distributed.inventory.infrastructure.rest.dto.ReserveStockRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReserveStockUseCase reserveStockUseCase;
    private final CancelReservationUseCase cancelReservationUseCase;

    public ReservationController(ReserveStockUseCase reserveStockUseCase,
                                 CancelReservationUseCase cancelReservationUseCase) {
        this.reserveStockUseCase = reserveStockUseCase;
        this.cancelReservationUseCase = cancelReservationUseCase;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> reserveStock(@Valid @RequestBody ReserveStockRequest request) {
        ReserveStockCommand command = new ReserveStockCommand(request.orderId(), request.productId(), request.quantity());
        ReservationResponse response = reserveStockUseCase.reserveStock(command);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable UUID orderId) {
        ReservationResponse response = cancelReservationUseCase.cancelReservation(orderId);
        return ResponseEntity.ok(response);
    }
}
