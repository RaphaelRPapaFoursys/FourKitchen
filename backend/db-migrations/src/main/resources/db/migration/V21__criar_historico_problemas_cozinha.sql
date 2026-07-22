CREATE TABLE problemas_cozinha (
    id SERIAL PRIMARY KEY,
    id_pedido INTEGER NOT NULL,
    id_produto_pedido INTEGER NOT NULL,
    motivo VARCHAR(50) NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_resolucao TIMESTAMP,
    CONSTRAINT fk_problemas_cozinha_pedido
        FOREIGN KEY (id_pedido) REFERENCES pedidos(id),
    CONSTRAINT fk_problemas_cozinha_produto_pedido
        FOREIGN KEY (id_produto_pedido) REFERENCES produtos_pedidos(id),
    CONSTRAINT ck_problemas_cozinha_motivo
        CHECK (motivo IN ('ERRO', 'INDISPONIVEL'))
);

CREATE INDEX idx_problemas_cozinha_data_motivo
    ON problemas_cozinha (data_criacao, motivo);

CREATE INDEX idx_problemas_cozinha_pendente
    ON problemas_cozinha (id_pedido, id_produto_pedido, data_criacao DESC)
    WHERE data_resolucao IS NULL;
