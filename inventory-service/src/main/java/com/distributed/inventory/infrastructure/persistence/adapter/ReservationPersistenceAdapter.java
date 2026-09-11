package com.distributed.inventory.infrastructure.persistence.adapter;

import com.distributed.inventory.domain.model.Reservation;
import com.distributed.inventory.domain.repository.ReservationRepositoryPort;
import com.distributed.inventory.infrastructure.persistence.entity.ReservationJpaEntity;
import com.distributed.inventory.infrastructure.persistence.repository.SpringDataReservationRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ReservationPersistenceAdapter implements ReservationRepositoryPort {

    private final SpringDataReservationRepository reservationRepository;

    public ReservationPersistenceAdapter(SpringDataReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        ReservationJpaEntity entity = new ReservationJpaEntity(
                reservation.getId(),
                reservation.getOrderId(),
                reservation.getProductId(),
                reservation.getQuantity(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
        ReservationJpaEntity saved = reservationRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Reservation> findByOrderId(UUID orderId) {
        return reservationRepository.findByOrderId(orderId).map(this::toDomain);
    }

    @Override
    public boolean existsByOrderId(UUID orderId) {
        return reservationRepository.existsByOrderId(orderId);
    }

    private Reservation toDomain(ReservationJpaEntity entity) {
        return new Reservation(
                entity.getId(),
                entity.getOrderId(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
