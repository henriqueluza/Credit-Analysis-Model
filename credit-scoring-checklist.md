# 💳 Credit Scoring System - Checklist Completa do Projeto

## Informações do Projeto

| Item | Descrição |
|------|-----------|
| **Objetivo** | Sistema de previsão de inadimplência de cartão de crédito |
| **Dataset** | AmExpert CodeLab 2021 (American Express) |
| **Métrica Principal** | F1-Score ≥ 0.80 |

---

## 📋 FASE 1: Fundação e Preparação de Dados

### 1.1 Configuração do Ambiente
- [x] Criar diretório raiz do projeto (`Credit-Analysis-Model/`)
- [x] Criar estrutura de pastas:
  - [x] `.github/workflows/`
  - [x] `data/raw/`
  - [x] `data/processed/`
  - [x] `data/external/`
  - [x] `notebooks/`
  - [x] `src/data/`
  - [x] `src/features/`
  - [x] `src/models/`
  - [x] `src/api/`
  - [x] `src/dashboard/`
  - [x] `src/monitoring/`
  - [x] `src/utils/`
  - [x] `tests/`
  - [x] `monitoring/prometheus/`
  - [x] `monitoring/grafana/`
  - [x] `reports/figures/`
  - [x] `scripts/`
  - [x] `docker/`
  - [x] `mlruns/`
- [x] Criar ambiente virtual Python (`python -m venv venv`)
- [x] Criar `requirements.txt` com todas as dependências
- [x] Instalar dependências (`pip install -r requirements.txt`)
- [x] Inicializar Git (`git init`)
- [x] Criar `.gitignore`
- [x] Criar `.env.example` com variáveis de ambiente
- [x] Criar arquivos `__init__.py` em todos os módulos

### 1.2 Download e Exploração dos Dados
- [x] Baixar dataset AmExpert CodeLab 2021
- [x] Verificar integridade dos dados
- [x] Criar notebook `01_data_acquisition.ipynb`
- [x] Documentar estrutura do dataset (colunas, tipos, tamanho)

### 1.3 Análise Exploratória (EDA)
- [ ] Criar notebook `02_eda.ipynb`
- [ ] Analisar distribuição da variável target (`credit_card_default`)
- [ ] Calcular taxa de inadimplência
- [ ] Identificar desbalanceamento de classes
- [ ] Analisar valores missing por coluna
- [ ] Gerar estatísticas descritivas (numéricas e categóricas)
- [ ] Criar visualizações:
  - [ ] Distribuição do target (barplot + pie chart)
  - [ ] Histogramas das variáveis numéricas
  - [ ] Countplots das variáveis categóricas
  - [ ] Boxplots: numéricas vs target
  - [ ] Taxa de default por categoria
  - [ ] Matriz de correlação (heatmap)
  - [ ] Correlação com target (barplot horizontal)
- [ ] Detectar outliers (método IQR)
- [ ] Salvar todas as figuras em `reports/figures/`
- [ ] Documentar insights e conclusões

### 1.4 Pré-processamento
- [ ] Criar notebook `03_preprocessing.ipynb`
- [ ] Separar features (X) e target (y)
- [ ] Remover coluna de ID se existir
- [ ] Identificar colunas categóricas e numéricas
- [ ] Tratar valores missing:
  - [ ] Numéricas: imputar com mediana
  - [ ] Categóricas: imputar com moda
- [ ] Aplicar Label Encoding nas categóricas
- [ ] Criar novas features (Feature Engineering):
  - [ ] `debt_to_score_ratio`
  - [ ] `years_employed`
  - [ ] `adults_in_family`
  - [ ] `high_risk_flag`
  - [ ] `critical_utilization`
- [ ] Realizar split train/test (80/20, estratificado)
- [ ] Salvar dados processados em formato Parquet:
  - [ ] `X_train.parquet`
  - [ ] `X_test.parquet`
  - [ ] `y_train.parquet`
  - [ ] `y_test.parquet`
- [ ] Salvar artefatos de pré-processamento:
  - [ ] `num_imputer.joblib`
  - [ ] `cat_imputer.joblib`
  - [ ] `label_encoders.joblib`
- [ ] Salvar metadados em JSON

