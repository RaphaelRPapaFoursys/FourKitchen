UPDATE produtos_pedidos
SET status_produto_pedido = 'DISPONIVEL'
WHERE status_produto_pedido IS NULL;

ALTER TABLE produtos_pedidos
    ALTER COLUMN status_produto_pedido SET DEFAULT 'DISPONIVEL';

ALTER TABLE produtos_pedidos
    ALTER COLUMN status_produto_pedido SET NOT NULL;
