package com.distributed.inventory.infrastructure.persistence.repository;

import com.distributed.inventory.infrastructure.persistence.entity.ReservationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataReservationRepository extends JpaRepository<ReservationJpaEntity, UUID> {

    Optional<ReservationJpaEntity> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);
}
