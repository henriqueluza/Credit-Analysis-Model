import streamlit as st
import requests
import pandas as pd
from datetime import datetime

st.set_page_config(page_title="Credit Score Analysis Project", page_icon="💰", layout="wide")
st.title("Sistema de Análise de Crédito")

API_URL = "http://localhost:8000"

# Sidebar para navegação
menu = st.sidebar.selectbox(
    "Menu",
    ["Nova Análise", "Histórico de Análises", "Estatísticas"]
)

if menu == "Nova Análise":
    st.markdown("Preencha os dados abaixo para obter uma previsão de aprovação de crédito")

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

        if submit_button:
            dicionario_moradia = {
                "Casa Própria": "own",
                "Aluguel": "rent",
                "Mora de Graça/Com os Pais": "free"
            }
            situacao_enviar = dicionario_moradia[situacao_moradia_label]

            payload = {
                "idade": idade,
                "valor_conta_poupanca": valor_conta_poupanca,
                "valor_conta_corrente": valor_conta_corrente,
                "salario_anual": salario_anual,
                "valor_emprestimo": valor_emprestimo,
                "prazo_meses": prazo_meses,
                "situacao_moradia": situacao_enviar
            }

            try:
                with st.spinner("Consultando o modelo..."):
                    response = requests.post(f"{API_URL}/predict", json=payload)

                if response.status_code == 200:
                    resultado = response.json()
                    status = resultado["resultado"]
                    probabilidade = resultado["probabilidade_risco"]

                    if status == "Aprovado":
                        st.success(f"✅ APROVADO")
                        st.metric(label="Probabilidade de risco", value=f"{probabilidade:.2%}", delta="Baixo Risco")
                    else:
                        st.error(f"❌ REPROVADO")
                        st.metric(label="Probabilidade de Risco", value=f"{probabilidade:.2%}", delta_color="inverse",
                                  delta="Alto Risco")

                    with st.expander("Ver detalhes técnicos"):
                        st.json(resultado)
                else:
                    st.error(f"Erro na API: {response.text}")

            except requests.exceptions.ConnectionError:
                st.error("Não foi possível conectar à API. Verifique se o backend está funcionando corretamente.")


