# Arquitetura — Sistema de Análise de Crédito

## Visão geral

Este documento descreve o bounded context **Análise de Crédito** e a arquitetura
alvo da migração do sistema, hoje um monólito FastAPI + Streamlit, para:

- **Spring Boot (Java)** — API principal, dona das regras de negócio e da persistência.
- **FastAPI (Python)** — microsserviço interno, stateless, que serve exclusivamente o
  modelo de Machine Learning.
- **PostgreSQL** — banco de dados, de propriedade exclusiva do Spring Boot.
- **React + Tailwind** — frontend, substituindo o Streamlit.

Nenhum código é escrito antes deste documento estar completo, pois ele define o
contrato entre os serviços e a estrutura de camadas que ambos devem seguir.

### Bounded context: Análise de Crédito

O contexto cobre todo o fluxo de "um analista submete os dados de um cliente e de
um pedido de empréstimo, e o sistema retorna uma decisão de risco (aprovado ou
reprovado) com a probabilidade associada". Isso inclui:

- Cadastro e autenticação de usuários (analistas/administradores) que operam o sistema.
- Submissão de uma solicitação de análise de crédito.
- Cálculo da probabilidade de risco via modelo de ML.
- Persistência e consulta do histórico de análises.
- Estatísticas agregadas sobre as análises realizadas.

O cálculo do modelo de ML em si (feature engineering, threshold, inferência) é
tratado como um **subdomínio de suporte** (MLOps), isolado no microsserviço FastAPI,
consumido pelo Spring Boot através de um contrato HTTP bem definido — nunca acessado
diretamente pelo frontend.

## Modelo de domínio (Spring Boot)

O agregado raiz do contexto é `SolicitacaoEmprestimo`. Ele é o único ponto de
entrada para modificações consistentes do agregado (nenhuma outra entidade é
acessada ou persistida isoladamente a partir de fora do agregado).

### `Cliente` — Value Object

Representa o perfil financeiro do solicitante **no momento da análise**. É imutável
e comparado por valor — o sistema atual não modela um cadastro de cliente com
identidade própria (não há CPF/CNPJ ou tabela de clientes), então não há motivo para
tratá-lo como Entity. Se no futuro for necessário rastrear o histórico de um mesmo
cliente ao longo de várias solicitações, ele é promovido a Entity nesse momento —
mas isso está fora do escopo desta migração.

Atributos:
- `idade: int` (≥ 18, ≤ 120)
- `salarioAnual: BigDecimal` (≥ 0)
- `situacaoMoradia: SituacaoMoradia` (enum VO: `OWN`, `RENT`, `FREE`)
- `saldoContaCorrente: BigDecimal` (≥ 0)
- `saldoContaPoupanca: BigDecimal` (≥ 0)

### `SolicitacaoEmprestimo` — Entity / Aggregate Root

Representa um pedido de análise de crédito. Tem identidade própria (`id`), é criado
por um usuário autenticado (`solicitadoPor`, referência ao `Usuario`) e contém um
`Cliente` (VO) e os dados do empréstimo pedido.

Atributos:
- `id: Long`
- `cliente: Cliente` (VO)
- `valorEmprestimo: BigDecimal` (> 0)
- `prazoMeses: int` (> 0, ≤ 360)
- `solicitadoPor: UsuarioId`
- `criadoEm: Instant`
- `resultado: ResultadoAnalise` (VO, nulo até a análise ser processada)

Regra de negócio que vive no domínio: validação de consistência dos dados de
entrada (idade, prazo, valores) — a mesma validação hoje feita nos `field_validator`
do Pydantic em `app/backend/main.py`, mas reimplementada como invariante do
agregado, não como validação de borda de um framework.

### `ResultadoAnalise` — Value Object

Representa o resultado, imutável, de uma análise processada pelo subdomínio de ML.

Atributos:
- `resultado: StatusAnalise` (enum VO: `APROVADO`, `REPROVADO`)
- `probabilidadeRisco: BigDecimal` (0–1)
- `thresholdUtilizado: BigDecimal` (0–1)
- `versaoModelo: String` (nova informação, não existente hoje — necessária para
  rastreabilidade/MLOps: qual versão do modelo gerou este resultado)
- `analisadoEm: Instant`

### `Usuario` — Entity

Necessário para autenticação (etapa 5), mas definido aqui pois faz parte do modelo
de domínio geral.

Atributos:
- `id: Long`
- `nome: String`
- `email: String` (único)
- `senhaHash: String`
- `role: Role` (enum VO: `ANALISTA`, `ADMIN`)
- `criadoEm: Instant`

## Contrato REST — Spring Boot → FastAPI (microsserviço de ML)

### Decisão de design: o que significa "features já processadas"

