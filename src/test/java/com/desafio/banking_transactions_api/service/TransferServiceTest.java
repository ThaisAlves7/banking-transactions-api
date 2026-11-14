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
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private IdempotenciaRepository idempotenciaRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TransferService transferService;

    private Long sourceId;
    private Long destId;
    private TransferRequestDto requestDto;
    private AccountEntity sourceAccount;
    private AccountEntity destinationAccount;

    @BeforeEach
    void setUp() {
        sourceId = 1L;
        destId = 2L;

        requestDto = new TransferRequestDto(sourceId, destId, new BigDecimal("100.00"), "Teste de transferência");

        sourceAccount = new AccountEntity();
        sourceAccount.setId(sourceId);
        sourceAccount.setBalance(new BigDecimal("500.00"));
        sourceAccount.setStatus(AccountStatus.ACTIVE);

        destinationAccount = new AccountEntity();
        destinationAccount.setId(destId);
        destinationAccount.setBalance(new BigDecimal("200.00"));
        destinationAccount.setStatus(AccountStatus.ACTIVE);
    }

    @Test
    void makeTransferSuccessfully() {
//        Simula a consulta da conta de Origem e Destinatário
        when(accountRepository.findByIdWithLock(sourceId))
                .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdWithLock(destId))
                .thenReturn(Optional.of(destinationAccount));

//        Simula a consulta da chave de idempotência
        when(accountRepository.findByIdWithLock(destId))
                .thenReturn(Optional.of(destinationAccount));

//        Simula o save() das contas
        when(accountRepository.save(any(AccountEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(transferRepository.save(any(TransferEntity.class)))
                .thenAnswer(invocation -> {
                    TransferEntity t = invocation.getArgument(0);

                    t.setTransferId(UUID.randomUUID());
                    t.setCreatedAt(OffsetDateTime.now());

                    return t;
                });

        TransferResponseDto response = transferService.executeTransfer("key-123", requestDto);

        assertNotNull(response);

        assertEquals(new BigDecimal("400.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("300.00"), destinationAccount.getBalance());

        verify(accountRepository, times(1)).save(sourceAccount);
        verify(accountRepository, times(1)).save(destinationAccount);

        verify(transferRepository, times(1))
                .save(any(TransferEntity.class));

    }

    @Test
    void throwExceptionWhenIdempotenceHasDifferentContent() {
//        Gera um hash diferente da transação
        IdempotencyEntity existingIdempotency = new IdempotencyEntity();

        existingIdempotency.setIdempotencyKey("key-123");
        existingIdempotency.setRequestHash("hash-diferente");

//        Simula a consulta do hash
        when(idempotenciaRepository.findByIdempotencyKey("key-123"))
                .thenReturn(Optional.of(existingIdempotency));

//        Simula a chamada da requisição com hash criado
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> {
                    transferService.executeTransfer("key-123", requestDto);
                }
        );

//      Valida se causou o erro esperado
        assertEquals(409, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("A chave de idempotência já foi utilizada para outra transação"));
    }

    @Test
    void throwExceptionIfCountsAreEqual() {
//        Simula uma requisição com origem e destinatário iguais
        TransferRequestDto invalidRequest = new TransferRequestDto(
                sourceId,
                sourceId,
                new BigDecimal("100.00"),
                "Mesma Conta"
        );

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> {
                    transferService.executeTransfer("key-same-account", invalidRequest);
                });

//        Verifica se causou o erro esperado
        assertEquals(409, exception.getStatusCode().value());
    }

    @Test
    void throwExceptionIfBalanceIsInsufficient() {
        sourceAccount.setBalance(new BigDecimal("50.00"));

        when(accountRepository.findByIdWithLock(sourceId))
                .thenReturn(Optional.of(sourceAccount));

        when(accountRepository.findByIdWithLock(destId))
                .thenReturn(Optional.of(destinationAccount));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> {
                    transferService.executeTransfer("key-insufficient-balance", requestDto);
                });

        assertEquals(422, exception.getStatusCode().value());
    }

}
