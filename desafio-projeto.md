# Desafio Técnico Backend Java — API de Movimentações Bancárias

**Nível:** Desenvolvedor(a) Backend Júnior  
**Stack principal:** Java 17+ ou 21, Spring Boot 3, Spring Web, Spring Data JPA, Bean Validation e Maven  
**Tempo sugerido:** 6 a 10 horas  
**Contexto:** exercício fictício inspirado em problemas comuns de sistemas financeiros. Não representa regras ou sistemas de uma instituição real.

---

## 1. Contexto

Você faz parte de um time responsável por uma API interna de movimentações bancárias. O time precisa de um serviço que permita consultar contas, realizar transferências entre contas e consultar o histórico de movimentações.

Sua tarefa é implementar uma API REST com Spring Boot que garanta consistência dos saldos e registre as movimentações realizadas.

O objetivo não é construir um sistema bancário completo. Queremos avaliar como você estrutura uma aplicação, modela dados, implementa regras de negócio, trata erros e testa o comportamento da API.

## 2. Objetivo

Implemente uma API que permita:

1. Consultar uma conta e seu saldo.
2. Listar as contas cadastradas.
3. Realizar uma transferência entre duas contas.
4. Consultar o extrato de uma conta.
5. Impedir transferências inválidas sem alterar os saldos.

## 3. Regras de negócio

Considere as seguintes regras obrigatórias:

- Cada conta possui um identificador único, número, titular, status e saldo.
- Uma conta só pode movimentar valores quando estiver **ATIVA**.
- O valor de uma transferência deve ser maior que zero.
- A conta de origem e a conta de destino devem ser diferentes.
- A conta de origem deve possuir saldo suficiente.
- A conta de destino deve existir e estar ativa.
- Uma transferência deve debitar a origem e creditar o destino.
- As duas alterações de saldo e o registro da movimentação devem ocorrer na mesma transação de banco de dados. Se uma etapa falhar, nenhuma alteração deve ser persistida.
- Utilize `BigDecimal` para valores monetários. Não utilize `float` ou `double`.
- Registre data/hora, valor, conta de origem, conta de destino e identificador da transferência.
- Não permita que o cliente informe diretamente o saldo final das contas.

### Idempotência

Uma mesma requisição de transferência não deve ser processada duas vezes por acidente.

Implemente idempotência usando o header `Idempotency-Key`:

- O cliente deve enviar uma chave única ao solicitar uma transferência.
- Se a mesma chave for enviada novamente com o mesmo conteúdo, a API deve retornar o resultado da transferência já processada, sem movimentar os saldos novamente.
- Se a mesma chave for reutilizada com conteúdo diferente, retorne `409 Conflict`.
- A chave deve ser persistida no banco e protegida por uma restrição de unicidade.

> Para simplificar, não é necessário implementar expiração de chaves nem armazenamento distribuído.

## 4. Endpoints esperados