### 1.5 Versionamento de Dados
- [ ] Inicializar DVC (`dvc init`)
- [ ] Adicionar dados ao DVC (`dvc add data/raw/train.csv`)
- [ ] Configurar remote storage
- [ ] Fazer commit e push do DVC

---

## 📋 FASE 2: Modelagem e Experimentação

### 2.1 Configurar MLflow
- [ ] Criar script `scripts/start_mlflow.sh`
- [ ] Iniciar servidor MLflow na porta 5000
- [ ] Criar arquivo `src/utils/config.py` com configurações
- [ ] Testar conexão com MLflow
- [ ] Criar experimento "credit-scoring-baseline"
- [ ] Criar experimento "credit-scoring-advanced"
- [ ] Criar experimento "credit-scoring-optimization"

### 2.2 Modelos Baseline
- [ ] Criar notebook `04_modeling_baseline.ipynb`
- [ ] Carregar dados processados
- [ ] Aplicar SMOTE para balanceamento
- [ ] Criar função de avaliação (`evaluate_model`)
- [ ] Treinar e avaliar Logistic Regression:
  - [ ] Configurar parâmetros
  - [ ] Treinar modelo
  - [ ] Calcular métricas (F1, Precision, Recall, AUC)
  - [ ] Gerar confusion matrix
  - [ ] Logar no MLflow
- [ ] Treinar e avaliar Decision Tree:
  - [ ] Configurar parâmetros
  - [ ] Treinar modelo
  - [ ] Calcular métricas
  - [ ] Logar no MLflow
- [ ] Treinar e avaliar Random Forest:
  - [ ] Configurar parâmetros
  - [ ] Treinar modelo
  - [ ] Calcular métricas
  - [ ] Extrair feature importance
  - [ ] Logar no MLflow
- [ ] Comparar resultados dos baselines
- [ ] Salvar gráfico de comparação

### 2.3 Modelos Avançados (Gradient Boosting)
- [ ] Criar notebook `05_modeling_advanced.ipynb`
- [ ] Treinar e avaliar LightGBM:
  - [ ] Configurar `is_unbalance=True`
  - [ ] Configurar early stopping
  - [ ] Treinar modelo
  - [ ] Calcular métricas
  - [ ] Logar no MLflow
- [ ] Treinar e avaliar XGBoost:
  - [ ] Calcular `scale_pos_weight`
  - [ ] Configurar early stopping
  - [ ] Treinar modelo
  - [ ] Calcular métricas
  - [ ] Logar no MLflow
- [ ] Treinar e avaliar CatBoost:
  - [ ] Configurar `auto_class_weights='Balanced'`
  - [ ] Configurar early stopping
  - [ ] Treinar modelo
  - [ ] Calcular métricas
  - [ ] Logar no MLflow
- [ ] Comparar resultados dos modelos avançados
- [ ] Identificar melhor modelo base

### 2.4 Otimização de Hiperparâmetros
- [ ] Criar notebook `06_hyperparameter_tuning.ipynb`
- [ ] Definir função objetivo para Optuna
- [ ] Definir espaço de busca dos hiperparâmetros:
  - [ ] `n_estimators`
  - [ ] `learning_rate`
  - [ ] `num_leaves`
  - [ ] `max_depth`
  - [ ] `min_child_samples`
  - [ ] `subsample`
  - [ ] `colsample_bytree`
  - [ ] `reg_alpha`
  - [ ] `reg_lambda`
- [ ] Configurar validação cruzada estratificada (5-fold)
- [ ] Executar otimização (100+ trials)
- [ ] Visualizar resultados do Optuna:
  - [ ] Histórico de otimização
  - [ ] Importância dos hiperparâmetros
  - [ ] Coordenadas paralelas
- [ ] Documentar melhores hiperparâmetros

### 2.5 Modelo Final
- [ ] Treinar modelo final com melhores parâmetros
- [ ] Avaliar no conjunto de teste
- [ ] Logar métricas finais no MLflow

### 2.6 Otimização de Threshold
- [ ] Calcular precision-recall curve
- [ ] Calcular F1-score para cada threshold
- [ ] Identificar threshold que maximiza F1
- [ ] Comparar F1 com threshold padrão (0.5) vs otimizado
- [ ] Criar visualização threshold vs F1
- [ ] Salvar threshold otimizado em `best_threshold.json`

