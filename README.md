# Sistema de Análise de Crédito

> Sistema de análise de risco de crédito com arquitetura distribuída: API principal em Spring Boot, microsserviço de Machine Learning em FastAPI, frontend em React e persistência em PostgreSQL.

---

## Sobre o Projeto

Sistema para apoiar a decisão de aprovação de crédito, combinando um modelo de Machine Learning (probabilidade de inadimplência) com regras de negócio, autenticação e histórico de análises.

A arquitetura segue **Clean Architecture + Domain-Driven Design** nos dois backends, com um contrato HTTP explícito entre eles. O desenho completo do domínio (entidades, value objects, contratos REST, camadas) está documentado em [`docs/architecture.md`](docs/architecture.md) — este README cobre a visão geral e como rodar cada parte.

---

## Arquitetura

```
┌──────────────┐        ┌──────────────────┐        ┌──────────────────┐
│              │  HTTP  │                  │  HTTP  │                  │
│ React + Vite │───────▶│   Spring Boot    │───────▶│  FastAPI          │
│  (frontend)  │  JWT   │   (credit-api)   │        │  (ml-service)     │
│              │◀───────│                  │◀───────│  modelo de ML     │
└──────────────┘        └────────┬─────────┘        └──────────────────┘
     :5173                       │ JDBC                    :8000
                                  ▼
                         ┌──────────────────┐
                         │   PostgreSQL     │
                         │   (credito_db)   │
                         └──────────────────┘
                                :5432
```

- O **frontend** só conversa com o **Spring Boot** — nunca chama o FastAPI diretamente.
- O **Spring Boot** (`credit-api`) é dono de toda a persistência, da autenticação e das regras de negócio; chama o **FastAPI** de forma síncrona (via `WebClient`, com timeout configurado) só para obter a predição do modelo.
- O **FastAPI** (`ml-service`) é stateless, não tem banco: recebe os dados do cliente/empréstimo já validados, calcula as features derivadas e devolve a predição + versão do modelo.
- Cada serviço tem `/health` (ou `/actuator/health`) e logging estruturado em JSON, propagando um `X-Request-Id` entre eles para rastrear uma predição ponta a ponta.

---

## Estrutura do Repositório

```
Credit-Analysis-Model/
├── docs/
│   └── architecture.md        # Domínio, contratos REST, camadas (DDD/Clean Architecture)
├── services/
│   ├── credit-api/            # API principal — Spring Boot (Java 21)
│   └── ml-service/            # Microsserviço de ML — FastAPI (Python)
├── frontend/                  # React + Vite + TypeScript + Tailwind
├── load-tests/
│   └── analise-credito.js     # Teste de carga (k6)
├── modelos/
│   └── modelo_credito_final.joblib
├── notebooks/                 # Exploração de dados e treino do modelo
└── docker-compose.yaml        # PostgreSQL
```

---

## Tecnologias

| Componente | Stack |
|---|---|
| `services/credit-api` | Java 21, Spring Boot 4, Spring Security + JWT, Spring Data JPA, Flyway, WebClient, Spring Boot Actuator, Logback (JSON) |
| `services/ml-service` | Python, FastAPI, scikit-learn, joblib, structlog |
| `frontend` | React 19, TypeScript, Vite, Tailwind CSS v4, React Router |
| Banco de dados | PostgreSQL 15 |
| Testes | JUnit5 + Mockito + MockMvc (Java), pytest (Python), k6 (carga) |

---

## Pré-requisitos

