# Arquitetura — Sistema de Análise de Crédito

## Visão geral

Este documento descreve o bounded context **Análise de Crédito** e a arquitetura
alvo da migração do sistema, hoje um monólito FastAPI + Streamlit, para:

- **Spring Boot (Java)** — API principal, dona das regras de negócio e da persistência.
- **FastAPI (Python)** — microsserviço interno, stateless, que serve exclusivamente o
  modelo de Machine Learning.
- **PostgreSQL** — banco de dados, de propriedade exclusiva do Spring Boot.
- **React + Tailwind** — frontend, substituindo o Streamlit.

Nenhum código é escrito antes deste documento estar completo, pois ele define o
contrato entre os serviços e a estrutura de camadas que ambos devem seguir.

### Bounded context: Análise de Crédito

O contexto cobre todo o fluxo de "um analista submete os dados de um cliente e de
um pedido de empréstimo, e o sistema retorna uma decisão de risco (aprovado ou
reprovado) com a probabilidade associada". Isso inclui:

- Cadastro e autenticação de usuários (analistas/administradores) que operam o sistema.
- Submissão de uma solicitação de análise de crédito.
- Cálculo da probabilidade de risco via modelo de ML.
- Persistência e consulta do histórico de análises.
- Estatísticas agregadas sobre as análises realizadas.

O cálculo do modelo de ML em si (feature engineering, threshold, inferência) é
tratado como um **subdomínio de suporte** (MLOps), isolado no microsserviço FastAPI,
consumido pelo Spring Boot através de um contrato HTTP bem definido — nunca acessado
diretamente pelo frontend.
