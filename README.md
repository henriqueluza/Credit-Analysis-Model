# Sistema de Análise de Crédito com Machine Learning

> Sistema completo de análise de risco de crédito utilizando Machine Learning, API REST, interface web interativa e persistência de dados.

---

## Sobre o Projeto

Sistema de análise de risco de crédito desenvolvido para auxiliar instituições financeiras na tomada de decisão sobre concessão de empréstimos. Utiliza um modelo de Machine Learning treinado com técnicas de classificação para prever a probabilidade de inadimplência de clientes.

### Principais Diferenciais

-  **Machine Learning**: Modelo de Regressão Logística com F2-score de 0.74
-  **API REST**: Backend robusto com FastAPI e documentação automática (Swagger)
-  **Interface Intuitiva**: Frontend interativo desenvolvido com Streamlit
-  **Persistência de Dados**: Banco PostgreSQL com histórico completo de análises
-  **Containerização**: Deploy facilitado com Docker Compose
-  **Boas Práticas**: Variáveis de ambiente, validação de dados, tratamento de erros

---

## Funcionalidades

### Análise de Crédito
- Avaliação de risco baseada em múltiplas variáveis (idade, renda, patrimônio, etc.)
- Decisão automática de aprovação/reprovação com threshold otimizado
- Cálculo de probabilidade de inadimplência

### Dashboard e Histórico
- Visualização de todas as análises realizadas com filtros e paginação
- Estatísticas gerais (taxa de aprovação, total de análises, etc.)
- Busca de análises específicas por ID
- Gráficos e métricas consolidadas

### API REST
- Documentação automática com Swagger UI
- Endpoints RESTful para integração com outros sistemas
- Validação automática de dados com Pydantic
- Tratamento robusto de erros

###  Persistência
- Armazenamento de todas as predições em PostgreSQL
- Histórico completo com timestamps
- Dados estruturados para análises futuras

---

## Tecnologias

