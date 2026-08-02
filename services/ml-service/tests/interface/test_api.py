import pytest
from fastapi.testclient import TestClient

from app.main import app


@pytest.fixture()
def client():
    with TestClient(app) as test_client:
        yield test_client


def _payload(**overrides):
    base = {
        "idade": 35,
        "salarioAnual": 60000.0,
        "situacaoMoradia": "OWN",
        "saldoContaCorrente": 1500.0,
        "saldoContaPoupanca": 5000.0,
        "valorEmprestimo": 10000.0,
        "prazoMeses": 24,
    }
    base.update(overrides)
    return base


def test_predict_retorna_200_com_resultado(client):
    response = client.post("/predict", json=_payload())

    assert response.status_code == 200
    body = response.json()
    assert body["resultado"] in ("APROVADO", "REPROVADO")
    assert 0.0 <= body["probabilidadeRisco"] <= 1.0
    assert body["thresholdUtilizado"] > 0
    assert body["versaoModelo"] == "1.0.0"


def test_predict_retorna_422_para_idade_invalida(client):
    response = client.post("/predict", json=_payload(idade=10))

    assert response.status_code == 422


def test_predict_retorna_422_para_prazo_invalido(client):
    response = client.post("/predict", json=_payload(prazoMeses=0))

    assert response.status_code == 422


def test_predict_retorna_422_para_valor_emprestimo_invalido(client):
    response = client.post("/predict", json=_payload(valorEmprestimo=0))

    assert response.status_code == 422


def test_predict_retorna_422_para_situacao_moradia_invalida(client):
    response = client.post("/predict", json=_payload(situacaoMoradia="INVALIDA"))

    assert response.status_code == 422


def test_model_info_retorna_metadados_do_modelo(client):
    response = client.get("/model-info")

    assert response.status_code == 200
    body = response.json()
    assert body["versaoModelo"] == "1.0.0"
    assert body["dataTreino"] == "2025-12-31"
    assert "f2_score" in body["metricas"]
    assert "idade" in body["features"]