### 2.7 Registro do Modelo
- [ ] Registrar modelo no MLflow Model Registry
- [ ] Nomear modelo como "credit-scoring-model"
- [ ] Transicionar para stage "Production"
- [ ] Salvar modelo localmente (`final_model.joblib`)
- [ ] Salvar metadados do modelo (`model_metadata.json`)

### 2.8 Validação Final
- [ ] Verificar F1-Score ≥ 0.80
- [ ] Documentar todas as métricas finais
- [ ] Criar relatório de modelagem

---

## 📋 FASE 3: API e Serving

### 3.1 Schemas Pydantic
- [ ] Criar arquivo `src/api/schemas.py`
- [ ] Implementar `CustomerInput` (dados de entrada)
- [ ] Implementar `PredictionOutput` (resultado da predição)
- [ ] Implementar `BatchPredictionInput` (entrada em lote)
- [ ] Implementar `BatchPredictionOutput` (saída em lote)
- [ ] Implementar `HealthResponse` (health check)
- [ ] Implementar `ErrorResponse` (erros)
- [ ] Adicionar validadores customizados
- [ ] Adicionar exemplos nos schemas

### 3.2 Classe de Predição
- [ ] Criar arquivo `src/api/predictor.py`
- [ ] Implementar classe `CreditPredictor`
- [ ] Implementar método `_load_artifacts()`:
  - [ ] Carregar modelo do MLflow
  - [ ] Fallback para modelo local
  - [ ] Carregar label encoders
  - [ ] Carregar threshold otimizado
  - [ ] Carregar metadados
- [ ] Implementar método `preprocess()`:
  - [ ] Converter dict para DataFrame
  - [ ] Aplicar label encoding
  - [ ] Aplicar feature engineering
  - [ ] Garantir ordem das features
- [ ] Implementar método `predict()`:
  - [ ] Pré-processar dados
  - [ ] Fazer predição
  - [ ] Aplicar threshold
  - [ ] Determinar decisão e nível de risco
- [ ] Implementar método `get_model_info()`

### 3.3 Métricas Prometheus
- [ ] Criar arquivo `src/api/metrics.py`
- [ ] Implementar contador `PREDICTIONS_TOTAL`
- [ ] Implementar histograma `PREDICTION_LATENCY`
- [ ] Implementar histograma `MODEL_PROBABILITY`
- [ ] Implementar gauge `MODEL_LOADED`
- [ ] Implementar gauge `DRIFT_DETECTED`
- [ ] Criar context manager `track_prediction()`

### 3.4 API FastAPI
- [ ] Criar arquivo `src/api/main.py`
- [ ] Configurar logging com Loguru
- [ ] Implementar lifespan (startup/shutdown)
- [ ] Configurar CORS
- [ ] Implementar endpoint `GET /` (raiz)
- [ ] Implementar endpoint `GET /health`
- [ ] Implementar endpoint `POST /predict`
- [ ] Implementar endpoint `POST /predict/batch`
- [ ] Implementar endpoint `GET /metrics`
- [ ] Implementar exception handler global
- [ ] Testar todos os endpoints localmente

### 3.5 Containerização
- [ ] Criar `Dockerfile` principal
- [ ] Configurar imagem base Python 3.11-slim
- [ ] Instalar dependências do sistema (gcc, libgomp1)
- [ ] Copiar requirements e instalar
- [ ] Copiar código fonte e dados processados
- [ ] Criar usuário não-root
- [ ] Configurar healthcheck
- [ ] Definir comando de inicialização
- [ ] Testar build da imagem

### 3.6 Docker Compose
- [ ] Criar `docker-compose.yml`
- [ ] Configurar serviço `api`
- [ ] Configurar serviço `mlflow`
- [ ] Configurar serviço `postgres`
- [ ] Configurar serviço `prometheus`
- [ ] Configurar serviço `grafana`
- [ ] Configurar serviço `dashboard`
- [ ] Definir volumes persistentes
- [ ] Definir rede interna
- [ ] Testar `docker-compose up`

### 3.7 Testes da API
- [ ] Testar endpoint `/health` via curl
- [ ] Testar endpoint `/predict` com dados válidos
- [ ] Testar endpoint `/predict` com dados inválidos
- [ ] Testar endpoint `/predict/batch`
- [ ] Verificar documentação Swagger (`/docs`)
- [ ] Verificar métricas (`/metrics`)

---

## 📋 FASE 4: MLOps e CI/CD

