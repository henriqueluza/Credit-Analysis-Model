import psycopg2
import os
from dotenv import load_dotenv

load_dotenv()

def test_db():
    try:
        conn = psycopg2.connect(
            host = os.getenv("DB_HOST"),
            database = os.getenv("DB_NAME"),
            user = os.getenv("DB_USER"),
            password = os.getenv("DB_PASSWORD"),
            port = os.getenv("DB_PORT"),
        )

        cur = conn.cursor()
        cur.execute("SELECT version();")
        db_version = cur.fetchone()

        print("Conexão com o Banco de Dados realizada com sucesso!")
        print(f"Versão do Banco: {db_version[0]}")

        cur.close()
        conn.close()

    except Exception as e:
        print("Erro ao conectar no Banco de Dados ")
        print(e)

if __name__ == "__main__":
    test_db()