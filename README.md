 # Credit-Analysis-Model
 
## 🏛️ Arquitetura do Projeto

## ⚙️ Stack Tecnológica

- Python e bibliotecas (Pandas, Numpy, SQLAlchemy, joblib, scikit-learn, Imbalanced-learn, FastAPI, Streamlit)
- PostgreSQL
- Docker
- Apache Airflow
- AWS
- GitHub Actions
 
## 🧪 Metodologia do Projeto
 
## 📂 Estrutura do Projeto
 ```
credit-risk-model/
│
├── data/                          # Dados locais (gitignore)
│   ├── raw/                       # Download do Kaggle
│   ├── processed/                 # Dados limpos
│   └── features/                  # Feature engineering
│
├── notebooks/
│   ├── 01_eda.ipynb              # Análise exploratória
│   ├── 02_feature_engineering.ipynb
│   ├── 03_baseline_models.ipynb
│   └── 04_model_optimization.ipynb
│
├── src/
│   ├── data/
│   │   ├── extract.py            # Download e upload S3
│   │   ├── transform.py          # Limpeza e feature eng
│   │   ├── load.py               # Carga para PostgreSQL
│   │   └── schemas.py            # Pydantic schemas
│   │
│   ├── models/
│   │   ├── train.py              # Pipeline de treinamento
│   │   ├── evaluate.py           # Métricas e validação
│   │   ├── predict.py            # Inferência
│   │   └── registry.py           # MLflow model registry
│   │
│   ├── api/
│   │   ├── main.py               # FastAPI app
│   │   ├── routes.py             # Endpoints
│   │   ├── dependencies.py       # Carregamento modelo
│   │   └── schemas.py            # Request/Response models
│   │
│   ├── monitoring/
│   │   ├── metrics.py            # Cálculo de métricas
│   │   └── drift.py              # Detecção de drift
│   │
│   └── utils/
│       ├── database.py           # Conexão PostgreSQL
│       ├── s3_client.py          # Cliente AWS S3
│       └── logger.py             # Configuração de logs
│
├── airflow/
│   └── dags/
│       ├── etl_daily.py          # ETL PostgreSQL → S3
│       ├── retrain_weekly.py    # Retreinamento semanal
│       └── monitor_daily.py     # Monitoramento drift
│
├── streamlit_app/
│   ├── app.py                    # App principal
│   └── pages/
│       ├── 1_📝_Cadastro.py     # Formulário
│       ├── 2_📊_Dashboard.py    # Métricas
│       └── 3_🔍_Histórico.py    # Predições anteriores
│
├── tests/
│   ├── test_api.py               # Testes da API
│   ├── test_model.py             # Testes do modelo
│   └── test_data_pipeline.py    # Testes ETL
│
├── infra/
│   ├── docker/
│   │   ├── Dockerfile.api
│   │   ├── Dockerfile.streamlit
│   │   ├── Dockerfile.airflow
│   │   └── Dockerfile.mlflow
│   ├── docker-compose.yml
│   └── sql/
│       └── init.sql              # Criação de tabelas
│
├── .github/workflows/
│   ├── ci.yml                    # Testes e linting
│   └── cd.yml                    # Build Docker
│
├── .env.example
├── requirements.txt
└── README.md
```

## 💻 Como Executar o Projeto  

1. Clone o repositório
```
git clone https://github.com/henriqueluza/Credit-Analysis-Model

```

2. Suba toda a infraestrutura com Docker Compose
```
docker-compose up -d
```
3. API está disponível em: `http://localhost:8000` e o Frontend em `http://localhost:8501`


## 📊 Resultados 

