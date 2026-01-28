import joblib
import numpy as np
from src.api.schemas import ClientInput

MODEL_PATH = 'src/models/logistic_regression_final.joblib'
SCALER_PATH = 'src/models/scaler.joblib'
FEATURES_PATH = 'src/models/feature_names.joblib'

model = joblib.load(MODEL_PATH)
scaler = joblib.load(SCALER_PATH)
feature_names = joblib.load(FEATURES_PATH)

def get_model():
    return model

def get_scaler():
    return scaler

def get_features_names():
    return feature_names

def transform_input(client: ClientInput) -> list:
    log_renda_mensal = np.log1p(client.renda_mensal)
    uso_credito_rotativo = client.uso_limite / client.limite

    if client.renda_mensal > 0:
        razao_divida_renda = client.divida/client.renda_mensal
    else: razao_divida_renda = 0

    razao_extrema = 1 if razao_divida_renda > 1 else 0

    features = [
        log_renda_mensal,
        int(client.tem_atraso_grave),
        client.total_atrasos,
        client.idade,
        uso_credito_rotativo,
        razao_divida_renda,
        razao_extrema,
        client.num_dependentes,
        client.linhas_credito_abertas,
        client.emprestimos_imobiliarios
    ]

    return features