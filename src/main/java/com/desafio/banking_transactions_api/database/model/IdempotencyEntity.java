package com.desafio.banking_transactions_api.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys", indexes = {
        @Index(name = "idx_idempotency_key", columnList = "idempotency_key")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
// Controle de Idempotência para evitar requisições duplicadas
public class IdempotencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "transfer_id")
    private UUID transferId;

    @NotNull
    @Column(name = "response_status", nullable = false)
    private Integer responseStatus;

    @Lob
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
