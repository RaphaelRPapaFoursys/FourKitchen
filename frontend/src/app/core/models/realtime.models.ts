export type RealtimeEventType =
  | 'PEDIDO_CRIADO'
  | 'PEDIDO_EM_PREPARO'
  | 'PEDIDO_PRONTO'
  | 'PEDIDO_ENTREGUE'
  | 'PEDIDO_CANCELADO'
  | 'PEDIDO_PROBLEMA_SINALIZADO'
  | 'PEDIDO_PROBLEMA_RESOLVIDO'
  | 'MESA_ABERTA'
  | 'MESA_FECHADA'
  | 'GARCOM_ATRIBUIDO'
  | 'CHAMADA_GARCOM_CRIADA'
  | 'CHAMADA_GARCOM_CONCLUIDA'
  | 'PRODUTO_ALTERADO'
  | 'CATEGORIA_ALTERADA'
  | 'USUARIO_ALTERADO';

export interface RealtimeEvent {
  eventId: string;
  type: RealtimeEventType;
  occurredAt: string;
  aggregateId: string | null;
  data: Record<string, unknown>;
}

export type RealtimeConnectionState =
  | 'disconnected'
  | 'connecting'
  | 'connected'
  | 'offline';

export const RealtimeTopic = {
  cozinhaPedidos: '/topic/cozinha/pedidos',
  garcomOperacao: '/topic/garcom/operacao',
  gestorOperacao: '/topic/gestor/operacao',
  gestorCatalogo: '/topic/gestor/catalogo',
  gestorUsuarios: '/topic/gestor/usuarios',
  cardapio: '/topic/cardapio',
  mesa: (idMesa: number) => `/topic/mesas/${idMesa}`,
} as const;