Você pode propor pequenas melhorias, desde que documente as decisões e mantenha os comportamentos obrigatórios.

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/v1/accounts` | Lista contas |
| `GET` | `/api/v1/accounts/{id}` | Consulta uma conta |
| `POST` | `/api/v1/transfers` | Realiza uma transferência |
| `GET` | `/api/v1/accounts/{id}/transactions` | Consulta o extrato da conta |

### 4.1 Listar contas

`GET /api/v1/accounts`

Resposta esperada — exemplo:

```json
[
  {
    "id": 1,
    "accountNumber": "000123-4",
    "holder": "Ana Souza",
    "status": "ACTIVE",
    "balance": 1500.00
  }
]
```

### 4.2 Consultar conta

`GET /api/v1/accounts/1`

- Retorne `200 OK` quando a conta existir.
- Retorne `404 Not Found` quando não existir.

### 4.3 Realizar transferência

`POST /api/v1/transfers`

Header obrigatório:

```http
Idempotency-Key: 7c4f5c3a-2d5e-4f8a-9c1b-000000000001
```

Corpo da requisição:

```json
{
  "sourceAccountId": 1,
  "destinationAccountId": 2,
  "amount": 250.00,
  "description": "Pagamento de serviço"
}
```

Resposta sugerida — `201 Created`:

```json
{
  "transferId": "b9c4d3e2-6f10-4f9e-8a21-2a6d0b1c1234",
  "sourceAccountId": 1,
  "destinationAccountId": 2,
  "amount": 250.00,
  "description": "Pagamento de serviço",
  "status": "COMPLETED",
  "createdAt": "2026-01-15T14:30:00Z"
}
```

Você pode retornar `200 OK` ao repetir uma chave já processada, desde que não execute a movimentação novamente.

### 4.4 Consultar extrato

`GET /api/v1/accounts/{id}/transactions?page=0&size=10`

O extrato deve:

- Retornar movimentações em ordem decrescente de data/hora.
- Permitir paginação.
- Identificar se a conta foi origem ou destino.
- Retornar `404 Not Found` se a conta não existir.

Exemplo de item:

```json
{
  "transferId": "b9c4d3e2-6f10-4f9e-8a21-2a6d0b1c1234",
  "type": "DEBIT",
  "counterpartyAccountId": 2,
  "amount": 250.00,
  "description": "Pagamento de serviço",
  "createdAt": "2026-01-15T14:30:00Z"
}
```

## 5. Dados iniciais

Os arquivos em `dados/` são a massa inicial para desenvolvimento e testes:

- `accounts.csv`: contas e saldos iniciais.
- `transfer-scenarios.csv`: cenários sugeridos para validar transferências.

Você pode carregar os dados por `data.sql`, `import.sql`, CommandLineRunner ou outro mecanismo simples. Explique no README do seu projeto como inicializar a massa.

### Observação sobre os dados

Os saldos dos CSVs são dados fictícios. Em uma aplicação real, a carga inicial e a origem dos dados seriam definidas por requisitos de negócio e controles específicos.

## 6. Requisitos técnicos

### Obrigatórios

- Java 17+ (preferencialmente Java 21).
- Spring Boot 3.
- Spring Web.
- Spring Data JPA.
- Bean Validation.
- Maven.
- Banco relacional (H2 para desenvolvimento/testes ou PostgreSQL).
- Tratamento centralizado de exceções com `@RestControllerAdvice`.
- Separação mínima entre Controller, Service e Repository.
- DTOs para entrada e saída da API. Não exponha entidades JPA diretamente.
- Uso de `@Transactional` na operação de transferência.
- Testes automatizados com JUnit 5 e Mockito e, pelo menos, testes de integração dos principais fluxos.

### Não é obrigatório

- Autenticação e autorização.
- Interface web ou mobile.
- Microsserviços.
- Mensageria (Kafka/RabbitMQ).
- Kubernetes ou deploy em nuvem.
- Observabilidade avançada.

Não implemente complexidade adicional antes de concluir os requisitos obrigatórios.

## 7. Validações e respostas HTTP

Adote respostas HTTP coerentes. Sugestão:

| Situação | HTTP |
|---|---:|
| Consulta ou operação realizada | `200 OK` |
| Transferência criada | `201 Created` |
| Dados inválidos | `400 Bad Request` |
| Conta não encontrada | `404 Not Found` |
| Saldo insuficiente | `422 Unprocessable Entity` |
| Conta inativa ou conflito de idempotência | `409 Conflict` |
| Erro inesperado | `500 Internal Server Error` |

O formato de erro deve ser consistente. Exemplo:

```json
{
  "timestamp": "2026-01-15T14:30:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "code": "INSUFFICIENT_FUNDS",
  "message": "A conta de origem não possui saldo suficiente.",
  "path": "/api/v1/transfers"
}
```

Não retorne stack traces ou detalhes internos da aplicação na resposta HTTP.

## 8. Critérios de aceite

A entrega será considerada funcional quando:

- [ ] A aplicação inicia seguindo as instruções documentadas.
- [ ] É possível listar e consultar contas.
- [ ] Uma transferência válida atualiza os dois saldos corretamente.
- [ ] O extrato apresenta a movimentação para origem e destino.
- [ ] Uma transferência com saldo insuficiente não altera nenhum saldo.
- [ ] Contas inexistentes ou inativas são tratadas corretamente.
- [ ] Valores zero ou negativos são rejeitados.
- [ ] Transferência para a própria conta é rejeitada.
- [ ] A mesma `Idempotency-Key` não duplica a transferência.
- [ ] Reutilizar a chave com payload diferente retorna conflito.
- [ ] A operação é atômica (tudo ou nada).
- [ ] Há testes automatizados para os cenários principais.

## 9. Testes mínimos esperados

Implemente testes para, no mínimo:

1. Transferência válida.
2. Saldo insuficiente.
3. Conta de origem inexistente.
4. Conta de destino inexistente.
5. Conta de origem inativa.
6. Conta de destino inativa.
7. Valor igual a zero.
8. Valor negativo.
9. Origem e destino iguais.
10. Repetição da mesma chave de idempotência.
11. Reutilização da chave com payload diferente.
12. Falha durante a operação sem persistência parcial.

Nos testes de integração, valide o estado persistido no banco, não apenas o código HTTP.

## 10. Entregáveis

Disponibilize em um repositório Git:

- Código-fonte da aplicação.
- Testes automatizados.
- `README.md` com:
  - visão geral da solução;
  - pré-requisitos;
  - como executar;
  - como executar os testes;
  - endpoints e exemplos de requisição;
  - decisões técnicas e premissas.
- Arquivo de configuração de exemplo, se necessário (não envie senhas reais).
- Evidência simples de execução, como exemplos do Swagger ou coleção do Postman/Insomnia (opcional).

Não é necessário publicar a aplicação na internet.

## 11. O que será avaliado

| Critério | Peso |
|---|---:|
| Regras de negócio e consistência dos dados | 25% |
| Organização, legibilidade e qualidade do código | 20% |
| Modelagem e persistência | 15% |
| Testes automatizados | 20% |
| Tratamento de erros e validações | 10% |
| Documentação e instruções de execução | 10% |

Serão observados especialmente:

- Clareza na divisão de responsabilidades.
- Uso correto de transações.
- Cuidado com concorrência e atualização de saldo.
- Precisão no tratamento de valores monetários.
- Testes que comprovem comportamento, inclusive falhas.
- Decisões técnicas explicadas com objetividade.

## 12. Diferenciais (opcionais)

Faça somente depois de concluir o escopo obrigatório:

- Documentação OpenAPI/Swagger.
- Docker Compose para subir a aplicação e o PostgreSQL.
- Testcontainers para testes de integração.
- Controle de concorrência otimista com `@Version` ou estratégia equivalente.
- Logs estruturados com identificador de correlação.
- Pipeline simples de CI para executar os testes.

## 13. Como começar

Uma sequência recomendada:

1. Leia os requisitos e identifique as entidades e regras.
2. Desenhe rapidamente o modelo de dados.
3. Crie o projeto Spring Boot e configure o banco.
4. Implemente consulta de contas.
5. Implemente a transferência dentro de uma transação.
6. Adicione validações e tratamento global de erros.
7. Implemente extrato e paginação.
8. Implemente idempotência.
9. Escreva testes e valide os critérios de aceite.
10. Finalize a documentação.

**Importante:** não tente resolver tudo de uma vez. Priorize uma implementação pequena, correta e testável. O desafio avalia fundamentos de backend, não a quantidade de tecnologias utilizadas.
