export type StatusPedidoCozinha =
  | 'ENVIADO_COZINHA'
  | 'EM_PREPARO'
  | 'AGUARDANDO_DECISAO'
  | 'PRONTO'
  | 'ENTREGUE'
  | 'FINALIZADO'
  | 'CANCELADO';

export type StatusProdutoPedido =
  | 'DISPONIVEL'
  | 'FALTA_PRODUTO'
  | 'ERRO'
  | 'INDISPONIVEL'
  | 'CANCELADO'
  | 'REMOVIDO';

export interface ItemFilaCozinhaResponse {
  id: number;
  idProduto: number;
  nomeProduto?: string | null;
  quantidade: number;
  precoUnitario: number;
  observacao: string | null;
}

export interface PedidoFilaCozinhaResponse {
  id: number;
  codigo: number;
  canal: string;
  status: StatusPedidoCozinha;
  idMesa: number | null;
  idAtendimento: number | null;
  dataCriacao: string;
  itens: ItemFilaCozinhaResponse[];
}

export interface PedidoStatusCozinhaResponse {
  id: number;
  codigo: number;
  canal: string;
  status: StatusPedidoCozinha;
  idMesa: number | null;
  idAtendimento: number | null;
}

export interface SinalizarProblemaRequest {
  idPedido: number;
  idProdutoPedido: number;
  statusProdutoPedido: Extract<StatusProdutoPedido, 'FALTA_PRODUTO' | 'ERRO' | 'INDISPONIVEL'>;
}

export interface SinalizarProblemaResponse {
  idPedido: number;
  idProdutoPedido: number;
  statusPedido: StatusPedidoCozinha | string;
  statusProdutoPedido: StatusProdutoPedido;
}

export interface DecisaoProblemaRequest {
  idPedido: number;
  idProdutoPedido: number;
  novoStatusProdutoPedido: Extract<StatusProdutoPedido, 'DISPONIVEL' | 'CANCELADO' | 'REMOVIDO'>;
  pedidoCancelado: boolean;
  idNovoProduto: number | null;
}
