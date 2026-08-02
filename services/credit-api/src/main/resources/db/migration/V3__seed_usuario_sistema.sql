-- Usuário de sistema, usado como solicitante das análises enquanto não existe
-- autenticação real (etapa 5). Nenhuma senha funcional é atribuída a ele.
INSERT INTO usuarios (id, nome, email, senha_hash, role)
VALUES (1, 'Sistema', 'sistema@creditanalysis.local', 'N/A', 'ANALISTA');

SELECT setval('usuarios_id_seq', (SELECT MAX(id) FROM usuarios));
