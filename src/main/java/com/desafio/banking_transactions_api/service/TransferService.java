package com.desafio.banking_transactions_api.service;

import com.desafio.banking_transactions_api.database.model.AccountEntity;
import com.desafio.banking_transactions_api.database.model.IdempotencyEntity;
import com.desafio.banking_transactions_api.database.model.TransferEntity;
import com.desafio.banking_transactions_api.database.repository.AccountRepository;
import com.desafio.banking_transactions_api.database.repository.IdempotenciaRepository;
import com.desafio.banking_transactions_api.database.repository.TransferRepository;
import com.desafio.banking_transactions_api.dto.TransferRequestDto;
import com.desafio.banking_transactions_api.dto.TransferResponseDto;
import com.desafio.banking_transactions_api.enums.AccountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final IdempotenciaRepository idempotenciaRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public TransferResponseDto executeTransfer(
            String idempotencyKey,
            TransferRequestDto request
    ) {
//        Validar repetição na requisição
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<IdempotencyEntity> existingRecord = idempotenciaRepository.findByIdempotencyKey(idempotencyKey);

            if (existingRecord.isPresent()) {
                IdempotencyEntity record = existingRecord.get();

                try {
//                    Retorna o valor da chave idempotência enviada
                    return objectMapper.readValue(
                            record.getResponseBody(),
                            TransferResponseDto.class
                    );

                } catch (Exception e) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Conflito na chave de idempotência"
                    );
                }
            }
        }

//        Regras e Validações
        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A conta de origem e destino não pode ser iguais"
            );
        }

        AccountEntity source = accountRepository.findById(request.sourceAccountId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Conta de origem não encontrada"
                ));

        AccountEntity destination = accountRepository.findById(request.destinationAccountId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Conta de destino não encontrada"
                ));

        if (source.getStatus() != AccountStatus.ACTIVE ||
                destination.getStatus() != AccountStatus.ACTIVE
        ) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Conta de origem ou destino inativa"
            );
        }

        if (source.getBalance().compareTo(request.amount()) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Saldo insuficiente na conta origem"
            );
        }

//        Executar Transferências e Atualização de Saldos
        source.setBalance(source.getBalance().subtract(request.amount()));
        destination.setBalance(destination.getBalance().add(request.amount()));

        accountRepository.save(source);
        accountRepository.save(destination);

//        Registrar Transferência
        TransferEntity transfer = new TransferEntity();
        transfer.setSourceAccount(source);
        transfer.setDestinationAccount(destination);
        transfer.setAmount(request.amount());
        transfer.setDescription(request.description());
        transfer.setStatus("COMPLETED");

        transferRepository.save(transfer);

        TransferResponseDto responseDto = new TransferResponseDto(
                transfer.getTransferId(),
                source.getId(),
                destination.getId(),
                transfer.getAmount(),
                transfer.getDescription(),
                transfer.getStatus(),
                transfer.getCreatedAt()

        );

//        Registrar Idempotência
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {

            try {

                IdempotencyEntity idempotencyEntity = new IdempotencyEntity();
                idempotencyEntity.setIdempotencyKey(idempotencyKey);
                idempotencyEntity.setTransferId(transfer.getTransferId());
                idempotencyEntity.setResponseStatus(201);
                idempotencyEntity.setResponseBody(
                        objectMapper.writeValueAsString(responseDto)
                );

                idempotenciaRepository.save(idempotencyEntity);

            } catch (Exception ignored) {
            }
        }


        return responseDto;
    }
}
