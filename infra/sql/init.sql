CREATE TABLE clients (
      id SERIAL PRIMARY KEY,
      log_renda_mensal FLOAT,
      tem_atraso_grave INTEGER,
      total_atrasos INTEGER,
      idade INTEGER,
      uso_credito_rotativo FLOAT,
      razao_divida_renda FLOAT,
      razao_extrema FLOAT,
      num_dependentes INTEGER,
      linhas_credito_abertas INTEGER,
      emprestimos_imobiliarios INTEGER,
      created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE predictions (
    id SERIAL PRIMARY KEY,
    client_id INTEGER REFERENCES clients(id),
    prediction INTEGER,
    probability FLOAT,
    model_version VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE model_metrics(
    id SERIAL PRIMARY KEY,
    model_version VARCHAR(50) NOT NULL,
    accuracy FLOAT,
    f1_score FLOAT,
    auc_roc FLOAT,
    precision_class_1 FLOAT,
    recall_class_1 FLOAT,
    dataset_type VARCHAR(20),
    created_at TIMESTAMP DEFAULT NOW()

);
CREATE INDEX idx_predictions_client_id ON predictions(client_id);
CREATE INDEX idx_predictions_created_at ON predictions(created_at);
CREATE INDEX idx_model_metrics_model_version ON model_metrics(model_version);
