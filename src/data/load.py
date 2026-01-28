import pandas as pd
from src.utils.database import get_engine

def save_to_table(df: pd.Dataframe, table_name: str, if_exists: str = 'append'):
    engine = get_engine()
    df.to_sql(table_name, engine, if_exists=if_exists, index=False)