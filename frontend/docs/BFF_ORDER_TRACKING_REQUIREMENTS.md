# Pendencias do BFF para acompanhamento de pedidos da mesa

O frontend ja possui a tela `/mesa/pedidos`, mas ela nao usa dados mockados. Enquanto nao houver endpoint oficial de consulta, a tela exibe apenas pedidos reais que forem salvos localmente apos criacao bem-sucedida.

## Endpoint necessario

Confirmar ou disponibilizar um endpoint para consultar os pedidos do atendimento atual da mesa.

Sugestao:

```http
GET /api/mesa/pedidos?codigoAtendimento=123456
```

Resposta esperada:

```ts
interface PedidoMesaStatusResponse {
  id: number;
  codigo: number;
  canal: 'MESA' | 'TOTEM' | 'GARCOM';
  status:
    | 'ENVIADO_COZINHA'
    | 'EM_PREPARO'
    | 'PRONTO'
    | 'ENTREGUE'
    | 'FINALIZADO'
    | 'CANCELADO'
    | 'AGUARDANDO_DECISAO'
    | 'PROBLEMA_COZINHA';
  idMesa?: number;
  idAtendimento?: number;
  codigoAtendimento?: number;
  dataCriacao?: string;
  itens?: {
    idProduto: number;
    nome?: string;
    quantidade: number;
    observacao?: string;
  }[];
}
```

## Dados ainda faltantes no fluxo `/mesa`

Para enviar `POST /api/mesa/pedidos`, o frontend precisa receber de algum lugar:

- `idMesa`
- `codigoAtendimento`

O BFF deve confirmar se a criacao do pedido de mesa tambem usara `codigoAtendimento`. O frontend nao deve inventar esses valores.

## Ajustes previstos quando o endpoint existir

- Confirmar se `OrderService.getMesaOrders(codigoAtendimento)` usa exatamente `GET /api/mesa/pedidos?codigoAtendimento=...`.
- Chamar `OrderService.createMesaOrder()` no carrinho quando `idMesa/codigoAtendimento` estiverem disponiveis.
- Salvar a resposta real em `CustomerOrderCacheService` apenas como apoio local, nao como fonte principal.

