INSERT INTO usuarios (nome, email, senha, perfil, ativo) VALUES
    ('Carlos', 'carlos@fourkitchen.com', '$2b$10$KeqZIIDFiSW9uqnPfuwPruElUJxmz0yYV4UK.TCFH86rFfXJ4vtoK', 'ADMIN', TRUE),
    ('Amanda', 'amanda@fourkitchen.com', '$2b$10$KeqZIIDFiSW9uqnPfuwPruElUJxmz0yYV4UK.TCFH86rFfXJ4vtoK', 'GARCOM', TRUE),
    ('Thais', 'thais@fourkitchen.com', '$2b$10$KeqZIIDFiSW9uqnPfuwPruElUJxmz0yYV4UK.TCFH86rFfXJ4vtoK', 'COZINHA', TRUE),
    ('Lucas', 'lucas@fourkitchen.com', '$2b$10$KeqZIIDFiSW9uqnPfuwPruElUJxmz0yYV4UK.TCFH86rFfXJ4vtoK', 'GARCOM', TRUE),
    ('Matheus', 'matheus@fourkitchen.com', '$2b$10$KeqZIIDFiSW9uqnPfuwPruElUJxmz0yYV4UK.TCFH86rFfXJ4vtoK', 'COZINHA', TRUE),
    ('Amaral', 'amaral@fourkitchen.com', '$2b$10$KeqZIIDFiSW9uqnPfuwPruElUJxmz0yYV4UK.TCFH86rFfXJ4vtoK', 'GARCOM', TRUE),
    ('Ivan', 'ivan@fourkitchen.com', '$2b$10$KeqZIIDFiSW9uqnPfuwPruElUJxmz0yYV4UK.TCFH86rFfXJ4vtoK', 'COZINHA', TRUE),
    ('Rafael', 'rafael@fourkitchen.com', '$2b$10$KeqZIIDFiSW9uqnPfuwPruElUJxmz0yYV4UK.TCFH86rFfXJ4vtoK', 'ADMIN', TRUE);

INSERT INTO categorias (nome, ativo) VALUES
    ('Entradas', TRUE),
    ('Pratos principais', TRUE),
    ('Bebidas', TRUE),
    ('Sobremesas', TRUE),
    ('Promocoes', TRUE);

INSERT INTO produtos (nome, descricao, preco, id_categoria, disponivel) VALUES
    ('Bruschetta da casa', 'Pao italiano, tomate, manjericao e azeite', 22.90, (SELECT id FROM categorias WHERE nome = 'Entradas'), TRUE),
    ('Bolinho de bacalhau', 'Porcao com seis unidades e molho citrico', 34.90, (SELECT id FROM categorias WHERE nome = 'Entradas'), TRUE),
    ('Risoto de cogumelos', 'Arroz arboreo com mix de cogumelos e parmesao', 58.90, (SELECT id FROM categorias WHERE nome = 'Pratos principais'), TRUE),
    ('File ao molho madeira', 'File mignon com arroz, batatas rusticas e molho madeira', 72.90, (SELECT id FROM categorias WHERE nome = 'Pratos principais'), TRUE),
    ('Frango grelhado', 'Peito de frango com legumes salteados', 46.90, (SELECT id FROM categorias WHERE nome = 'Pratos principais'), TRUE),
    ('Suco natural de laranja', 'Copo de 500 ml', 14.90, (SELECT id FROM categorias WHERE nome = 'Bebidas'), TRUE),
    ('Refrigerante lata', 'Lata de 350 ml', 8.90, (SELECT id FROM categorias WHERE nome = 'Bebidas'), TRUE),
    ('Agua com gas', 'Garrafa de 500 ml', 6.90, (SELECT id FROM categorias WHERE nome = 'Bebidas'), TRUE),
    ('Petit gateau', 'Bolo quente de chocolate com sorvete de creme', 29.90, (SELECT id FROM categorias WHERE nome = 'Sobremesas'), TRUE),
    ('Pudim de leite', 'Fatia individual com calda de caramelo', 18.90, (SELECT id FROM categorias WHERE nome = 'Sobremesas'), TRUE),
    ('Combo executivo', 'Prato principal, bebida e sobremesa do dia', 59.90, (SELECT id FROM categorias WHERE nome = 'Promocoes'), TRUE);

