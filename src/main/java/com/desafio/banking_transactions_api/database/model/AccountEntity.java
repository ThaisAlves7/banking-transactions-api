package com.desafio.banking_transactions_api.database.model;

import com.desafio.banking_transactions_api.enums.AccountStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "accounts", indexes = {
        @Index(name = "idx_account_number", columnList = "account_number", unique = true)
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String holder;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = true)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", updatable = false)
    private OffsetDateTime updatedAt;
}
