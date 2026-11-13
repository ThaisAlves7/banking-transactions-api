package com.desafio.banking_transactions_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequestDto(
        @NotNull(message = "Informe conta de origem da transferência")
        Long sourceAccountId,

        @NotNull(message = "Informe a conta destino da transferência")
        Long destinationAccountId,

        @NotNull(message = "O valor para transferência é obrigatório")
        @DecimalMin(value = "0.01", message = "Informe um valor válido para a transferência")
        @Digits(integer = 17, fraction = 2, message = "O valor deve possuir no maximo 2 casas decimais")
        BigDecimal amount,

        String description
) {
}
