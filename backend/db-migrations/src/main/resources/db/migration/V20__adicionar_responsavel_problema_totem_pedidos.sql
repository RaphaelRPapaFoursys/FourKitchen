ALTER TABLE pedidos
    ADD COLUMN id_garcom_responsavel_problema INTEGER,
    ADD CONSTRAINT fk_pedidos_garcom_responsavel_problema
        FOREIGN KEY (id_garcom_responsavel_problema) REFERENCES usuarios(id);

CREATE INDEX idx_pedidos_garcom_responsavel_problema
    ON pedidos(id_garcom_responsavel_problema);