### Backend
- **[FastAPI](https://fastapi.tiangolo.com/)** - Framework web moderno e rápido
- **[Pydantic](https://pydantic-docs.helpmanual.io/)** - Validação de dados
- **[SQLAlchemy](https://www.sqlalchemy.org/)** - ORM para PostgreSQL
- **[Uvicorn](https://www.uvicorn.org/)** - Servidor ASGI

### Frontend
- **[Streamlit](https://streamlit.io/)** - Interface web interativa
- **[Pandas](https://pandas.pydata.org/)** - Manipulação de dados
- **[Requests](https://requests.readthedocs.io/)** - Cliente HTTP

### Machine Learning
- **[scikit-learn](https://scikit-learn.org/)** - Modelagem e pipeline
- **[NumPy](https://numpy.org/)** - Computação numérica
- **[imbalanced-learn](https://imbalanced-learn.org/)** - Tratamento de desbalanceamento (SMOTE)

### Infraestrutura
- **[PostgreSQL](https://www.postgresql.org/)** - Banco de dados relacional
- **[Docker](https://www.docker.com/)** - Containerização
- **[Docker Compose](https://docs.docker.com/compose/)** - Orquestração de containers

---

## Arquitetura

```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│                 │      │                 │      │                 │
│   Streamlit     │─────▶│   FastAPI       │─────▶│   PostgreSQL    │
│   (Frontend)    │ HTTP │   (Backend)     │ SQL  │   (Database)    │
│                 │◀─────│   + ML Model    │◀─────│                 │
│                 │ JSON │                 │      │                 │
└─────────────────┘      └─────────────────┘      └─────────────────┘
     Port 8501                Port 8000                Port 5432
         │                        │                        │
         └────────────────────────┴────────────────────────┘
                         Docker Compose
```

### Fluxo de Dados

1. **Usuário** preenche formulário no Streamlit
2. **Frontend** envia requisição POST para `/predict`
3. **Backend** valida dados, aplica feature engineering
4. **Modelo ML** calcula probabilidade de inadimplência
5. **Backend** aplica threshold e decide aprovação/reprovação
6. **PostgreSQL** armazena predição com timestamp
7. **Frontend** exibe resultado formatado ao usuário

---

## Pré-requisitos

- **Python** 3.10 ou superior
- **Docker** e **Docker Compose** (para PostgreSQL)
- **Git** (para clonar o repositório)

---

## Instalação

### 1. Clone o repositório

```bash
git clone https://github.com/henriqueluza/Credit-Analysis-Model.git
cd Credit-Analysis-Model
```

### 2. Crie um ambiente virtual

```bash
# Linux/Mac
python3 -m venv venv
source venv/bin/activate

# Windows
python -m venv venv
venv\Scripts\activate
```

### 3. Instale as dependências

```bash
pip install -r requirements.txt
```

### 4. Configure as variáveis de ambiente

```bash
# Copie o arquivo de exemplo
cp .env.example .env

# Edite o .env com suas credenciais (opcional)
# Por padrão já vem configurado para funcionar com o docker-compose
```

### 5. Suba o banco de dados PostgreSQL

```bash
docker-compose up -d
```

**Aguarde ~10 segundos** para o PostgreSQL inicializar completamente.

### 6. Verifique se o banco está rodando

```bash
docker ps | grep postgres
# Deve mostrar: postgres-credito com STATUS = Up
```

---

## Como Usar

### Iniciar o Backend (API)

```bash
# Na raiz do projeto
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

Acesse a documentação interativa em: **http://localhost:8000/docs**

### Iniciar o Frontend

**Em outro terminal:**

```bash
streamlit run app.py
```

Abre automaticamente em: **http://localhost:8501**

### Usar a Interface Web

1. **Nova Análise**: Preencha os dados do cliente e clique em "Avaliar Crédito"
2. **Histórico**: Visualize todas as análises realizadas com cores e filtros
3. **Estatísticas**: Veja métricas consolidadas e gráficos

---

##  API Endpoints

### POST `/predict`
Realiza análise de crédito e retorna decisão

**Request Body:**
```json
{
  "idade": 35,
  "valor_conta_poupanca": 50000.0,
  "valor_conta_corrente": 15000.0,
  "salario_anual": 120000.0,
  "valor_emprestimo": 30000.0,
  "prazo_meses": 48,
  "situacao_moradia": "own"
}
```

**Response:**
```json
{
  "resultado": "Aprovado",
  "probabilidade_risco": 0.0192,
  "threshold_utilizado": 0.4158,
  "prediction_id": 1
}
```

### GET `/predictions`
Retorna histórico de predições com paginação

**Query Parameters:**
- `limit`: Número de resultados (padrão: 10, máx: 100)
- `skip`: Offset para paginação (padrão: 0)

**Response:**
```json
{
  "total": 50,
  "limit": 10,
  "skip": 0,
  "predictions": [...]
}
```

### GET `/predictions/{prediction_id}`
Busca uma predição específica por ID

### GET `/predictions/stats`
Retorna estatísticas gerais

**Response:**
```json
{
  "total_predicoes": 50,
  "aprovados": 32,
  "reprovados": 18,
  "taxa_aprovacao": 64.0
}
```

---

## Estrutura do Projeto

```
Credit-Analysis-Model/
│
├── main.py                     # Backend FastAPI + lógica de ML
├── app.py                      # Frontend Streamlit
├── docker-compose.yml          # Orquestração PostgreSQL
├── requirements.txt            # Dependências Python
├── .env                        # Variáveis de ambiente (não versionado)
├── .env.example                # Template de variáveis de ambiente
├── .gitignore                  # Arquivos ignorados pelo Git
│
├── modelos/
│   └── modelo_credito_final.joblib  # Modelo treinado + threshold
│
├── notebooks/
│   ├── 01_exploratory_analysis.ipynb
│   ├── 02_feature_engineering.ipynb
│   └── 03_machine_learning.ipynb
│
├── data/
│   └── dados_credito_processados.parquet
│
└── README.md
```

---

## Modelo de Machine Learning

### Abordagem

- **Algoritmo**: Regressão Logística com regularização L2
- **Balanceamento**: SMOTE (Synthetic Minority Over-sampling Technique)
- **Escalonamento**: RobustScaler (resistente a outliers)
- **Validação**: Stratified K-Fold Cross-Validation (5 folds)

### Features Utilizadas (11 variáveis)

**Features Originais:**
- Idade
- Prazo do empréstimo (meses)
- Situação de moradia (própria/aluguel/graça)

**Features Derivadas (Feature Engineering):**
- Log do valor do empréstimo
- Log do valor em conta corrente
- Indicador de conta corrente zerada
- Comprometimento de renda (%)
- Parcela mensal estimada
- Renda livre mensal
- Cobertura de liquidez

### Métricas de Desempenho

| Métrica | Valor |
|---------|-------|
| **F2-Score** | 0.7456 |
| **F1-Score** | 0.6412 |
| **Precision (classe 1)** | 0.50 |
| **Recall (classe 1)** | 0.85 |
| **Acurácia** | 0.70 |

**Threshold otimizado:** 0.4158 (maximiza F2-score, priorizando Recall)

### Justificativa do F2-Score

O F2-score foi escolhido como métrica principal porque:
- Prioriza **Recall** sobre **Precision**
- É mais importante **detectar inadimplentes** (evitar falsos negativos)
- Aprovar um inadimplente custa mais que reprovar um bom pagador

---




