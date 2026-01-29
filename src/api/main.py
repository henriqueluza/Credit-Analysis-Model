from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from src.api.schemas import ClientInput, PredictionResponse
from src.api.dependencies import get_model, get_scaler, transform_input
from src.utils.crud import insert_client, insert_prediction
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

    # scaler
    features_array = np.array([features])
    scaler = get_scaler()
    features_scaled = scaler.transform(features_array)

    # predição
    model = get_model()
    prediction = model.predict(features_scaled)[0]
    probability = model.predict_proba(features_scaled)[0][1]
    resultado = "Reprovado" if prediction == 1 else 'Aprovado'

    return PredictionResponse(
        client_id=0,
        prediction=prediction,
        resultado=resultado,
        probability=probability,
        model_version='0.1.0'
    )

# @app.get('/predictions/{id}', response_model=)

# @app.get('/metrics', response_model=)

