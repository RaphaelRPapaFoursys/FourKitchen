export type TipoEventoRealtime =
  | 'PEDIDO_CRIADO'
  | 'PEDIDO_ATUALIZADO'
  | 'PEDIDO_PRONTO'
  | 'PROBLEMA_COZINHA'
  | 'CHAMADA_GARCOM';

export interface EventoRealtime<T = unknown> {
  tipo: TipoEventoRealtime;
  idPedido: number | null;
  idMesa: number | null;
  idAtendimento: number | null;
  idGarcom: number | null;
  data: string;
  payload: T;
}
