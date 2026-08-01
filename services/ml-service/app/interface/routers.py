from fastapi import APIRouter, Depends

from app.application.predizer_risco_use_case import PredizerRiscoUseCase
from app.domain.entities import FeaturesEmprestimo
from app.domain.ports import ModeloPreditivo
from app.interface.dependencies import get_modelo_preditivo, get_predizer_risco_use_case
from app.interface.schemas import ModelInfoResponse, ResultadoPredicaoResponse, SolicitacaoAnaliseRequest

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
    return ResultadoPredicaoResponse(
        resultado=resultado.resultado,
        probabilidade_risco=resultado.probabilidade_risco,
        threshold_utilizado=resultado.threshold_utilizado,
        versao_modelo=resultado.versao_modelo,
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