elif menu == "Histórico de Análises":
    st.subheader("📋 Histórico de Análises de Crédito")

    col1, col2 = st.columns([3, 1])
    with col1:
        limit = st.slider("Número de registros", min_value=5, max_value=100, value=20, step=5)
    with col2:
        if st.button("🔄 Atualizar"):
            st.rerun()

    try:
        response = requests.get(f"{API_URL}/predictions", params={"limit": limit})

        if response.status_code == 200:
            data = response.json()
            predictions = data['predictions']
            total = data['total']

            if total == 0:
                st.info("Nenhuma análise realizada ainda. Faça sua primeira análise!")
            else:
                st.info(f"Total de análises no banco: {total}")

                # Converter para DataFrame
                df = pd.DataFrame(predictions)

                # Formatar colunas
                df['timestamp'] = pd.to_datetime(df['timestamp']).dt.strftime('%d/%m/%Y %H:%M:%S')
                df['valor_emprestimo'] = df['valor_emprestimo'].apply(lambda x: f"R$ {x:,.2f}")
                df['salario_anual'] = df['salario_anual'].apply(lambda x: f"R$ {x:,.2f}")
                df['probabilidade_risco'] = df['probabilidade_risco'].apply(lambda x: f"{x:.2%}")

                # Mapear situação moradia
                moradia_map = {'own': 'Própria', 'rent': 'Aluguel', 'free': 'Graça'}
                df['situacao_moradia'] = df['situacao_moradia'].map(moradia_map)

                # Renomear colunas
                df_display = df[[
                    'id', 'timestamp', 'idade', 'salario_anual', 'valor_emprestimo',
                    'prazo_meses', 'situacao_moradia', 'resultado', 'probabilidade_risco'
                ]].rename(columns={
                    'id': 'ID',
                    'timestamp': 'Data/Hora',
                    'idade': 'Idade',
                    'salario_anual': 'Salário Anual',
                    'valor_emprestimo': 'Valor Empréstimo',
                    'prazo_meses': 'Prazo (meses)',
                    'situacao_moradia': 'Moradia',
                    'resultado': 'Resultado',
                    'probabilidade_risco': 'Prob. Risco'
                })


                # Mostrar tabela com cores
                def color_resultado(val):
                    color = 'green' if val == 'Aprovado' else 'red'
                    return f'background-color: {color}; color: white'


                st.dataframe(
                    df_display.style.applymap(color_resultado, subset=['Resultado']),
                    use_container_width=True,
                    height=600
                )

                # Detalhes de uma análise específica
                st.divider()
                st.subheader("🔍 Ver Detalhes de uma Análise")

                prediction_id = st.number_input(
                    "Digite o ID da análise",
                    min_value=1,
                    max_value=int(df['ID'].max()) if len(df) > 0 else 1,
                    value=1
                )

                if st.button("Buscar"):
                    try:
                        detail_response = requests.get(f"{API_URL}/predictions/{prediction_id}")
                        if detail_response.status_code == 200:
                            detail = detail_response.json()

                            col1, col2, col3 = st.columns(3)

                            with col1:
                                st.metric("Idade", detail['idade'])
                                st.metric("Salário Anual", f"R$ {detail['salario_anual']:,.2f}")
                                st.metric("Conta Corrente", f"R$ {detail['valor_conta_corrente']:,.2f}")

                            with col2:
                                st.metric("Valor Empréstimo", f"R$ {detail['valor_emprestimo']:,.2f}")
                                st.metric("Prazo", f"{detail['prazo_meses']} meses")
                                st.metric("Poupança", f"R$ {detail['valor_conta_poupanca']:,.2f}")

                            with col3:
                                moradia_label = moradia_map.get(detail['situacao_moradia'], detail['situacao_moradia'])
                                st.metric("Moradia", moradia_label)
                                st.metric("Resultado", detail['resultado'])
                                st.metric("Prob. Risco", f"{detail['probabilidade_risco']:.2%}")

                        else:
                            st.error("Análise não encontrada")
                    except Exception as e:
                        st.error(f"Erro ao buscar análise: {e}")
        else:
            st.error("Erro ao carregar histórico")

    except requests.exceptions.ConnectionError:
        st.error("Não foi possível conectar à API. Verifique se o backend está funcionando.")

elif menu == "Estatísticas":
    st.subheader("📊 Estatísticas Gerais")

    try:
        response = requests.get(f"{API_URL}/predictions/stats")

        if response.status_code == 200:
            stats = response.json()

            col1, col2, col3, col4 = st.columns(4)

            with col1:
                st.metric(
                    label="Total de Análises",
                    value=stats['total_predicoes']
                )

            with col2:
                st.metric(
                    label="Aprovados",
                    value=stats['aprovados'],
                    delta=f"{stats['taxa_aprovacao']}%"
                )

            with col3:
                st.metric(
                    label="Reprovados",
                    value=stats['reprovados'],
                    delta=f"{100 - stats['taxa_aprovacao']:.2f}%",
                    delta_color="inverse"
                )

            with col4:
                st.metric(
                    label="Taxa de Aprovação",
                    value=f"{stats['taxa_aprovacao']}%"
                )

            # Gráfico de pizza
            if stats['total_predicoes'] > 0:
                st.divider()

                col1, col2 = st.columns(2)

                with col1:
                    st.subheader("Distribuição de Resultados")
                    chart_data = pd.DataFrame({
                        'Resultado': ['Aprovados', 'Reprovados'],
                        'Quantidade': [stats['aprovados'], stats['reprovados']]
                    })
                    st.bar_chart(chart_data.set_index('Resultado'))

                with col2:
                    st.subheader("Percentuais")
                    st.write(f"**Aprovados:** {stats['taxa_aprovacao']}%")
                    st.write(f"**Reprovados:** {100 - stats['taxa_aprovacao']:.2f}%")

                    # Barra de progresso
                    st.progress(stats['taxa_aprovacao'] / 100)
        else:
            st.error("Erro ao carregar estatísticas")

    except requests.exceptions.ConnectionError:
        st.error("Não foi possível conectar à API. Verifique se o backend está funcionando.")