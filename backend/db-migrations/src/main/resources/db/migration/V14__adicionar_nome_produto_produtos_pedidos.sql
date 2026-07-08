ALTER TABLE produtos_pedidos
ADD COLUMN IF NOT EXISTS nome_produto VARCHAR(50);

UPDATE produtos_pedidos pp
SET nome_produto = p.nome
FROM produtos p
WHERE pp.id_produto = p.id
  AND pp.nome_produto IS NULL;