- **Docker** e **Docker Compose** (para o PostgreSQL)
- **Java 21** e **Maven** (o `credit-api` já inclui o wrapper `./mvnw`)
- **Python 3.11+** (para o `ml-service`)
- **Node.js 20+** (para o `frontend`)
- **[k6](https://k6.io/)** (opcional, só para o teste de carga)

---

## Como rodar tudo localmente

### 1. Banco de dados

```bash
docker compose up -d postgres
```

### 2. Microsserviço de ML (`ml-service`)

```bash
cd services/ml-service
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --port 8000
```

Health check: `curl http://localhost:8000/health`

### 3. API principal (`credit-api`)

As migrations do Flyway rodam automaticamente na subida da aplicação.

```bash
cd services/credit-api
./mvnw spring-boot:run
```

Health check: `curl http://localhost:8080/actuator/health`

Por padrão o `credit-api` espera o `ml-service` em `http://localhost:8000` (configurável via `ML_SERVICE_URL`) e o Postgres em `localhost:5432` (configurável via `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`). Veja `services/credit-api/src/main/resources/application.yml` para todas as variáveis.

### 4. Frontend

```bash
cd frontend
npm install
npm run dev
```

Abre em **http://localhost:5173** (o Vite já vem configurado com proxy de `/auth` e `/api` para `http://localhost:8080` em desenvolvimento).

### 5. Login

Um usuário administrador é semeado via migration para permitir o primeiro acesso:

- **email:** `admin@creditanalysis.local`
- **senha:** `admin123`

> Credenciais de bootstrap para desenvolvimento local — troque a senha (ou crie um fluxo próprio de administração) antes de qualquer uso além disso. Novos usuários se cadastram via `POST /auth/register` e entram como `ANALISTA`.

---

## Autenticação

- `POST /auth/register` — cria um usuário com role `ANALISTA` (público)
- `POST /auth/login` — retorna um JWT (`Authorization: Bearer <token>`)
- Todas as rotas de negócio exigem JWT válido; `GET /api/analises/stats` exige role `ADMIN`
- Token expira em 60 minutos por padrão (`JWT_EXPIRATION_MINUTES`)

## Endpoints principais (`credit-api`)

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| POST | `/auth/register` | Cria usuário (`ANALISTA`) | Pública |
| POST | `/auth/login` | Login, retorna JWT | Pública |
| POST | `/api/analises` | Cria uma análise de crédito | `ANALISTA`, `ADMIN` |
| GET | `/api/analises` | Histórico paginado (`page`, `size`) | `ANALISTA`, `ADMIN` |
| GET | `/api/analises/{id}` | Detalhe de uma análise | `ANALISTA`, `ADMIN` |
| GET | `/api/analises/stats` | Estatísticas agregadas | `ADMIN` |
| GET | `/actuator/health` | Health check | Pública |

Contrato completo (incluindo o do `ml-service`) em [`docs/architecture.md`](docs/architecture.md).

---

## Testes

### `credit-api` (JUnit5, Mockito, MockMvc)

```bash
cd services/credit-api
./mvnw test
```

Cobre unitários dos use cases, testes de integração dos controllers (incluindo as regras de autorização por role) e o comportamento do cliente HTTP do modelo de ML sob timeout/erro.

### `ml-service` (pytest)

```bash
cd services/ml-service
pip install -r requirements-dev.txt
pytest
```

Cobre a lógica de domínio (feature engineering, decisão de threshold), o use case de predição e os endpoints via `TestClient`.

### Teste de carga (k6)

Com o `credit-api` (e o `ml-service` por trás dele) rodando:

```bash
k6 run -e ADMIN_SENHA=admin123 load-tests/analise-credito.js
```

O script faz login como `admin`, e gera carga ramping até 10 VUs simultâneos contra `POST /api/analises`, `GET /api/analises` e `GET /api/analises/stats`, com thresholds de p95/p99 configurados. Última execução: 443 iterações, 0% de falha, p95 de `POST /api/analises` (que inclui a chamada ao FastAPI) em ~29ms.

---

## Observabilidade

- **Health checks:** `GET /actuator/health` (credit-api) e `GET /health` (ml-service, verifica se o modelo está carregado)
- **Logging estruturado:** JSON em ambos os serviços (Logback + logstash-encoder no Java, structlog no Python)
- **Rastreamento ponta a ponta:** header `X-Request-Id` gerado (ou repassado) pelo `credit-api`, propagado para o `ml-service` e presente em todos os logs da requisição em ambos os serviços

---

## Modelo de Machine Learning

- **Algoritmo:** Regressão Logística com regularização L1, `RobustScaler`
- **Balanceamento:** SMOTE
- **Validação:** Stratified K-Fold Cross-Validation
- **F2-score:** 0.7456 — métrica escolhida por priorizar Recall (é mais custoso deixar passar um inadimplente do que reprovar um bom pagador)
- **Threshold otimizado:** 0.4158

### Features do modelo

Originais: idade, prazo do empréstimo, situação de moradia.
Derivadas (calculadas pelo `ml-service`): log do valor do empréstimo, log do saldo em conta corrente, indicador de conta corrente zerada, comprometimento de renda, parcela mensal estimada, renda livre mensal, cobertura de liquidez.

O artefato (`modelo_credito_final.joblib`) e seus metadados de versão (`model_metadata.json`) ficam em `services/ml-service/models/` e são expostos via `GET /model-info`.

Os notebooks de exploração e treino do modelo estão em [`notebooks/`](notebooks/).
