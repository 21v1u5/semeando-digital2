-- LGPD: coluna de anonimização do aluno (direito ao esquecimento).
-- IF NOT EXISTS torna a migração idempotente: funciona tanto em banco novo
-- (criado pela V1) quanto em bancos que já tinham a coluna via ddl-auto.
ALTER TABLE public.aluno ADD COLUMN IF NOT EXISTS anonimizado boolean NOT NULL DEFAULT false;
