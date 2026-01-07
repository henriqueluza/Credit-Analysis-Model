import pandas as pd
import psycopg2
from dotenv import load_dotenv

load_dotenv()

# criamos e printamos a lista dos nomes da colunas para poder organizar a tabela sql

df = pd.read_csv("modelo v2/data/cs-training.csv")

print(df.columns.tolist())

novos_nomes = {
    "SeriousDlqin2yrs": "Inadimplente", # bom ou mal pagador
    "RevolvingUtilizationOfUnsecuredLines": "UtilizacaoCredito", # saldo total em cartões de crédito e linhas de crédito pessoal dividido pela soma dos limites de crédito. Porcentagem do limite de crédito que está sendo usada
    "age": "Idade", # idade
    "NumberOfTime30-59DaysPastDueNotWorse": "Atraso30_59Dias", # quantidade de atrasos de pagamentos entre 30 e 59 dias nos dois últimos anos
    "DebtRatio": "TaxaEndividamento", # custo de vida incluindo dívidas divididos pela renda mensal bruta
    "MonthlyIncome": "RendaMensal", # renda mensal
    "NumberOfOpenCreditLinesAndLoans": "NumEmprestimosAbertos", # número de empréstimos abertos e linhas de crédito
    "NumberOfTimes90DaysLate": "Atraso90MaisDias", # número de vezes que o pagador atrasou pagamento por 90 dias ou mais
    "NumberRealEstateLoansOrLines": "NumEmprestimosImobiliarios", # número de empréstimos imobiliários
    "NumberOfTime60-89DaysPastDueNotWorse": "Atraso60_89Dias", # quantidades de vezes que o cliente atrasou pagamentos entre 60 e 89 dias nos dois últimos anos
    "NumberOfDependents": "NumDependentes" # número de dependentes na família
}

df = df.rename(columns = novos_nomes) # renomeando colunas para nomes mais simples e compreensíveis
df = df.drop(columns = ["Unnamed: 0"])

print("Colunas renomeadas:")
print(df.columns.tolist())