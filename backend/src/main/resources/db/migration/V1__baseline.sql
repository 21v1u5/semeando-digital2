-- Baseline do schema (gerado a partir do schema real criado pelo Hibernate).
-- Em bancos que já existem, o Flyway marca esta versão como baseline e NÃO a
-- executa (spring.flyway.baseline-on-migrate=true); em bancos novos, ela cria
-- todo o schema. A coluna aluno.anonimizado é adicionada na V2.

CREATE TABLE public.aluno (
    id bigint NOT NULL,
    autorizados_retirada character varying(255),
    comprovante_taxa character varying(255),
    consentimento_lgpd boolean NOT NULL,
    contato_emergencia character varying(255),
    data_nascimento date,
    doc_cartao_vacina boolean NOT NULL,
    doc_certidao_nascimento boolean NOT NULL,
    doc_comprovante_residencia boolean NOT NULL,
    doc_documento_responsavel boolean NOT NULL,
    doc_foto boolean NOT NULL,
    fardamento character varying(255),
    hospital_referencia character varying(255),
    matricula character varying(255),
    motivo_rejeicao character varying(255),
    nome character varying(255),
    status character varying(255),
    taxa_matricula_paga boolean NOT NULL,
    turma_id bigint,
    usuario_id bigint,
    CONSTRAINT aluno_status_check CHECK (((status)::text = ANY ((ARRAY['PRE_MATRICULA'::character varying, 'APROVADA'::character varying, 'MATRICULADO'::character varying, 'REJEITADA'::character varying])::text[])))
);

CREATE TABLE public.aluno_responsaveis (
    aluno_id bigint NOT NULL,
    responsaveis_id bigint NOT NULL
);

CREATE SEQUENCE public.aluno_seq START WITH 1 INCREMENT BY 50 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE public.auditoria (
    id bigint NOT NULL,
    acao character varying(255),
    data_hora timestamp(6) without time zone,
    detalhe character varying(1000),
    usuario character varying(255)
);

CREATE SEQUENCE public.auditoria_seq START WITH 1 INCREMENT BY 50 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE public.aviso (
    id bigint NOT NULL,
    autor character varying(255),
    data_hora timestamp(6) without time zone,
    mensagem character varying(2000),
    titulo character varying(255),
    turma_id bigint
);

CREATE SEQUENCE public.aviso_seq START WITH 1 INCREMENT BY 50 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE public.contrato (
    id bigint NOT NULL,
    ano_letivo integer NOT NULL,
    data_emissao date,
    desconto_percentual double precision NOT NULL,
    numero_parcelas integer NOT NULL,
    observacao character varying(255),
    parcelas_geradas boolean NOT NULL,
    tipo character varying(255),
    valor_parcela numeric(38,2),
    aluno_id bigint
);

CREATE SEQUENCE public.contrato_seq START WITH 1 INCREMENT BY 50 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE public.evento_calendario (
    id bigint NOT NULL,
    data date,
    tipo character varying(255),
    titulo character varying(255)
);

CREATE SEQUENCE public.evento_calendario_seq START WITH 1 INCREMENT BY 50 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE public.frequencia (
    id bigint NOT NULL,
    data date,
    presente boolean NOT NULL,
    aluno_id bigint
);

CREATE SEQUENCE public.frequencia_seq START WITH 1 INCREMENT BY 50 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE public.instituicao (
    id bigint NOT NULL,
    cnpj character varying(255),
    endereco character varying(255),
    juros_mensal_percentual double precision NOT NULL,
    multa_percentual double precision NOT NULL,
    nome_fantasia character varying(255),
    razao_social character varying(255),
    telefone character varying(255)
);

CREATE TABLE public.nota (
    id bigint NOT NULL,
    bimestre integer NOT NULL,
    disciplina character varying(255),
    valor double precision NOT NULL,
    aluno_id bigint
);

CREATE SEQUENCE public.nota_seq START WITH 1 INCREMENT BY 50 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE public.parcela (
    id bigint NOT NULL,
    codigo_boleto character varying(255),
    data_pagamento date,
    numero integer NOT NULL,
    pago boolean NOT NULL,
    valor numeric(38,2),
    valor_pago numeric(38,2),
    vencimento date,
    contrato_id bigint
);

CREATE SEQUENCE public.parcela_seq START WITH 1 INCREMENT BY 50 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE public.plano_aula (
    id bigint NOT NULL,
    atividades character varying(2000),
    data date,
    disciplina character varying(255),
    objetivos character varying(2000),
    titulo character varying(255),
    professor_id bigint,
    turma_id bigint
);

CREATE SEQUENCE public.plano_aula_seq START WITH 1 INCREMENT BY 50 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE public.registro_aula (
    id bigint NOT NULL,
    conteudo character varying(2000),
    data date,
    disciplina character varying(255),
    turma_id bigint
);

CREATE SEQUENCE public.registro_aula_seq START WITH 1 INCREMENT BY 50 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE public.tabela_preco (
    id bigint NOT NULL,
    serie character varying(255),
    valor numeric(38,2)
);

