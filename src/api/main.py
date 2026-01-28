from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

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
