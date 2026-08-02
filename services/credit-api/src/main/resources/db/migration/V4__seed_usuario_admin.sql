-- Usuário administrador inicial, necessário para acessar rotas restritas a ADMIN
-- (ex.: GET /api/analises/stats) logo após o setup do sistema.
-- Credenciais de bootstrap — troque a senha em um ambiente real via um fluxo
-- próprio de administração assim que o primeiro login for feito.
-- email: admin@creditanalysis.local | senha: admin123
INSERT INTO usuarios (nome, email, senha_hash, role)
VALUES ('Administrador', 'admin@creditanalysis.local', '$2b$10$icsk4RtNV5UBHT145yfPyun6H3B76ZhIPQrlRt7vAblPv3BYj.ftS', 'ADMIN');
