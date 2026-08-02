-- A V5 gravou um hash inválido (placeholder) por engano. Esta migration corrige
-- o hash do usuário admin de bootstrap para um valor bcrypt válido.
UPDATE usuarios
SET senha_hash = '$2b$10$vbK.RCkXfIFL1FMQbeuqgemZezFBGQY3DZRZ8V3zrrdzK4eJaFxEe'
WHERE email = 'admin@creditanalysis.local';