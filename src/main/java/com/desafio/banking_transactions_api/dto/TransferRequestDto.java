package com.desafio.banking_transactions_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class TransferRequestDto {

    @NotNull(message = "Informe conta de origem da transferência")
    Long sourceAccountId;

    @NotNull(message = "Informe a conta destino da transferência")
    Long destinationAccountId;

    @NotNull(message = "O valor para transferência é obrigatório")
    @DecimalMin(value = "0.01", message = "Informe um valor válido para a transferência")
    BigDecimal amount;

    String description;
}
