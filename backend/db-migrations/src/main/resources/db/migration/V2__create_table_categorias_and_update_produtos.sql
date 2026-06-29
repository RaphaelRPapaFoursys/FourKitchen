CREATE TABLE categorias (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(80) NOT NULL UNIQUE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

ALTER TABLE produtos
    DROP COLUMN categoria;

ALTER TABLE produtos
    ADD COLUMN id_categoria INTEGER NOT NULL;

ALTER TABLE produtos
    ADD CONSTRAINT fk_produtos_categorias
        FOREIGN KEY (id_categoria)
        REFERENCES categorias(id);
