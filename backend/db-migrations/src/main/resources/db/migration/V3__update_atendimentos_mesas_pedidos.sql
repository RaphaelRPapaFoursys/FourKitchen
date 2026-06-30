ALTER TABLE mesas
    ADD CONSTRAINT uk_mesas_numero UNIQUE (numero);

ALTER TABLE atendimentos
    ADD COLUMN id_mesa INTEGER,
    ADD COLUMN id_garcom INTEGER,
    ADD COLUMN data_abertura DATETIME,
    ADD COLUMN data_fechamento DATETIME;

ALTER TABLE atendimentos
    ADD CONSTRAINT fk_atendimentos_mesa
        FOREIGN KEY (id_mesa)
        REFERENCES mesas(id);

ALTER TABLE atendimentos
    ADD CONSTRAINT fk_atendimentos_garcom
        FOREIGN KEY (id_garcom)
        REFERENCES usuarios(id);

ALTER TABLE pedidos
    ADD COLUMN id_atendimento INTEGER;

ALTER TABLE pedidos
    ADD CONSTRAINT fk_pedidos_atendimento
        FOREIGN KEY (id_atendimento)
        REFERENCES atendimentos(id);
