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
Credit-Analysis-Model/
└── modelo v2/ 
    ├── data_lake/                  # Arquitetura Medallion
    │   ├── bronze/                 # Dados crus do Kaggle (.parquet ou .csv)
    │   ├── silver/                 # Dados limpos, engenharia de atributos e log aplicado
    │   └── gold/                   # Predições finais e tabelas para o dashboard
    ├── notebooks/                  # Desenvolvimento e Exploração
    │   ├── notebook1.ipynb         # Download e carrega na Bronze
    │   ├── notebook2.ipynb         # EDA, Limpeza, Log e Silver
    │   └── notebook3.ipynb         # Pipeline, SMOTE, Tunagem e Gold
    ├── src/                      
    │   ├── backend/                    # Backend com FastAPI
    │   │   ├── main.py
    │   │   ├── model_handler.py    # Lógica para carregar e usar o .joblib
    │   │   └── database.py         # Conexão com PostgreSQL (SQLAlchemy)
    │   ├── frontend/               # Interface com Streamlit
    │   │   └── app.py
    │   ├── utils/                  # Funções compartilhadas
    │   │   └── helpers.py
    │   └── models/                 # Pasta para salvar o modelo treinado
    │       └── model_v1.joblib
    ├── airflow/                    # Orquestração
    │   ├── dags/                   # DAGs do Airflow
    │   └── scripts/                # Scripts que a DAG irá chamar
    ├── infrastructure/             # Configurações de infra e banco
    │   ├── postgres/
    │   │   └── init.sql            # Queries para criar as tabelas iniciais
    │   ├──docker/
    │   └──aws/     
    │
    ├── tests/                      # Testes unitários para API
    ├── .github/                    # CI/CD
    │   └── workflows/
    │       └── main.yml            # GitHub Actions (Deploy na AWS)
    ├── docker-compose.yml          # Orquestrador de API, Front, DB e Airflow
    ├── requirements.txt            # Dependências do projeto
    ├── .gitignore                  # Arquivos e pastas a serem ignorados pelo Git
    └── README.md                   # Documentação do portfólio
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

