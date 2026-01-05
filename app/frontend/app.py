import streamlit as st
import requests

st.set_page_config(page_title="Credit Score Analysis Project", page_icon="💰")
st.title("Sistema de Análise de Crédito")
st.markdown("Preencha os dados abaixo para obter uma previsão de aprovação de crédito")

API_URL = "https://localhost:8000/predict"

with st.form('formulario_credito'):
    st.subheader("Preencha os dados do cliente")

    col1, col2 = st.columns(2)

    with col1:
        idade = st.number_input("Idade", min_value=18, max_value=120, value=25, step=1)
        salario_anual = st.number_input("Salário Anual (R$)", min_value=0.0, value=50000.0, step=1000.0)
        situacao_moradia_label = st.selectbox(
            "Situação de Moradia",
            options=["Casa Própria", "Aluguel", "Mora de Graça/Com os Pais"]
        )

    with col2:
        valor_conta_corrente = st.number_input("Saldo Conta Corrente (R$)", min_value=0.0, value=1500.0)
        valor_conta_poupanca = st.number_input("Saldo Poupança (R$)", min_value=0.0, value=5000.0)

    st.divider()
    st.subheader("Preencha os dados do Empréstimo")

    col3, col4 = st.columns(2)

    with col3:
        valor_emprestimo = st.number_input("Valor Solicitado (R$)", min_value=0.0, value=10000.0)
    with col4:
        prazo_meses = st.number_input("Prazo (meses)", min_value=1, max_value=360, value=24)

    submit_button = st.form_submit_button("Avaliar Crédito")