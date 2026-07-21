export type StatusRetirada =
  | 'ENVIADO_COZINHA'
  | 'EM_PREPARO'
  | 'PRONTO'
  | 'AGUARDANDO_DECISAO'
  | 'PROBLEMA_COZINHA';

export interface PedidoPainelRetiradaResponse {
  codigo: number;
  status: StatusRetirada;
}

export interface PedidoBalcaoResponse extends PedidoPainelRetiradaResponse {
  id: number;
  dataCriacao: string;
  dataInicioPreparo: string | null;
  dataPronto: string | null;
}
