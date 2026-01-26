from sqlalchemy import create_engine
import os
from dotenv import load_dotenv

load_dotenv()
def get_engine():
    user = os.getenv('DB_USER')
    password = os.getenv('DB_PASSWORD')
    host = os.getenv('DB_HOST')
    port = os.getenv('DB_PORT')
    db_name = os.getenv('DB_NAME')

    url = f'postgresql://{user}:{password}@{host}:{port}/{db_name}'
    engine = create_engine(url)
    return engine
