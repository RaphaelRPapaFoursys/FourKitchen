DROP TABLE IF EXISTS itens;
DROP TABLE IF EXISTS itens_pedidos;

CREATE TABLE produtos_pedidos(
	id INT AUTO_INCREMENT PRIMARY KEY,
    quantidade INT NOT NULL,
    id_pedido INT NOT NULL,
	id_produto INT NOT NULL,
    preco_unitario DECIMAL(10, 2) NOT NULL,
    observacao VARCHAR(255),

    CONSTRAINT fk_produtos_pedidos_pedido
    FOREIGN KEY(id_pedido)
    REFERENCES pedidos(id),

    CONSTRAINT fk_produtos_pedidos_produto
	FOREIGN KEY (id_produto)
	REFERENCES produtos(id)
);