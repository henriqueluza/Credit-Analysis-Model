-- Atualiza a senha do usuário admin semeado em V4 para um novo hash bcrypt.
-- V4 usava uma senha de bootstrap conhecida (admin123), inadequada para produção.
--
-- ATENÇÃO: o hash abaixo é um placeholder e NÃO deve ir para produção.
-- Antes do deploy, gere um novo hash bcrypt (ex.: com o BCryptPasswordEncoder
-- do Spring Security ou `htpasswd -bnBC 10 "" 'senha-forte' | tr -d ':\n'`)
-- para uma senha forte e substitua o valor abaixo.
UPDATE usuarios
SET senha_hash = '$2b$10$SUBSTITUA.ESTE.HASH.ANTES.DO.DEPLOY.EM.PRODUCAO'
WHERE email = 'admin@creditanalysis.local';
