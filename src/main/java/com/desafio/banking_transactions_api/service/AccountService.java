package com.desafio.banking_transactions_api.service;

import com.desafio.banking_transactions_api.database.repository.AccountRepository;
import com.desafio.banking_transactions_api.dto.AccountResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public List<AccountResponseDto> findAll() {
        return accountRepository.findAll()
                .stream()
                .map(account -> AccountResponseDto.builder()
                        .id(account.getId())
                        .accountNumber(account.getAccountNumber())
                        .holder(account.getHolder())
                        .status(account.getStatus())
                        .balance(account.getBalance())
                        .build())
                .toList();
    }
}

