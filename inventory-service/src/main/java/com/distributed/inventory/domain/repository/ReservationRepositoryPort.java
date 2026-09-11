package com.distributed.inventory.domain.repository;

import com.distributed.inventory.domain.model.Reservation;

import java.util.Optional;
import java.util.UUID;

public interface ReservationRepositoryPort {

    Reservation save(Reservation reservation);

    Optional<Reservation> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);
}
