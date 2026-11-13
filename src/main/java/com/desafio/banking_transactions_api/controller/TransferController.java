package com.desafio.banking_transactions_api.controller;

import com.desafio.banking_transactions_api.dto.TransferRequestDto;
import com.desafio.banking_transactions_api.dto.TransferResponseDto;
import com.desafio.banking_transactions_api.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponseDto> transfer(
            @RequestHeader(value = "Idempotency-Key")
            String idempotencyKey,
            @Valid @RequestBody TransferRequestDto request
    ) {
        TransferResponseDto response = transferService.executeTransfer(
                idempotencyKey, request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
