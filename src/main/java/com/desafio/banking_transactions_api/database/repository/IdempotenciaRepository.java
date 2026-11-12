package com.desafio.banking_transactions_api.database.repository;

import com.desafio.banking_transactions_api.database.model.IdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotenciaRepository extends JpaRepository<IdempotencyEntity, Long> {
    Optional<IdempotencyEntity> findByIdempotencyKey(String idempotencyKey);
}
