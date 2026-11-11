package com.desafio.banking_transactions_api.controller;

import com.desafio.banking_transactions_api.dto.AccountResponseDto;
import com.desafio.banking_transactions_api.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<AccountResponseDto>> findAll() {
        List<AccountResponseDto> accounts = accountService.findAll();

        return ResponseEntity.ok(accounts);
    }
}
