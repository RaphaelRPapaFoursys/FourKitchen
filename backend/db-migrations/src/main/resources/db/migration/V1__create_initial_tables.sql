CREATE TABLE usuarios(
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE
);

CREATE TABLE produtos(
    id SERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    descricao VARCHAR(255),
    preco DECIMAL(10, 2) NOT NULL,
    categoria VARCHAR(150),
    disponivel BOOLEAN DEFAULT TRUE
);

CREATE TABLE atendimentos(
    id SERIAL PRIMARY KEY,
    codigo_sessao INTEGER NOT NULL,
    status_pedido VARCHAR(100)
);

CREATE TABLE mesas(
    id SERIAL PRIMARY KEY,
    numero INTEGER NOT NULL,
    disponivel BOOLEAN NOT NULL,
    id_atendimento INTEGER,
    CONSTRAINT fk_id_atendimento
            FOREIGN KEY (id_atendimento)
            REFERENCES atendimentos(id)
);

CREATE TABLE pedidos(
    id SERIAL PRIMARY KEY,
    codigo INTEGER NOT NULL,
    canal VARCHAR(50) NOT NULL,
    status VARCHAR(50),
    id_mesa INTEGER,
    id_usuario INTEGER,
    CONSTRAINT fk_id_mesa
                FOREIGN KEY (id_mesa)
                REFERENCES mesas(id),
    CONSTRAINT fk_id_usuario
                    FOREIGN KEY (id_usuario)
                    REFERENCES usuarios(id)
);

CREATE TABLE itens(
    id SERIAL PRIMARY KEY,
    id_produto INTEGER,
    descricao VARCHAR(255) NOT NULL,
    preco DECIMAL(10, 2),
    CONSTRAINT fk_id_produto_itens
                    FOREIGN KEY (id_produto)
                    REFERENCES produtos(id)
);

CREATE TABLE itens_pedidos(
    id SERIAL PRIMARY KEY,
    id_pedido INTEGER,
    id_produto INTEGER,
    CONSTRAINT fk_id_itens_pedido
                    FOREIGN KEY (id_pedido)
                    REFERENCES pedidos(id),
    CONSTRAINT fk_id_produto
                    FOREIGN KEY (id_produto)
                    REFERENCES produtos(id)
);