### 4.1 Estrutura de Testes
- [ ] Criar arquivo `tests/conftest.py`
- [ ] Criar fixture `sample_customer_data`
- [ ] Criar fixture `high_risk_customer_data`
- [ ] Criar fixture `api_client`

### 4.2 Testes Unitários
- [ ] Criar `tests/test_api/test_schemas.py`
- [ ] Testar validação de CustomerInput
- [ ] Testar validação de campos obrigatórios
- [ ] Testar validação de ranges
- [ ] Criar `tests/test_api/test_endpoints.py`
- [ ] Testar `GET /health` retorna 200
- [ ] Testar `GET /health` retorna status healthy
- [ ] Testar `POST /predict` retorna 200
- [ ] Testar `POST /predict` retorna campos obrigatórios
- [ ] Testar probabilidade está no range [0, 1]
- [ ] Testar predição é binária (0 ou 1)
- [ ] Testar decisão é válida
- [ ] Testar risk_level é válido
- [ ] Testar cliente de alto risco tem probabilidade alta
- [ ] Testar dados inválidos retornam 422
- [ ] Testar campo faltante retorna 422
- [ ] Testar `POST /predict/batch` retorna 200
- [ ] Testar batch retorna contagem correta
- [ ] Testar batch excede limite retorna 400

### 4.3 Testes de Integração
- [ ] Criar `tests/test_integration/test_e2e.py`
- [ ] Testar fluxo completo de predição
- [ ] Testar conexão com MLflow
- [ ] Testar carregamento do modelo

### 4.4 Cobertura de Testes
- [ ] Configurar pytest-cov
- [ ] Executar testes com cobertura
- [ ] Verificar cobertura ≥ 80%
- [ ] Gerar relatório de cobertura

### 4.5 GitHub Actions - CI
- [ ] Criar `.github/workflows/ci.yml`
- [ ] Configurar job `lint`:
  - [ ] Checkout do código
  - [ ] Setup Python
  - [ ] Instalar ferramentas de lint
  - [ ] Executar Black
  - [ ] Executar isort
  - [ ] Executar Flake8
- [ ] Configurar job `test`:
  - [ ] Checkout do código
  - [ ] Setup Python
  - [ ] Cache de dependências pip
  - [ ] Instalar dependências
  - [ ] Executar pytest com cobertura
  - [ ] Upload para Codecov
- [ ] Configurar job `build`:
  - [ ] Executar apenas na branch main
  - [ ] Setup Docker Buildx
  - [ ] Login no GitHub Container Registry
  - [ ] Build e push da imagem

### 4.6 Qualidade de Código
- [ ] Configurar Black para formatação
- [ ] Configurar isort para imports
- [ ] Configurar Flake8 para linting
- [ ] Configurar pre-commit hooks
- [ ] Criar `.pre-commit-config.yaml`

---

## 📋 FASE 5: Monitoramento

### 5.1 Monitoramento de Drift (Evidently)
- [ ] Criar arquivo `src/monitoring/drift_monitor.py`
- [ ] Implementar classe `DriftMonitor`
- [ ] Implementar método `check_data_drift()`:
  - [ ] Criar relatório com DataDriftPreset
  - [ ] Salvar relatório HTML
  - [ ] Retornar indicador de drift
- [ ] Implementar método `check_prediction_drift()`
- [ ] Configurar geração periódica de relatórios
- [ ] Criar diretório `reports/drift/`

### 5.2 Configuração do Prometheus
- [ ] Criar `monitoring/prometheus/prometheus.yml`
- [ ] Configurar scrape interval (15s)
- [ ] Configurar job para scrape da API
- [ ] Configurar alerting rules (opcional)
- [ ] Testar coleta de métricas

### 5.3 Configuração do Grafana
- [ ] Criar `monitoring/grafana/provisioning/datasources/datasource.yml`
- [ ] Configurar Prometheus como datasource
- [ ] Criar dashboard para métricas do modelo:
  - [ ] Total de predições
  - [ ] Predições por decisão (approved/denied/manual)
  - [ ] Latência média e percentis
  - [ ] Distribuição de probabilidades
  - [ ] Indicador de drift
- [ ] Exportar dashboard como JSON
- [ ] Configurar provisioning automático

