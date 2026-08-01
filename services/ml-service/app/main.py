from contextlib import asynccontextmanager
from pathlib import Path

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from app.domain.exceptions import FeaturesInvalidasError
from app.infrastructure.modelo_joblib_repository import ModeloJoblibRepository
from app.interface.routers import router

MODELS_DIR = Path(__file__).resolve().parent.parent / "models"


@asynccontextmanager
async def lifespan(app: FastAPI):
    app.state.modelo_preditivo = ModeloJoblibRepository(
        caminho_modelo=MODELS_DIR / "modelo_credito_final.joblib",
        caminho_metadata=MODELS_DIR / "model_metadata.json",
    )
    yield


app = FastAPI(title="Serviço de ML - Análise de Crédito", lifespan=lifespan)
app.include_router(router)


@app.exception_handler(FeaturesInvalidasError)
async def handle_features_invalidas(request: Request, exc: FeaturesInvalidasError) -> JSONResponse:
    return JSONResponse(status_code=422, content={"detail": str(exc)})