CREATE SEQUENCE public.tabela_preco_seq START WITH 1 INCREMENT BY 50 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE public.turma (
    id bigint NOT NULL,
    horario character varying(255),
    nome character varying(255),
    sala character varying(255),
    segmento character varying(255),
    turno character varying(255),
    professor_id bigint,
    CONSTRAINT turma_segmento_check CHECK (((segmento)::text = ANY ((ARRAY['INFANTIL'::character varying, 'FUNDAMENTAL'::character varying])::text[])))
);

CREATE SEQUENCE public.turma_seq START WITH 1 INCREMENT BY 50 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE public.usuario (
    id bigint NOT NULL,
    disciplinas character varying(255),
    email character varying(255),
    nome character varying(255),
    perfil character varying(255),
    senha character varying(255),
    telefone character varying(255),
    CONSTRAINT usuario_perfil_check CHECK (((perfil)::text = ANY ((ARRAY['GESTOR'::character varying, 'PROFESSOR'::character varying, 'ALUNO'::character varying, 'RESPONSAVEL'::character varying])::text[])))
);

CREATE SEQUENCE public.usuario_seq START WITH 1 INCREMENT BY 50 NO MINVALUE NO MAXVALUE CACHE 1;

ALTER TABLE ONLY public.aluno ADD CONSTRAINT aluno_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.auditoria ADD CONSTRAINT auditoria_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.aviso ADD CONSTRAINT aviso_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.contrato ADD CONSTRAINT contrato_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.evento_calendario ADD CONSTRAINT evento_calendario_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.frequencia ADD CONSTRAINT frequencia_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.instituicao ADD CONSTRAINT instituicao_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.nota ADD CONSTRAINT nota_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.parcela ADD CONSTRAINT parcela_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.plano_aula ADD CONSTRAINT plano_aula_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.registro_aula ADD CONSTRAINT registro_aula_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.tabela_preco ADD CONSTRAINT tabela_preco_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.turma ADD CONSTRAINT turma_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.usuario ADD CONSTRAINT usuario_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.usuario ADD CONSTRAINT uk5171l57faosmj8myawaucatdw UNIQUE (email);
ALTER TABLE ONLY public.tabela_preco ADD CONSTRAINT uk751m33nbtnng35kwso978b13w UNIQUE (serie);
ALTER TABLE ONLY public.aluno ADD CONSTRAINT ukp3aj1urfrd6mo5m2ycjlae8g7 UNIQUE (usuario_id);

ALTER TABLE ONLY public.aluno_responsaveis ADD CONSTRAINT fk1g408psl5xfkm5ajoalfnwvlc FOREIGN KEY (responsaveis_id) REFERENCES public.usuario(id);
ALTER TABLE ONLY public.aluno_responsaveis ADD CONSTRAINT fk6i08ocqoqep6aj8vox2qsxto6 FOREIGN KEY (aluno_id) REFERENCES public.aluno(id);
ALTER TABLE ONLY public.nota ADD CONSTRAINT fk7ntsgem4ql46uy34ile14rg74 FOREIGN KEY (aluno_id) REFERENCES public.aluno(id);
ALTER TABLE ONLY public.registro_aula ADD CONSTRAINT fk91whb4vtgm7nrbhobsv5lvo5g FOREIGN KEY (turma_id) REFERENCES public.turma(id);
ALTER TABLE ONLY public.plano_aula ADD CONSTRAINT fk9vtuljcoupxwod8m03jgmooig FOREIGN KEY (turma_id) REFERENCES public.turma(id);
ALTER TABLE ONLY public.plano_aula ADD CONSTRAINT fkdd3uadlpawejtyt6fkcnqnbir FOREIGN KEY (professor_id) REFERENCES public.usuario(id);
ALTER TABLE ONLY public.aluno ADD CONSTRAINT fkehtgr8rih20h4gomh4dd4sbxu FOREIGN KEY (turma_id) REFERENCES public.turma(id);
ALTER TABLE ONLY public.turma ADD CONSTRAINT fkfh62oo4jnj5q7slxjieo8cwxc FOREIGN KEY (professor_id) REFERENCES public.usuario(id);
ALTER TABLE ONLY public.aviso ADD CONSTRAINT fkn792dxcrv16sg6sfv49blm51a FOREIGN KEY (turma_id) REFERENCES public.turma(id);
ALTER TABLE ONLY public.frequencia ADD CONSTRAINT fknasycuhhuprl6lsxf3h4wgvmi FOREIGN KEY (aluno_id) REFERENCES public.aluno(id);
ALTER TABLE ONLY public.contrato ADD CONSTRAINT fkq0m1nao25erxu2p91gb1d518m FOREIGN KEY (aluno_id) REFERENCES public.aluno(id);
ALTER TABLE ONLY public.parcela ADD CONSTRAINT fkri3lio205cwiosdbfnmgs3k8v FOREIGN KEY (contrato_id) REFERENCES public.contrato(id);
ALTER TABLE ONLY public.aluno ADD CONSTRAINT fksgpw6tb2kerkceshx1b10rhkg FOREIGN KEY (usuario_id) REFERENCES public.usuario(id);
