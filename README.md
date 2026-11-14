# Desafio Backend Java — API de Movimentações Bancárias - Em Andamento

[![CI](https://github.com/ThaisAlves7/banking-transactions-api/actions/workflows/ci.yml/badge.svg)](https://github.com/ThaisAlves7/banking-transactions-api/actions/workflows/ci.yml)

API REST para gerenciamento de contas bancárias e realização de transferências entre contas, desenvolvida com Java 21 e Spring Boot 4.

>Exercício fictício inspirado em problemas comuns de sistemas financeiros. Não representa regras ou sistemas de uma instituição real, apenas um projeto base para consolidar conhecimento

## Índice

- [Visão geral](#visão-geral)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Funcionalidades](#funcionalidades)
- [Regras de negócio](#regras-de-negócio)
- [Pré-requisitos](#pré-requisitos)
- [Executando com Docker](#executando-com-docker)
- [Executando localmente](#executando-localmente)
- [Banco de dados e migrações](#banco-de-dados-e-migrações)
- [Endpoints](#endpoints)
- [Tratamento de erros](#tratamento-de-erros)
- [Testes](#testes)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Descrição das camadas](#descrição-das-camadas)

## Visão geral

O **API de Movimentações Bancárias** disponibiliza operações básicas de consulta de contas e transferência de valores entre contas bancárias. A aplicação valida regras de negócio, persiste as movimentações em PostgreSQL e utiliza chaves de idempotência para permitir a repetição segura de requisições em cenários de dupla tentativa.

O projeto foi desenvolvido como exercício prático de engenharia backend, para demonstração de conhecimentos com Java e Spring Boot, incluindo construção de APIs REST, modelagem relacional, persistência de dados, validação de regras de negócio, controle transacional, tratamento de exceções e execução com Docker priorizando separação de responsabilidades, consistência transacional e evolução incremental.

## Tecnologias

- **Java 21**
- **Spring Boot 4.1.2**
- **Maven**
- **Spring Web**
- **Spring Data JPA**
- **PostgreSQL**
- **Flyway**
- **Docker e Docker Compose**

## Arquitetura

A aplicação segue uma organização em camadas:

- **Controller:** recebe as requisições HTTP, valida entradas e define as respostas.
- **Service:** concentra as regras de negócio e coordena as operações.
- **Repository:** realiza o acesso e a persistência dos dados.
- **Entity:** representa as estruturas persistidas no banco.
- **DTO:** define os contratos de entrada e saída da API.
- **Exception:** centraliza o tratamento e a padronização de erros.
- **Tests**: contém os testes automatizados responsáveis por validar o comportamento da aplicação e suas regras de negócio

O fluxo principal de uma transferência é:

1. A API recebe os dados da transferência.
2. O serviço verifica a chave de idempotência, quando informada.
3. São validadas as contas de origem e destino e as condições da operação.
4. O saldo é atualizado e a transferência é registrada.
5. A operação é confirmada pela transação do banco de dados.
6. A API retorna os dados da transferência.

## Funcionalidades

- Consultar uma conta e seu saldo
- Listar as contas cadastradas.
- Realizar uma transferência entre duas contas.
- Consultar o extrato de uma conta
- Validação de contas de origem e destino.
- Validação de status das contas.
- Impedir transferência inválidas sem alterar os saldos.
- Registro das transferências realizadas.
- Obrigatoriedade `Idempotency-Key` em requisições de transferência.
- Tratamento global de exceções.
- Versionamento do esquema do banco com Flyway.

## Regras de negócio

| Regra | Comportamento |
| --- | --- |
| Identificação da conta | Cada conta deve possuir identificador único, número, titular, status e saldo. |
| Status da conta | Apenas contas com status **`ATIVA`** podem realizar movimentações. |
| Valor da transferência | O valor transferido deve ser **maior que zero**. |
| Contas distintas | A conta de origem e a conta de destino devem ser diferentes. |
| Saldo suficiente | A conta de origem deve possuir saldo suficiente para realizar a transferência. |
| Conta de destino | A conta de destino deve existir e estar ativa. |
| Movimentação de saldo | A transferência deve debitar o valor da conta de origem e creditar o mesmo valor na conta de destino. |
| Atomicidade da operação | A atualização dos dois saldos e o registro da transferência devem ocorrer na mesma transação. Caso alguma etapa falhe, nenhuma alteração deve ser persistida. |
| Precisão monetária | Utilizar **`BigDecimal`** para representar valores monetários. Não utilizar `float` ou `double`. |
| Registro da transferência | Registrar data/hora, valor, conta de origem, conta de destino e identificador único da transferência. |
| Atualização de saldo | O cliente não deve informar diretamente o saldo final das contas. O saldo deve ser atualizado exclusivamente pelas operações permitidas pela API. |
| Chave de idempotência | O cliente deve enviar uma chave única por requisição de transferência, utilizando o header **`Idempotency-Key`**. |
| Requisição duplicada | Se a mesma chave for enviada novamente com o mesmo conteúdo, a API deve retornar o resultado da transferência original, sem movimentar os saldos novamente. |
| Reutilização indevida da chave | Se uma chave já utilizada for enviada com conteúdo diferente, a API deve retornar **`409 Conflict`**. |
| Persistência da chave | A chave de idempotência deve ser persistida no banco de dados e protegida por uma restrição de unicidade. |

## Pré-requisitos

Para executar a aplicação localmente, tenha instalado:

- JDK 21
- Docker e Docker Compose (para execução conteinerizada)
- Git

O Maven Wrapper (`mvnw` / `mvnw.cmd`) está disponível no repositório, portanto não é necessário instalar o Maven separadamente para os comandos demonstrados.

## Executando com Docker

Clone o repositório:

``` bash
git clone https://github.com/ThaisAlves7/banking-transactions-api.git
cd banking-transactions-api
```

Suba os serviços:

``` bash
docker compose up -d postegres
```

Com a configuração padrão do projeto:

- API: `http://localhost:8082`
- PostgreSQL: `localhost:5434`
- Banco de dados: `movimentacoes_bancarias`
- Usuário: `api_bank`

Para interromper os serviços:

``` bash
docker compose down
```

Para remover também os volumes persistidos (incluindo os dados do
banco):

``` bash
docker compose down -v
```

> Atenção: o comando `docker compose down -v` apaga os dados armazenados
> no volume do PostgreSQL.

## Executando localmente

Se preferir executar a aplicação fora do Docker, inicie primeiro uma
instância PostgreSQL acessível pela configuração da aplicação.

Na configuração padrão, a aplicação espera o banco em:

``` text
jdbc:postgresql://localhost:5434/movimentacoes_bancarias
```

Execute a aplicação no Windows:

``` powershell
.\mvnw.cmd spring-boot:run
```

Ou em Linux/macOS:

``` bash
./mvnw spring-boot:run
```

A API ficará disponível em `http://localhost:8082`.

## Banco de dados e migrações

O PostgreSQL é utilizado como banco de dados relacional. O Flyway controla a evolução do esquema por meio de scripts versionados em:

``` text
src/main/resources/db/migration
```

Migrações identificadas no projeto:

- **`V1__create_tables.sql`**: criação das tabelas `accounts`, `transfers`   e `idempotency_keys`, além de índices relacionados às   transferências.
- **`V2__insert_initial_accounts.sql`**: inclusão de contas iniciais para  utilização no ambiente de desenvolvimento.
- **`V3__add_request_hash_to_idempotency.sql`**: adiciona a coluna `request_hash` à tabela de idempotência para armazenar o hash do conteúdo da requisição, permitindo validar se uma `Idempotency-Key` está sendo reutilizada com os mesmos dados.

O Hibernate está configurado com `ddl-auto: validate`. Dessa forma, a aplicação valida o mapeamento das entidades em relação ao esquema existente, enquanto a criação e evolução do esquema ficam sob responsabilidade do Flyway.

## Endpoints

A URL base da API é:

``` text
http://localhost:8082/api/v1
```

### Contas

| Método | Endpoint | Descrição |
| --- | --- | --- |
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
        "id": 5,
        "accountNumber": "001112-2",
        "holder": "Elisa Costa",
        "status": "ACTIVE",
        "balance": 12500.75
    }
]
```

### 4.2 Consultar conta

**`GET /api/v1/accounts/1`**

- Retorne `200 OK` quando a conta existir.
- Retorne `404 Not Found` quando não existir.

### 4.3 Realizar transferência

**`POST /api/v1/transfers`**

Header obrigatório:

```http
Idempotency-Key: TRANSFER-0002
```

Corpo da requisição:

```json
{
  "sourceAccountId": 5,
  "destinationAccountId": 4,
  "amount": 50.00,
  "description": "PIX"

}
```

Resposta sugerida — `201 Created`:

```json
{
    "transferId": "67de18d7-41de-46c0-ac60-62ebd9d06bcf",
    "sourceAccountId": 5,
    "destinationAccountId": 4,
    "amount": 50.00,
    "description": "PIX",
    "status": "COMPLETED",
    "createdAt": "2026-11-13T23:10:49.6272726-03:00"
}
```

### 4.4 Consultar extrato

**`GET /api/v1/accounts/{id}/transactions?page=0&size=10`**

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

## Tratamento de erros

As exceções são centralizadas por meio de `GlobalExceptionHandler`, que
padroniza as respostas da API.

O formato de erro contém:

``` json
{
    "timestamp": "2026-11-13T23:13:10.1386961-03:00",
    "status": 422,
    "error": "Unprocessable Content",
    "code": "GENERAL_ERROR",
    "message": "Saldo insuficiente na conta origem",
    "path": "/api/v1/transfers"

}
```

## Validações e respostas HTTP

Adote respostas HTTP coerentes. Sugestão:

| Situação | HTTP |
| --- | ---: |
| Consulta ou operação realizada | `200 OK` |
| Transferência criada | `201 Created` |
| Dados inválidos | `400 Bad Request` |
| Conta não encontrada | `404 Not Found` |
| Saldo insuficiente | `422 Unprocessable Entity` |
| Conta inativa ou conflito de idempotência | `409 Conflict` |
| Erro inesperado | `500 Internal Server Error` |

## Testes

Execute os testes automatizados com o Maven Wrapper:

Windows:

``` powershell
.\mvnw.cmd test
```

Linux/macOS:

``` bash
./mvnw test
```

Para gerar o pacote:

``` bash
./mvnw clean package
```

## Estrutura do projeto

```
banking-transactions-api/
│
├── .github/
│   └── workflows/
│       └── maven.yml
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/desafio/banking_transactions_api/
│   │   │       │
│   │   │       ├── controller/
│   │   │       │   ├── AccountController.java
│   │   │       │   └── TransferController.java
│   │   │       │
│   │   │       ├── database/
│   │   │       │   ├── model/
│   │   │       │   │   ├── AccountEntity.java
│   │   │       │   │   ├── TransferEntity.java
│   │   │       │   │   └── IdempotencyEntity.java
│   │   │       │   │
│   │   │       │   └── repository/
│   │   │       │       ├── AccountRepository.java
│   │   │       │       ├── TransferRepository.java
│   │   │       │       └── IdempotenciaRepository.java
│   │   │       │
│   │   │       ├── dto/
│   │   │       │   ├── AccountResponseDto.java
│   │   │       │   ├── TransferRequestDto.java
│   │   │       │   └── TransferResponseDto.java
│   │   │       │
│   │   │       ├── enums/
│   │   │       │   └── AccountStatus.java
│   │   │       │
│   │   │       ├── exception/
│   │   │       │   └── GlobalExceptionHandler.java
│   │   │       │
│   │   │       ├── service/
│   │   │       │   ├── AccountService.java
│   │   │       │   └── TransferService.java
│   │   │       │
│   │   │       └── BankingTransactionsApiApplication.java
│   │   │
│   │   └── resources/
│   │       ├── db/
│   │       │   └── migration/
│   │       │       └── V1__initial_schema.sql
│   │       │
│   │       └── application.yaml
│   │
│   └── test/
│       └── java/
│           └── com/desafio/banking_transactions_api/
│               │
│               ├── service/
│               │   └── TransferServiceTest.java
│               │
│               └── BankingTransactionsApiApplicationTests.java
│
├── .gitignore
├── Dockerfile
├── compose.yaml
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## Descrição das camadas

| Diretório | Responsabilidade |
| --- | --- |
| `controller/` | Exposição dos endpoints REST e recebimento das requisições HTTP. |
| `database/model/` | Entidades JPA que representam as tabelas do banco de dados. |
| `database/repository/` | Interfaces responsáveis pelo acesso e persistência dos dados. |
| `dto/` | Objetos utilizados para entrada e saída de dados da API. |
| `enums/` | Enumerações utilizadas para representar estados e valores definidos. |
| `exception/` | Tratamento centralizado de exceções e padronização das respostas de erro. |
| `service/` | Implementação das regras de negócio e orquestração das operações. |
| `resources/db/migration/` | Scripts SQL versionados e gerenciados pelo Flyway. |
| `test/service/` | Testes unitários das regras de negócio dos serviços. |