INSERT INTO mesas (numero, disponivel) VALUES
    (1, FALSE),
    (2, FALSE),
    (3, TRUE),
    (4, TRUE),
    (5, FALSE),
    (6, TRUE),
    (7, TRUE),
    (8, TRUE);

INSERT INTO atendimentos (codigo_sessao, status_pedido, id_mesa, id_garcom, data_abertura, data_fechamento) VALUES
    (1001, 'EM_ANDAMENTO', (SELECT id FROM mesas WHERE numero = 1), (SELECT id FROM usuarios WHERE email = 'amanda@fourkitchen.com'), '2026-07-01 11:35:00', NULL),
    (1002, 'EM_ANDAMENTO', (SELECT id FROM mesas WHERE numero = 2), (SELECT id FROM usuarios WHERE email = 'lucas@fourkitchen.com'), '2026-07-01 12:10:00', NULL),
    (1003, 'FINALIZADO', (SELECT id FROM mesas WHERE numero = 5), (SELECT id FROM usuarios WHERE email = 'amaral@fourkitchen.com'), '2026-07-01 10:20:00', '2026-07-01 11:15:00');

UPDATE mesas
SET id_atendimento = (SELECT id FROM atendimentos WHERE codigo_sessao = 1001)
WHERE numero = 1;

UPDATE mesas
SET id_atendimento = (SELECT id FROM atendimentos WHERE codigo_sessao = 1002)
WHERE numero = 2;

UPDATE mesas
SET id_atendimento = (SELECT id FROM atendimentos WHERE codigo_sessao = 1003)
WHERE numero = 5;

INSERT INTO pedidos (codigo, canal, status, id_mesa, id_usuario, id_atendimento) VALUES
    (5001, 'GARCOM', 'EM_PREPARO', (SELECT id FROM mesas WHERE numero = 1), (SELECT id FROM usuarios WHERE email = 'amanda@fourkitchen.com'), (SELECT id FROM atendimentos WHERE codigo_sessao = 1001)),
    (5002, 'MESA', 'ENVIADO_COZINHA', (SELECT id FROM mesas WHERE numero = 2), (SELECT id FROM usuarios WHERE email = 'lucas@fourkitchen.com'), (SELECT id FROM atendimentos WHERE codigo_sessao = 1002)),
    (5003, 'TOTEM', 'FINALIZADO', (SELECT id FROM mesas WHERE numero = 5), (SELECT id FROM usuarios WHERE email = 'amaral@fourkitchen.com'), (SELECT id FROM atendimentos WHERE codigo_sessao = 1003));

INSERT INTO produtos_pedidos (quantidade, id_pedido, id_produto, preco_unitario, observacao) VALUES
    (2, (SELECT id FROM pedidos WHERE codigo = 5001), (SELECT id FROM produtos WHERE nome = 'Bruschetta da casa'), 22.90, 'Sem cebola'),
    (1, (SELECT id FROM pedidos WHERE codigo = 5001), (SELECT id FROM produtos WHERE nome = 'Risoto de cogumelos'), 58.90, NULL),
    (1, (SELECT id FROM pedidos WHERE codigo = 5001), (SELECT id FROM produtos WHERE nome = 'Suco natural de laranja'), 14.90, 'Sem gelo'),
    (1, (SELECT id FROM pedidos WHERE codigo = 5002), (SELECT id FROM produtos WHERE nome = 'File ao molho madeira'), 72.90, 'Carne ao ponto'),
    (2, (SELECT id FROM pedidos WHERE codigo = 5002), (SELECT id FROM produtos WHERE nome = 'Refrigerante lata'), 8.90, NULL),
    (1, (SELECT id FROM pedidos WHERE codigo = 5003), (SELECT id FROM produtos WHERE nome = 'Combo executivo'), 59.90, NULL),
    (1, (SELECT id FROM pedidos WHERE codigo = 5003), (SELECT id FROM produtos WHERE nome = 'Pudim de leite'), 18.90, 'Adicionar calda extra');
