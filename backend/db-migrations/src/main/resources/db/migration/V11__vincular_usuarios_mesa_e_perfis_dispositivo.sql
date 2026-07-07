ALTER TABLE usuarios
    ADD COLUMN id_mesa INTEGER;

ALTER TABLE usuarios
    ADD CONSTRAINT fk_usuarios_mesa
        FOREIGN KEY (id_mesa)
        REFERENCES mesas(id);

ALTER TABLE usuarios
    ADD CONSTRAINT ck_usuarios_id_mesa_perfil
        CHECK (
            (perfil = 'MESA' AND id_mesa IS NOT NULL)
            OR (perfil <> 'MESA' AND id_mesa IS NULL)
        );

CREATE UNIQUE INDEX uk_usuarios_mesa_dispositivo
    ON usuarios(id_mesa)
    WHERE perfil = 'MESA' AND id_mesa IS NOT NULL;

INSERT INTO usuarios (nome, email, senha, perfil, ativo, id_mesa)
SELECT
    dispositivo.nome,
    dispositivo.email,
    '$2b$10$KeqZIIDFiSW9uqnPfuwPruElUJxmz0yYV4UK.TCFH86rFfXJ4vtoK',
    dispositivo.perfil,
    TRUE,
    mesa.id
FROM (
    VALUES
        ('Mesa 01', 'mesa01@fourkitchen.com', 'MESA', 1),
        ('Mesa 02', 'mesa02@fourkitchen.com', 'MESA', 2),
        ('Mesa 03', 'mesa03@fourkitchen.com', 'MESA', 3),
        ('Mesa 04', 'mesa04@fourkitchen.com', 'MESA', 4),
        ('Mesa 05', 'mesa05@fourkitchen.com', 'MESA', 5),
        ('Mesa 06', 'mesa06@fourkitchen.com', 'MESA', 6),
        ('Mesa 07', 'mesa07@fourkitchen.com', 'MESA', 7),
        ('Mesa 08', 'mesa08@fourkitchen.com', 'MESA', 8),
        ('Totem 01', 'totem01@fourkitchen.com', 'TOTEM', NULL),
        ('Totem 02', 'totem02@fourkitchen.com', 'TOTEM', NULL)
) AS dispositivo(nome, email, perfil, numero_mesa)
LEFT JOIN mesas mesa ON mesa.numero = dispositivo.numero_mesa
ON CONFLICT (email) DO UPDATE
SET nome = EXCLUDED.nome,
    senha = EXCLUDED.senha,
    perfil = EXCLUDED.perfil,
    ativo = EXCLUDED.ativo,
    id_mesa = EXCLUDED.id_mesa;
