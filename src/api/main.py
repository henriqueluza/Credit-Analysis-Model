from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from src.api.schemas import ClientInput, PredictionResponse
from src.api.dependencies import get_model, get_scaler, transform_input
from src.utils.crud import insert_client, insert_prediction, get_prediction_by_id
import numpy as np

app = FastAPI(
    title='Modelo de Risco de Crédito',
    description='API que recebe dados de cliente, cadastra ele num banco de dados e roda um modelo de machine learning treinado para retornar um "aceito" ou "reprovado" para uma solicitação de crédito.',
    version='0.1.0'
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=['*'], # por enquanto (durante o desenvolvimento) aceita todos os métodos, requisições e headers
    allow_credentials=True,
    allow_methods=['*'],
    allow_headers=['*'],
)

@app.get('/health')
def health_check():
    return {'status': 'OK'}

@app.post('/predict', response_model=PredictionResponse)
def predict(client: ClientInput):
    features = transform_input(client)

    features_dict = {
        "log_renda_mensal":float(features[0]),
        "tem_atraso_grave":int(features[1]),
        "total_atrasos":int(features[2]),
        "idade":int(features[3]),
        "uso_credito_rotativo":float(features[4]),
        "razao_divida_renda":float(features[5]),
        "razao_extrema":float(features[6]),
        "num_dependentes":int(features[7]),
        "linhas_credito_abertas":int(features[8]),
        "emprestimos_imobiliarios":int(features[9])
    }

    client_id = insert_client(features_dict)

    # scaler
    features_array = np.array([features])
    scaler = get_scaler()
    features_scaled = scaler.transform(features_array)

    # predição
    model = get_model()
    prediction = model.predict(features_scaled)[0]
    probability = model.predict_proba(features_scaled)[0][1]
    resultado = "Reprovado" if prediction == 1 else 'Aprovado'

    insert_prediction(client_id, int(prediction), float(probability), '0.1.0')

    return PredictionResponse(
        client_id=client_id,
        prediction=prediction,
        resultado=resultado,
        probability=probability,
        model_version='0.1.0'
    )

@app.get('/predictions/{prediction_id}')
def get_prediction(prediction_id: int):
    row = get_prediction_by_id(prediction_id)

    if row is None:
        raise HTTPException(status_code=404, detail='Predição não encontrada')

    return {
        'id': row[0],
        'client_id': row[1],
        'prediction': row[2],
        'probability': row[3],
        'model_version': row[4],
        'created_at': str(row[5])
    }

# @app.get('/metrics', response_model=)