O FastAPI é rebaixado a um serviço stateless que só sabe fazer uma coisa: dado um
perfil de cliente e um pedido de empréstimo, calcular a probabilidade de risco.
Duas opções foram consideradas:

1. Spring calcula toda a engenharia de atributos (log-transforms, razões,
   one-hot encoding) e manda o vetor de features pronto para o modelo.
2. Spring manda os dados **brutos, porém validados e tipados** do cliente/empréstimo,
   e o FastAPI mantém a lógica de feature engineering na sua própria camada de
   domínio (`domain/`, regra de negócio pura de ML).

Optamos pela **opção 2**. Motivo: a etapa 3 exige que o microsserviço FastAPI tenha
uma camada `domain/` com "regras puras de feature engineering e threshold" — ou seja,
esse conhecimento (quais features derivar, como) é responsabilidade do subdomínio de
ML, não do Spring Boot. "Features já processadas" significa que o Spring já fez
**validação e tipagem** (não envia strings soltas nem valores fora de domínio) —
mas o cálculo das features derivadas do modelo (`log_valor_emprestimo`,
`comprometimento_renda`, etc.) continua sendo responsabilidade interna do FastAPI,
igual é hoje em `preparar_dados_modelo()`. Isso mantém o contrato estável mesmo que
o modelo troque de features no futuro — o Spring não precisa saber quais são.

### `POST /predict`

Request body:
```json
{
  "idade": 35,
  "salarioAnual": 60000.0,
  "situacaoMoradia": "OWN",
  "saldoContaCorrente": 1500.0,
  "saldoContaPoupanca": 5000.0,
  "valorEmprestimo": 10000.0,
  "prazoMeses": 24
}
```

Response body (200):
```json
{
  "resultado": "APROVADO",
  "probabilidadeRisco": 0.1234,
  "thresholdUtilizado": 0.35,
  "versaoModelo": "1.2.0"
}
```

Erros:
- `422` — payload inválido (mesmas invariantes já validadas no Spring, validadas de
  novo aqui porque o FastAPI não deve confiar cegamente em quem o chama).
- `503` — modelo ainda não carregado em memória.

### `GET /model-info`

Expõe os metadados de versionamento do modelo (etapa 3 — MLOps básico).

Response body (200):
```json
{
  "versaoModelo": "1.2.0",
  "dataTreino": "2026-05-10",
  "metricas": { "f2_score": 0.81, "auc_roc": 0.87 },
  "features": ["idade", "log_valor_emprestimo", "..."]
}
```

### `GET /health`

Response body (200): `{ "status": "UP", "modeloCarregado": true }`.
Response body (503): `{ "status": "DOWN", "modeloCarregado": false }`.

### Propagação de request-id

Toda chamada Spring → FastAPI propaga o header `X-Request-Id` (gerado pelo Spring
na entrada da requisição do cliente, ou recebido dele). O FastAPI inclui o mesmo
valor em todo log estruturado da requisição, permitindo rastrear uma predição
ponta a ponta (detalhado na etapa 6).

## Camadas — Clean Architecture (ambos os serviços)

Regra de dependência única, em ambos os serviços: **as camadas internas nunca
conhecem as externas**. `domain` não importa nada de `application`, `infrastructure`
ou `interface`. `application` só conhece `domain`. `infrastructure` e `interface`
podem conhecer `application` e `domain`, nunca uma à outra diretamente — a
composição (wiring) é feita por injeção de dependência na borda do framework.

```
interface  ──depends on──▶  application  ──depends on──▶  domain
infrastructure ──implements ports defined in──▶  domain / application
```

### FastAPI (microsserviço de ML)

- **`domain/`** — regras puras, sem nenhuma dependência de FastAPI/Pydantic/pandas
  além do necessário para tipos. Contém:
  - `FeaturesEmprestimo` (VO com os dados de entrada validados)
  - `calcular_features_derivadas()` — a lógica hoje em `preparar_dados_modelo()`
  - `decidir_resultado(probabilidade, threshold)` — a lógica de decisão hoje
    inline em `predict_credit()`
  - Interface (port) `ModeloPreditivo` — abstrai "algo que recebe features e
    devolve uma probabilidade", implementado pela infraestrutura.
- **`application/`** — `PredizerRiscoUseCase`: orquestra `domain` (calcula
  features, chama a porta `ModeloPreditivo`, aplica a decisão de threshold) e
  retorna um DTO de resultado. Não sabe nada de HTTP.
- **`infrastructure/`** — `ModeloJoblibRepository` (implementa `ModeloPreditivo`
  carregando o `.joblib`), leitura de metadados do modelo (versão, data de treino,
  métricas), configuração/logging.
- **`interface/`** — routers FastAPI (`/predict`, `/model-info`, `/health`),
  schemas Pydantic de request/response, tratamento de exceções HTTP. É a única
  camada que sabe o que é FastAPI.

### Spring Boot (API principal)

