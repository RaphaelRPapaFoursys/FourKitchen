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
  nome?: string | null;
  quantidade: number;
  precoUnitario: number | null;
  valorTotal: number;
  observacao?: string;
}

export interface PedidoMesaStatusResponse {
  id: number;
  codigo: number;
  canal: PedidoCanal;
  status: PedidoStatus;
  idMesa: number;
  idAtendimento: number;
  codigoAtendimento: number;
  dataCriacao: string;
  valorTotal: number;
  itens: PedidoStatusItemResponse[];
}

export interface ResumoContaMesaResponse {
  idAtendimento: number;
  codigoAtendimento: number;
  valorFinal: number;
  totalPedidos: number;
  totalItens: number;
}

export interface MesaAtendimentoAtualResponse {
  idMesa: number;
  idAtendimento: number;
  codigoAtendimento: number;
  status: string;
}

export interface PedidoMesaResponse {
  id: number;
  codigo: number;
  canal: 'MESA';
  status: string;
  idMesa: number;
  idAtendimento: number;
  codigoAtendimento?: number;
  dataCriacao?: string;
  itens?: PedidoStatusItemResponse[];
}

export interface PedidoTotemResponse {
  id: number;
  codigo: number;
  canal: 'TOTEM';
  status: string;
}

export type PedidoResponse = PedidoMesaResponse | PedidoTotemResponse;

export interface ApiErrorObject {
  codError: string;
  msgError: string;
}
