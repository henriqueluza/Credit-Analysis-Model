# Roadmap do Projeto de Análise de Crédito

### 1. Configuração e Estrutura de Dados (Data Engineering)
- [ ] Configurar um container Docker para o banco de dados PostgreSQL.
- [ ] Criar um script (ETL) em Python para simular a ingestão do "Data Lake".
- [ ] Definir o schema do banco de dados.
- [ ] Validar a integridade dos dados inseridos via queries SQL.

### 2. Ciência de Dados e Modelagem (Data Science)
- [ ] Realizar Análise Exploratória de Dados (EDA) nos Jupyter Notebooks.
- [ ] Realizar pré-processamento (tratamento de nulos, encoding, normalização).
- [ ] Treinar múltiplos modelos (Logistic Regression, RandomForest, XGBoost, etc.).
- [ ] Otimizar hiperparâmetros focando no **F-beta score**.
- [ ] Serializar o melhor modelo.

### 3. Backend (FastAPI)
- [ ] Criar estrutura do projeto FastAPI.
- [ ] Desenvolver endpoint de `POST /predict`.
- [ ] Implementar validação de dados com Pydantic.
- [ ] Carregar o modelo treinado na inicialização da API.
- [ ] Adicionar testes unitários.
- [ ] Gerar documentação automática (Swagger UI).

### 4. Frontend (Streamlit)
- [ ] Criar interface para input dos dados.
- [ ] Conectar o Streamlit ao backend FastAPI.
- [ ] Exibir a classificação e probabilidade visualmente.
- [ ] Adicionar "Explainable AI" (SHAP values).

### 5. DevOps e Infraestrutura
- [ ] Criar `Dockerfile` para API e Frontend.
- [ ] Criar `docker-compose.yml`.
- [ ] Configurar conta e infraestrutura na AWS (EC2/ECS, RDS).

### 6. CI/CD e Deploy
- [ ] Criar repositório no GitHub.
- [ ] Configurar Pipeline de CI (GitHub Actions).
- [ ] Configurar Pipeline de CD para deploy na AWS.
- [ ] Escrever o `README.md`.