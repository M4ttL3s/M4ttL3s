package com.distributed.order.domain.repository;

import com.distributed.order.domain.model.IdempotencyRecord;

import java.util.Optional;

public interface IdempotencyKeyRepositoryPort {

    Optional<IdempotencyRecord> findByKey(String key);

    IdempotencyRecord save(IdempotencyRecord record);

    boolean existsByKey(String key);
}