- **`domain/`** — `SolicitacaoEmprestimo`, `Cliente`, `ResultadoAnalise`,
  `Usuario` e demais VOs/enums descritos acima, mais as portas (interfaces) que a
  infraestrutura implementa: `SolicitacaoEmprestimoRepository`,
  `UsuarioRepository`, `ModeloCreditoClient` (porta para o FastAPI). Sem
  anotações JPA, sem Spring, sem nada de infraestrutura.
- **`application/`** — casos de uso, um por operação de negócio:
  `AnalisarCreditoUseCase` (monta `SolicitacaoEmprestimo`, chama
  `ModeloCreditoClient`, persiste via `SolicitacaoEmprestimoRepository`),
  `ListarHistoricoUseCase`, `ObterEstatisticasUseCase`,
  `AutenticarUsuarioUseCase`, `RegistrarUsuarioUseCase`. Recebem e devolvem
  objetos de domínio ou DTOs de aplicação — nunca entidades JPA nem DTOs de HTTP.
- **`infrastructure/`** — `SolicitacaoEmprestimoJpaRepository` e
  `UsuarioJpaRepository` (Spring Data JPA, implementando as portas do domínio via
  adapters), `ModeloCreditoWebClient` (implementa `ModeloCreditoClient` chamando o
  FastAPI via `WebClient`), configuração do JWT, configuração do Flyway/DataSource.
- **`interface/`** (camada web) — `@RestController`s, DTOs de request/response
  HTTP, `@ExceptionHandler`s, filtros de segurança (JWT). Traduz HTTP ↔ objetos de
  aplicação/domínio e nada mais.

## Contrato REST — Spring Boot → Frontend (React)

Esta é a única API que o frontend consome; o FastAPI nunca é acessado diretamente
pelo browser. Todas as rotas exceto `/auth/*` e `/health` exigem JWT válido
(`Authorization: Bearer <token>`).

### `POST /auth/register`
Cria um usuário. Body: `{ "nome", "email", "senha" }`. Role padrão: `ANALISTA`
(criação de `ADMIN` é feita apenas por outro `ADMIN`, protegida por role).
Response `201`: `{ "id", "nome", "email", "role" }`.

### `POST /auth/login`
Body: `{ "email", "senha" }`.
Response `200`: `{ "token", "tipo": "Bearer", "expiraEm": "2026-08-01T18:00:00Z" }`.
Response `401`: credenciais inválidas.

### `POST /api/analises`
Cria e processa uma nova análise de crédito (chama o FastAPI internamente).
Roles: `ANALISTA`, `ADMIN`.
Body: os mesmos campos de `Cliente` + dados do empréstimo, em português, tal como
hoje no `ClienteInput` do Streamlit/FastAPI (`idade`, `salarioAnual`,
`situacaoMoradia`, `saldoContaCorrente`, `saldoContaPoupanca`, `valorEmprestimo`,
`prazoMeses`).
Response `201`: a `SolicitacaoEmprestimo` criada, incluindo `ResultadoAnalise`.
Response `502`: FastAPI indisponível ou retornou erro.

### `GET /api/analises`
Lista paginada do histórico. Roles: `ANALISTA`, `ADMIN`.
Query params: `page` (default 0), `size` (default 10, máx 100), `sort` (opcional).
Response `200`: `{ "content": [...], "page", "size", "totalElements", "totalPages" }`
— formato padrão `Page<T>` do Spring Data, substituindo o `{total, limit, skip}`
ad-hoc de hoje.

### `GET /api/analises/{id}`
Detalhe de uma análise. Roles: `ANALISTA`, `ADMIN`. `404` se não encontrada.

### `GET /api/analises/stats`
Estatísticas agregadas (total, aprovados, reprovados, taxa de aprovação). Role:
`ADMIN` (informação sensível de negócio, mais restrita que o histórico individual).

### `GET /health`
Público, sem autenticação — Spring Boot Actuator (`/actuator/health` exposto
como `/health`, ou mapeado). Usado por orquestração/monitoramento.

## Resumo das mudanças de contrato em relação ao sistema atual

| Hoje (FastAPI monólito) | Alvo |
|---|---|
| `POST /predict` (público, sem auth) | `POST /api/analises` no Spring (com JWT) → Spring chama `POST /predict` no FastAPI internamente |
| `GET /predictions` | `GET /api/analises` (paginação no formato `Page<T>`) |
| `GET /predictions/{id}` | `GET /api/analises/{id}` |
| `GET /predictions/stats` | `GET /api/analises/stats` (restrito a `ADMIN`) |
| Sem autenticação | JWT obrigatório em todas as rotas de negócio |
| Sem versionamento de modelo | `versaoModelo` em todo `ResultadoAnalise`, exposto via `GET /model-info` |
| Sem `/health` | `/health` em ambos os serviços |
