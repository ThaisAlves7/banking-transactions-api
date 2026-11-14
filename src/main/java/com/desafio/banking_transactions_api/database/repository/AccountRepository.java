package com.desafio.banking_transactions_api.database.repository;

import com.desafio.banking_transactions_api.database.model.AccountEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a FROM AccountEntity a 
                    WHERE a.id = :id
            """)
    Optional<AccountEntity> findByIdWithLock(@Param("id") Long id);
}

