package com.desafio.banking_transactions_api.service;

import com.desafio.banking_transactions_api.database.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
}
