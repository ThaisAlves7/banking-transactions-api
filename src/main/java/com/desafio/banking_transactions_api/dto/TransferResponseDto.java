package com.desafio.banking_transactions_api.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record TransferResponseDto(
        UUID transferId,
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount,
        String description,
        String status,
        OffsetDateTime createdAt
        ) {
}
