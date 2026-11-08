# Exemplos rápidos — API de Movimentações

> Os exemplos assumem a aplicação em `http://localhost:8080`.

## Listar contas

```bash
curl http://localhost:8080/api/v1/accounts
```

## Consultar conta

```bash
curl http://localhost:8080/api/v1/accounts/1
```

## Realizar transferência

```bash
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: 7c4f5c3a-2d5e-4f8a-9c1b-000000000001" \
  -d '{
    "sourceAccountId": 1,
    "destinationAccountId": 2,
    "amount": 250.00,
    "description": "Pagamento de serviço"
  }'
```

## Consultar extrato

```bash
curl "http://localhost:8080/api/v1/accounts/1/transactions?page=0&size=10"
```
