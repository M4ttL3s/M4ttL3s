package com.distributed.order.infrastructure.persistence.repository;

import com.distributed.order.infrastructure.persistence.entity.IdempotencyKeyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataIdempotencyKeyRepository extends JpaRepository<IdempotencyKeyJpaEntity, Long> {

    Optional<IdempotencyKeyJpaEntity> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);
}
