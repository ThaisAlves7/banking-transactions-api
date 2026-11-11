package com.desafio.banking_transactions_api.dto;

import com.desafio.banking_transactions_api.enums.AccountStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class AccountResponseDto {
    Long id;
    String accountNumber;
    String holder;
    AccountStatus status;
    BigDecimal balance;
}
