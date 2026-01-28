,# Caderno de Estudos - Credit Risk Model

Anotações de conceitos aprendidos durante o projeto.

---

## CORS (Cross-Origin Resource Sharing)

**Data**: 2026-01-28

### O que é

CORS é uma política de segurança implementada pelos navegadores que controla quais origens (domínios) podem fazer requisições para sua API.

**Analogia**: Imagine que sua API é um restaurante. Por padrão, o restaurante só aceita pedidos de pessoas que estão DENTRO dele (mesma origem). CORS é como uma lista de permissão que diz: "Aceite também pedidos de delivery vindos desses endereços específicos".

### Por que usar

- **Segurança**: Evita que sites maliciosos façam requisições não autorizadas à sua API
- **Controle**: Permite definir exatamente quais domínios, métodos e headers são permitidos
- **Necessário para SPAs**: Quando frontend e backend rodam em portas/domínios diferentes (ex: Streamlit em `localhost:8501` chamando API em `localhost:8000`)

### Exemplo prático

```python
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],           # Quais origens podem acessar (* = todas)
    allow_credentials=True,        # Permite envio de cookies/auth
    allow_methods=["*"],           # Quais métodos HTTP (GET, POST, etc.)
    allow_headers=["*"],           # Quais headers são permitidos
)
```

### Boas práticas

- **Desenvolvimento**: `allow_origins=["*"]` é aceitável para facilitar testes
- **Produção**: Especificar apenas as origens necessárias (ex: `["https://meusite.com"]`)
- Nunca usar `*` em produção com `allow_credentials=True`

### Documentação

- [FastAPI CORS](https://fastapi.tiangolo.com/tutorial/cors/)
- [MDN Web Docs - CORS](https://developer.mozilla.org/pt-BR/docs/Web/HTTP/CORS)

---