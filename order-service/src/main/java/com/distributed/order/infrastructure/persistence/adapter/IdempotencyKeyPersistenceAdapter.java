package com.distributed.order.infrastructure.persistence.adapter;

import com.distributed.order.domain.model.IdempotencyRecord;
import com.distributed.order.domain.repository.IdempotencyKeyRepositoryPort;
import com.distributed.order.infrastructure.persistence.entity.IdempotencyKeyJpaEntity;
import com.distributed.order.infrastructure.persistence.repository.SpringDataIdempotencyKeyRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class IdempotencyKeyPersistenceAdapter implements IdempotencyKeyRepositoryPort {

    private final SpringDataIdempotencyKeyRepository springDataRepository;

    public IdempotencyKeyPersistenceAdapter(SpringDataIdempotencyKeyRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<IdempotencyRecord> findByKey(String key) {
        return springDataRepository.findByIdempotencyKey(key)
                .map(entity -> new IdempotencyRecord(
                        entity.getIdempotencyKey(),
                        entity.getOrderId(),
                        entity.getStatusCode(),
                        entity.getResponsePayload(),
                        entity.getCreatedAt()
                ));
    }

    @Override
    public IdempotencyRecord save(IdempotencyRecord record) {
        IdempotencyKeyJpaEntity entity = new IdempotencyKeyJpaEntity(
                record.getKey(),
                record.getOrderId(),
                record.getStatusCode(),
                record.getResponsePayload(),
                record.getCreatedAt()
        );
        IdempotencyKeyJpaEntity saved = springDataRepository.save(entity);
        return new IdempotencyRecord(
                saved.getIdempotencyKey(),
                saved.getOrderId(),
                saved.getStatusCode(),
                saved.getResponsePayload(),
                saved.getCreatedAt()
        );
    }

    @Override
    public boolean existsByKey(String key) {
        return springDataRepository.existsByIdempotencyKey(key);
    }
}
