ALTER TABLE notificacoes
    ADD COLUMN id_mesa INTEGER,
    ADD COLUMN id_atendimento INTEGER,
    ADD COLUMN id_garcom INTEGER;

ALTER TABLE notificacoes
    ADD CONSTRAINT fk_notificacoes_mesa
        FOREIGN KEY (id_mesa)
        REFERENCES mesas(id);

ALTER TABLE notificacoes
    ADD CONSTRAINT fk_notificacoes_atendimento
        FOREIGN KEY (id_atendimento)
        REFERENCES atendimentos(id);

ALTER TABLE notificacoes
    ADD CONSTRAINT fk_notificacoes_garcom
        FOREIGN KEY (id_garcom)
        REFERENCES usuarios(id);

CREATE INDEX idx_notificacoes_chamadas_atendimento
    ON notificacoes (tipo, destino, lida, id_atendimento);
