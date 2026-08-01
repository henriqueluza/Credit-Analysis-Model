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
