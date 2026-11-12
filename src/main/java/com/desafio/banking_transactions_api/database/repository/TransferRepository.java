package com.desafio.banking_transactions_api.database.repository;

import com.desafio.banking_transactions_api.database.model.TransferEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransferRepository extends JpaRepository<TransferEntity, UUID> {

    @Query("""
            SELECT t FROM transfer t
            WHERE t.sourceAccount.id = :accountId
            OR t.destinationAccount.id = :accountId
            ORDER BY t.createdAt DESC
            """)
    Page<TransferEntity> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);
}