### 5.4 Alertas
- [ ] Configurar alertas no Grafana:
  - [ ] Alerta de latência alta (>1s)
  - [ ] Alerta de taxa de erro alta
  - [ ] Alerta de drift detectado
- [ ] Configurar canais de notificação (opcional)

### 5.5 Logging
- [ ] Configurar Loguru em toda a aplicação
- [ ] Definir formato de log estruturado
- [ ] Configurar rotação de logs
- [ ] Configurar retenção de logs

---

## 📋 FASE 6: Interface e Deploy Final

### 6.1 Dashboard Streamlit
- [ ] Criar arquivo `src/dashboard/app.py`
- [ ] Configurar página (título, ícone, layout)
- [ ] Criar sidebar com formulário:
  - [ ] Dados pessoais (gender, owns_car, owns_house, etc.)
  - [ ] Dados profissionais (occupation, days_employed, etc.)
  - [ ] Dados financeiros (credit_score, utilization, etc.)
  - [ ] Histórico (prev_defaults, default_6m)
- [ ] Implementar chamada à API
- [ ] Exibir resultado:
  - [ ] Métrica: Probabilidade de default
  - [ ] Métrica: Decisão (com emoji colorido)
  - [ ] Métrica: Nível de risco
- [ ] Criar gauge de risco com Plotly:
  - [ ] Faixas de cores (verde, amarelo, laranja, vermelho)
  - [ ] Indicador de threshold
- [ ] Criar expander com detalhes da análise
- [ ] Adicionar footer com informações do sistema
- [ ] Criar Dockerfile para dashboard

### 6.2 Documentação
- [ ] Criar `README.md` completo:
  - [ ] Descrição do projeto
  - [ ] Arquitetura
  - [ ] Quick Start
  - [ ] Instalação detalhada
  - [ ] Uso da API
  - [ ] Métricas do modelo
  - [ ] Estrutura do projeto
  - [ ] Contribuição
  - [ ] Licença
- [ ] Documentar endpoints da API
- [ ] Documentar schemas de entrada/saída
- [ ] Criar exemplos de uso com curl
- [ ] Documentar variáveis de ambiente

### 6.3 Deploy Final
- [ ] Verificar todos os serviços no docker-compose
- [ ] Executar `docker-compose up -d`
- [ ] Verificar saúde de todos os containers
- [ ] Testar acesso a todas as interfaces:
  - [ ] API Docs: http://localhost:8000/docs
  - [ ] MLflow: http://localhost:5000
  - [ ] Grafana: http://localhost:3000
  - [ ] Dashboard: http://localhost:8501
- [ ] Verificar métricas no Prometheus
- [ ] Verificar dashboards no Grafana

### 6.4 Testes End-to-End
- [ ] Testar fluxo completo via Dashboard
- [ ] Testar fluxo completo via API
- [ ] Verificar logs de todas as aplicações
- [ ] Verificar persistência de dados
- [ ] Testar restart dos containers
- [ ] Documentar qualquer issue encontrado

### 6.5 Finalização
- [ ] Fazer commit final
- [ ] Criar tag de versão (v1.0.0)
- [ ] Verificar CI/CD passou
- [ ] Atualizar documentação se necessário

---

## 📊 Métricas de Sucesso

| Métrica | Meta | Status |
|---------|------|--------|
| F1-Score | ≥ 0.80 | [ ] |
| Cobertura de Testes | ≥ 80% | [ ] |
| Latência da API (p95) | < 500ms | [ ] |
| Uptime | > 99% | [ ] |
| Documentação | Completa | [ ] |

---

## 🔗 Links Úteis

| Recurso | URL |
|---------|-----|
| Dataset | https://www.kaggle.com/datasets/pradip11/amexpert-codelab-2021 |
| Projeto Referência (F1=0.91) | https://github.com/maixbach/credit-risk-analysis-using-ML |
| MLflow Docs | https://mlflow.org/docs/latest/index.html |
| FastAPI Docs | https://fastapi.tiangolo.com/ |
| Evidently Docs | https://docs.evidentlyai.com/ |
| Optuna Docs | https://optuna.readthedocs.io/ |

---

## 📝 Notas

- Sempre fazer commit após completar cada secção
- Documentar decisões técnicas importantes
- Manter o MLflow atualizado com todos os experimentos
- Revisar código antes de cada merge
- Atualizar este checklist conforme o projeto avança

---
