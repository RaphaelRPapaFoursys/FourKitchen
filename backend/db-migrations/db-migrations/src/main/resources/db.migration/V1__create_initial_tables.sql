CREATE TABLE usuarios(
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE
);

CREATE TABLE produtos(
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(150) NOT NULL,
    descricao VARCHAR(255),
    preco DECIMAL(10, 2) NOT NULL,
    categoria VARCHAR(150),
    disponivel BOOLEAN DEFAULT TRUE
);

CREATE TABLE atendimentos(
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    codigo_sessao INTEGER NOT NULL,
    status_pedido VARCHAR(100)
);

CREATE TABLE mesas(
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    numero INTEGER NOT NULL,
    disponivel BOOLEAN NOT NULL,
    id_atendimento INTEGER,
    CONSTRAINT fk_id_atendimento
            FOREIGN KEY (id_atendimento)
            REFERENCES atendimentos(id)
);

CREATE TABLE pedidos(
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
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
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    id_produto INTEGER,
    descricao VARCHAR(255) NOT NULL,
    preco DECIMAL(10, 2),
    CONSTRAINT fk_id_produto
                    FOREIGN KEY (id_produto)
                    REFERENCES produtos(id)
);

CREATE TABLE itens_pedidos(
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    id_pedido INTEGER,
    id_produto INTEGER,
    CONSTRAINT fk_id_itens_pedido
                    FOREIGN KEY (id_pedido)
                    REFERENCES pedidos(id),
    CONSTRAINT fk_id_usuario
                    FOREIGN KEY (id_produto)
                    REFERENCES produtos(id)
);