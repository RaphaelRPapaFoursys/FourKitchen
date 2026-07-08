export type PedidoStatus =
  | 'ENVIADO_COZINHA'
  | 'EM_PREPARO'
  | 'PRONTO'
  | 'ENTREGUE'
  | 'FINALIZADO'
  | 'CANCELADO'
  | 'AGUARDANDO_DECISAO'
  | 'PROBLEMA_COZINHA';

export type PedidoCanal = 'MESA' | 'TOTEM' | 'GARCOM';

export interface PedidoStatusItemResponse {
  idProduto: number;
  nome?: string;
  quantidade: number;
  observacao?: string;
}

export interface PedidoMesaStatusResponse {
  id: number;
  codigo: number;
  canal: PedidoCanal;
  status: PedidoStatus;
  idMesa?: number;
  idAtendimento?: number;
  codigoAtendimento?: number;
  dataCriacao?: string;
  itens?: PedidoStatusItemResponse[];
}

export interface PedidoResponse {
  id?: number;
  codigo?: number;
  canal?: PedidoCanal;
  status?: PedidoStatus;
  idMesa?: number;
  idAtendimento?: number;
  codigoAtendimento?: number;
  dataCriacao?: string;
  itens?: PedidoStatusItemResponse[];
  mensagem?: string;
}
