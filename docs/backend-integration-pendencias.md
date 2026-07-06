# Pendencias de Integracao do Backend

Este documento lista os ajustes necessarios no backend para fechar o fluxo
frontend -> BFF -> microsservicos sem rotas quebradas ou contratos incompletos.

## 1. Vinculo de mesa no login/JWT

### Problema

As rotas do BFF para o perfil `MESA` dependem de `idMesa` no usuario
autenticado. O BFF tenta extrair esse campo do JWT, mas o `ms-usuarios` nao
inclui `idMesa` no token nem na resposta de login.

### Arquivos relacionados

- `backend/ms-usuarios/src/main/java/br/com/fourkitchen/ms_usuarios/security/JwtService.java`
- `backend/ms-usuarios/src/main/java/br/com/fourkitchen/ms_usuarios/dto/responseDto/LoginResponse.java`
- `backend/bff-restaurante/src/main/java/br/com/fourkitchen/bff_restaurante/security/JwtService.java`
- `backend/bff-restaurante/src/main/java/br/com/fourkitchen/bff_restaurante/service/MesaPedidoService.java`
- `backend/bff-restaurante/src/main/java/br/com/fourkitchen/bff_restaurante/service/MesaChamadaGarcomService.java`

### Ajuste necessario

Adicionar `idMesa` ao modelo de usuario, quando aplicavel, e incluir esse campo:

- no JWT emitido pelo `ms-usuarios`;
- na resposta de login do `ms-usuarios`;
- no DTO consumido pelo BFF.

### Por que e necessario

Sem `idMesa`, o BFF nao consegue validar sessao, criar pedido de mesa ou criar
chamada de garcom para usuario com perfil `MESA`.

### Risco/impacto

Impacta autenticacao e cadastro/gestao de usuarios do tipo `MESA`. E importante
manter `idMesa` opcional para perfis que nao representam uma mesa fisica.

## 2. Nome do produto na fila da cozinha

### Problema

O frontend da cozinha precisa exibir o nome do item. Hoje o contrato da fila
retorna apenas `idProduto`, `quantidade`, `precoUnitario` e `observacao`.

### Arquivos relacionados

- `backend/ms-pedidos/src/main/java/br/com/fourkitchen/ms_pedidos/dto/response/ItemPedidoCozinhaResponse.java`
- `backend/bff-restaurante/src/main/java/br/com/fourkitchen/bff_restaurante/dto/response/ItemFilaCozinhaResponse.java`
- `backend/bff-restaurante/src/main/java/br/com/fourkitchen/bff_restaurante/service/CozinhaService.java`

### Ajuste necessario

Incluir `nomeProduto` no item da fila da cozinha. Existem duas opcoes:

1. `ms-pedidos` consultar/enriquecer o item com o nome do produto.
2. BFF buscar os produtos no `ms-produtos` e montar a resposta com `nomeProduto`.

### Por que e necessario

Sem o nome, a tela consegue funcionar, mas mostra apenas algo como
`Produto #10`, o que reduz a usabilidade da cozinha.

### Risco/impacto

Se o enriquecimento for feito no BFF, aumenta o numero de chamadas ao
`ms-produtos`. Se for feito no `ms-pedidos`, o microsservico passa a depender de
dados de produto para responder a fila.

## 3. Endpoints chamados pelo BFF que nao existem nos microsservicos

### Problema

Alguns clients Feign do BFF apontam para endpoints que nao aparecem nos
controllers atuais dos microsservicos.

### Endpoints ausentes

No `ms-pedidos`:

- `GET /pedidos/resumo-operacao`
- `GET /pedidos/atendimentos/ativos/detalhado`
- `PATCH /pedidos/{id}/entregar`

No `ms-mesas`:

- `GET /api/mesas/resumo-operacao`

No `ms-notificacoes`:

- `PATCH /api/notificacoes/chamadas-garcom/{id}/concluir`
- `GET /api/notificacoes/resumo-operacao`

### Arquivos relacionados

- `backend/bff-restaurante/src/main/java/br/com/fourkitchen/bff_restaurante/client/pedidos/PedidoClient.java`
- `backend/bff-restaurante/src/main/java/br/com/fourkitchen/bff_restaurante/client/mesas/MesaClient.java`
- `backend/bff-restaurante/src/main/java/br/com/fourkitchen/bff_restaurante/client/notificacoes/NotificacaoClient.java`
- `backend/ms-pedidos/src/main/java/br/com/fourkitchen/ms_pedidos/controller/PedidoController.java`
- `backend/ms-mesas/src/main/java/br/com/fourkitchen/ms_mesas/controller/MesaController.java`
- `backend/ms-notificacoes/src/main/java/br/com/fourkitchen/ms_notificacoes/controller/NotificacaoController.java`

### Ajuste necessario

Implementar esses endpoints nos microsservicos ou remover/adaptar os clients e
servicos correspondentes no BFF.

### Por que e necessario

As rotas do BFF de gestor e de conclusao de chamada dependem desses endpoints.
Sem eles, o BFF retorna erro quando essas funcionalidades forem acionadas.

### Risco/impacto

Afeta principalmente painel de gestor, marcacao de pedido entregue e conclusao
segura de chamadas do garcom.

## 4. Fechamento de conta pela mesa

### Problema

O frontend tinha uma acao de fechar conta chamando fechamento de mesa, mas essa
rota pertence ao contexto de gestor no BFF. Nao existe endpoint especifico no
BFF para a mesa solicitar fechamento de conta.

### Ajuste necessario

Definir e implementar uma rota de mesa, por exemplo:

```http
POST /api/mesa/conta/solicitacoes
```

Payload sugerido:

```json
{
  "codigoSessao": 123456
}
```

Comportamento esperado:

- validar token com perfil `MESA`;
- validar `idMesa` e `codigoSessao`;
- criar notificacao `CONTA_SOLICITADA` para `GARCOM` ou `GESTOR`;
- retornar a notificacao/solicitacao criada.

### Por que e necessario

Fechar mesa diretamente e uma acao operacional de gestor. A mesa deveria
solicitar a conta, nao alterar o status da mesa.

### Risco/impacto

Baixo se for implementado como nova rota. Evita dar privilegio indevido ao
perfil `MESA`.
