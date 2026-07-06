export type StatusPedidoCozinha =
  | 'ENVIADO_COZINHA'
  | 'EM_PREPARO'
  | 'PRONTO'
  | 'ENTREGUE'
  | 'FINALIZADO'
  | 'CANCELADO';

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
