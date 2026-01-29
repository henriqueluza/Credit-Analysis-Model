from sqlalchemy import text
from src.utils.database import get_engine

def insert_client(features: dict) -> int:
    engine = get_engine()

    query = text("""
                 INSERT INTO clients (log_renda_mensal, tem_atraso_grave, total_atrasos, idade, uso_credito_rotativo, razao_divida_renda, razao_extrema, num_dependentes, linhas_credito_abertas, emprestimos_imobiliarios)
                 VALUES (:log_renda_mensal, :tem_atraso_grave, :total_atrasos, :idade, :uso_credito_rotativo, :razao_divida_renda, :razao_extrema, :num_dependentes, :linhas_credito_abertas, :emprestimos_imobiliarios)
                 RETURNING id
    """)

    with engine.connect() as conn:
        result = conn.execute(query, features)
        conn.commit()
        client_id = result.scalar()

    return client_id

def insert_prediction(client_id: int, prediction: int, probability: float, model_version: str) -> int:
    engine = get_engine()
    query = text("""
                INSERT INTO predictions (client_id, prediction, probability, model_version)
                VALUES (:client_id, :prediction, :probability , :model_version)
                RETURNING id
    """)

    with engine.connect() as conn:
        result = conn.execute(query, {"client_id": client_id, "prediction": prediction, "probability": probability, "model_version": model_version})
        conn.commit()
        prediction_id = result.scalar()

    return prediction_id