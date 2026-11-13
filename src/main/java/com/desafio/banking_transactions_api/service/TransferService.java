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

import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final IdempotenciaRepository idempotenciaRepository;
    private final ObjectMapper objectMapper;

    @Transactional(rollbackFor = Exception.class)
    public TransferResponseDto executeTransfer(
            String idempotencyKey,
            TransferRequestDto request
    ) {
//        Gera o requestHash para comparação
        String requestHash = generateRequestHash(request);
//        Validar repetição na requisição
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<IdempotencyEntity> existingRecord = idempotenciaRepository.findByIdempotencyKey(idempotencyKey);

            if (existingRecord.isPresent()) {
                IdempotencyEntity record = existingRecord.get();

//                Validação de hash para requisições nova e a guardada
                if (!record.getRequestHash().equals(requestHash)) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Chaves idempotência diferentes"
                    );
                }

                try {
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
//        transfer.setCreatedAt();
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
                idempotencyEntity.setRequestHash(requestHash);
                idempotencyEntity.setResponseBody(
                        objectMapper.writeValueAsString(responseDto)
                );

                idempotenciaRepository.save(idempotencyEntity);

            } catch (Exception e) {
                // 2. Rollback forçado da operação inteira caso ocorra exceção ignorada no registro de idempotência
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Erro ao persistir dados de idempotência",
                        e
                );
            }
        }


        return responseDto;
    }

    //    Gera o hash de segurança e valida para comparação
    private String generateRequestHash(TransferRequestDto request) {
        try {
            String rawData = String.join("|",
                    String.valueOf(request.sourceAccountId()),
                    String.valueOf(request.destinationAccountId()),
                    request.amount().setScale(2, RoundingMode.HALF_UP).toPlainString(),
                    request.description() == null ? "" : request.description()
            );
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(rawData.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {

            throw new RuntimeException("Erro ao gerar hash da requisição", e);
        }
    }
}
