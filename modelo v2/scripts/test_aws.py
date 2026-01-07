import boto3
import os
from dotenv import load_dotenv

load_dotenv() # carrega as variáveis do arquivo .env

# cria função para testar a conexão

def testar_conexao():
    aws_access_key = os.getenv("AWS_ACCESS_KEY_ID")
    aws_secret_key = os.getenv("AWS_SECRET_ACCESS_KEY")
    bucket_name = os.getenv("S3_BUCKET_NAME")

    try:
        s3 = boto3.client(
            's3',
            aws_access_key_id=aws_access_key,
            aws_secret_access_key=aws_secret_key,
        )

        response = s3.list_buckets()

        buckets = [bucket['Name'] for bucket in response['Buckets']]
        print("Conexão realizada com sucesso!")

        if bucket_name in buckets:
            print(f"Bucket '{bucket_name}' encontrado!")
        else:
            print(
                f"AVISO: Conectou, mas não encontrou o bucket '{bucket_name}'. Verifique o nome no .env ou no console AWS.")
            print(f"Buckets disponíveis: {buckets}")

    except Exception as e:
        print("Erro ao conectar com a AWS")
        print(e)

if __name__ == "__main__":
    testar_conexao()