DROP TABLE IF EXISTS itens;
DROP TABLE IF EXISTS itens_pedidos;

CREATE TABLE produtos_pedidos(
	id SERIAL PRIMARY KEY,
    quantidade INTEGER NOT NULL,
    id_pedido INTEGER NOT NULL,
	id_produto INTEGER NOT NULL,
    preco_unitario DECIMAL(10, 2) NOT NULL,
    observacao VARCHAR(255),

    CONSTRAINT fk_produtos_pedidos_pedido
    FOREIGN KEY(id_pedido)
    REFERENCES pedidos(id),

    CONSTRAINT fk_produtos_pedidos_produto
	FOREIGN KEY (id_produto)
	REFERENCES produtos(id)
);