CREATE TABLE historico_atendimentos (
    id SERIAL PRIMARY KEY,
    id_atendimento INTEGER NOT NULL,
    codigo_sessao INTEGER NOT NULL,
    id_mesa INTEGER NOT NULL,
    numero_mesa INTEGER NOT NULL,
    id_garcom INTEGER,
    nome_garcom VARCHAR(100),
    valor_final DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_pedidos INTEGER NOT NULL DEFAULT 0,
    total_itens INTEGER NOT NULL DEFAULT 0,
    data_abertura TIMESTAMP NOT NULL,
    data_fechamento TIMESTAMP NOT NULL,
    duracao_minutos INTEGER NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_historico_atendimentos_atendimento
        UNIQUE (id_atendimento),

    CONSTRAINT fk_historico_atendimentos_atendimento
        FOREIGN KEY (id_atendimento)
        REFERENCES atendimentos(id),

    CONSTRAINT ck_historico_atendimentos_valor_final
        CHECK (valor_final >= 0),

    CONSTRAINT ck_historico_atendimentos_total_pedidos
        CHECK (total_pedidos >= 0),

    CONSTRAINT ck_historico_atendimentos_total_itens
        CHECK (total_itens >= 0),

    CONSTRAINT ck_historico_atendimentos_duracao
        CHECK (duracao_minutos >= 0),

    CONSTRAINT ck_historico_atendimentos_periodo
        CHECK (data_fechamento >= data_abertura)
);

CREATE INDEX idx_historico_atendimentos_data_fechamento
    ON historico_atendimentos(data_fechamento);

CREATE INDEX idx_historico_atendimentos_id_mesa
    ON historico_atendimentos(id_mesa);

CREATE INDEX idx_historico_atendimentos_id_garcom
    ON historico_atendimentos(id_garcom);
