CREATE TABLE solicitacoes_emprestimo (
    id                              BIGSERIAL PRIMARY KEY,

    -- Cliente (value object embutido)
    idade                           INTEGER NOT NULL CHECK (idade >= 18 AND idade <= 120),
    salario_anual                   NUMERIC(14, 2) NOT NULL CHECK (salario_anual >= 0),
    situacao_moradia                VARCHAR(10) NOT NULL CHECK (situacao_moradia IN ('OWN', 'RENT', 'FREE')),
    saldo_conta_corrente            NUMERIC(14, 2) NOT NULL CHECK (saldo_conta_corrente >= 0),
    saldo_conta_poupanca            NUMERIC(14, 2) NOT NULL CHECK (saldo_conta_poupanca >= 0),

    -- Dados do empréstimo
    valor_emprestimo                NUMERIC(14, 2) NOT NULL CHECK (valor_emprestimo > 0),
    prazo_meses                     INTEGER NOT NULL CHECK (prazo_meses > 0 AND prazo_meses <= 360),

    -- Metadados da solicitação
    solicitado_por                  BIGINT NOT NULL REFERENCES usuarios (id),
    criado_em                       TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- ResultadoAnalise (value object embutido)
    resultado_status                VARCHAR(10) NOT NULL CHECK (resultado_status IN ('APROVADO', 'REPROVADO')),
    resultado_probabilidade_risco   NUMERIC(5, 4) NOT NULL,
    resultado_threshold_utilizado   NUMERIC(5, 4) NOT NULL,
    resultado_versao_modelo         VARCHAR(50) NOT NULL,
    resultado_analisado_em          TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_solicitacoes_emprestimo_criado_em ON solicitacoes_emprestimo (criado_em DESC);
CREATE INDEX idx_solicitacoes_emprestimo_solicitado_por ON solicitacoes_emprestimo (solicitado_por);
