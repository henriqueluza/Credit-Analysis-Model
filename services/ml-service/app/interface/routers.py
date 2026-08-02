import structlog
from fastapi import APIRouter, Depends, Request
from fastapi.responses import JSONResponse

from app.application.predizer_risco_use_case import PredizerRiscoUseCase
from app.domain.entities import FeaturesEmprestimo
from app.domain.ports import ModeloPreditivo
from app.interface.dependencies import get_modelo_preditivo, get_predizer_risco_use_case
from app.interface.schemas import ModelInfoResponse, ResultadoPredicaoResponse, SolicitacaoAnaliseRequest

logger = structlog.get_logger(__name__)

router = APIRouter()


@router.post("/predict", response_model=ResultadoPredicaoResponse)
def predict(
    request: SolicitacaoAnaliseRequest,
    use_case: PredizerRiscoUseCase = Depends(get_predizer_risco_use_case),
) -> ResultadoPredicaoResponse:
    features = FeaturesEmprestimo(
        idade=request.idade,
        salario_anual=request.salario_anual,
        situacao_moradia=request.situacao_moradia,
        saldo_conta_corrente=request.saldo_conta_corrente,
        saldo_conta_poupanca=request.saldo_conta_poupanca,
        valor_emprestimo=request.valor_emprestimo,
        prazo_meses=request.prazo_meses,
    )
    resultado = use_case.executar(features)
    logger.info(
        "predicao_realizada",
        resultado=resultado.resultado.value,
        probabilidade_risco=resultado.probabilidade_risco,
        versao_modelo=resultado.versao_modelo,
    )
    return ResultadoPredicaoResponse(
        resultado=resultado.resultado,
        probabilidade_risco=resultado.probabilidade_risco,
        threshold_utilizado=resultado.threshold_utilizado,
        versao_modelo=resultado.versao_modelo,
    )


@router.get("/health")
def health(request: Request) -> JSONResponse:
    modelo_carregado = getattr(request.app.state, "modelo_preditivo", None) is not None
    status_code = 200 if modelo_carregado else 503
    return JSONResponse(
        status_code=status_code,
        content={"status": "UP" if modelo_carregado else "DOWN", "modeloCarregado": modelo_carregado},
    )


@router.get("/model-info", response_model=ModelInfoResponse)
def model_info(modelo: ModeloPreditivo = Depends(get_modelo_preditivo)) -> ModelInfoResponse:
    info = modelo.informacoes()
    return ModelInfoResponse(
        versao_modelo=info.versao,
        data_treino=info.data_treino,
        metricas=info.metricas,
        features=info.features,
    )
