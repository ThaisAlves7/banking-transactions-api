package com.desafio.banking_transactions_api.service;

import com.desafio.banking_transactions_api.database.model.AccountEntity;
import com.desafio.banking_transactions_api.database.repository.AccountRepository;
import com.desafio.banking_transactions_api.dto.AccountResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    private Function<AccountEntity, AccountResponseDto> accountMapper() {
        return account -> AccountResponseDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .holder(account.getHolder())
                .status(account.getStatus())
                .balance(account.getBalance())
                .build();

    }

    public List<AccountResponseDto> findAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(accountMapper())
                .collect(Collectors.toList());
    }

    public AccountResponseDto findAccountById(Long id) {
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta inválida tente novamente."));

        return accountMapper().apply(account);
    }
}
