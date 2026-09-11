package com.distributed.order.infrastructure.persistence.repository;

import com.distributed.order.infrastructure.persistence.entity.ProcessedEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataProcessedEventRepository extends JpaRepository<ProcessedEventJpaEntity, UUID> {
}
