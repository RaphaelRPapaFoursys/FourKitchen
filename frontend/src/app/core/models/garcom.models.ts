export interface PedidoAtivoMesaResponse {
  id: number;
  codigo: number;
  canal: string;
  status: string;
  idAtendimento: number | null;
}

export interface ChamadaPendenteMesaResponse {
  id: number;
  tipo: string;
  mensagem: string;
  data: string;
}

export interface MesaGarcomResponse {
  idMesa: number;
  numero: number;
  status: 'DISPONIVEL' | 'OCUPADA' | string;
  idAtendimento: number | null;
  codigoSessao: number | null;
  idGarcom: number | null;
  dataAbertura: string | null;
  pedidosAtivos: PedidoAtivoMesaResponse[];
  chamadasPendentes: ChamadaPendenteMesaResponse[];
  possuiChamadaPendente: boolean;
}

export interface ItemPedidoDetalheGarcomResponse {
  id: number;
  idProduto: number;
  nomeProduto: string;
  quantidade: number;
  precoUnitario: number;
  observacao: string | null;
  status: string;
}

export interface PedidoDetalheGarcomResponse {
  id: number;
  codigo: number;
  canal: string;
  status: string;
  dataCriacao: string;
  dataInicioPreparo: string | null;
  dataPronto: string | null;
  itens: ItemPedidoDetalheGarcomResponse[];
}

export interface ProblemaPedidoGarcomResponse {
  idPedido: number;
  idProdutoPedido: number;
  tipo: 'FALTA_PRODUTO' | 'ERRO' | 'INDISPONIVEL' | string;
  mensagem: string;
}

export interface MesaGarcomDetalheResponse {
  mesa: {
    idMesa: number;
    numero: number;
    status: string;
    idAtendimento: number;
    codigoSessao: number;
    dataAbertura: string;
  };
  conta: {
    subtotal: number;
    total: number;
    totalPedidos: number;
    totalItens: number;
  };
  pedidos: PedidoDetalheGarcomResponse[];
  problemas: ProblemaPedidoGarcomResponse[];
}

export interface MesaProblemasGarcomResponse {
  mesa: MesaGarcomDetalheResponse['mesa'];
  pedidos: PedidoDetalheGarcomResponse[];
  problemas: ProblemaPedidoGarcomResponse[];
}

export interface DecisaoProblemaGarcomRequest {
  idPedido: number;
  idProdutoPedido: number;
  novoStatusProdutoPedido: 'DISPONIVEL' | 'REMOVIDO';
  pedidoCancelado: boolean;
  idNovoProduto: number | null;
}

export interface ItemPedidoGarcomRequest {
  idProduto: number;
  quantidade: number;
  precoUnitario: number;
  observacao?: string;
}

export interface CriarPedidoGarcomRequest {
  idMesa: number;
  itens: ItemPedidoGarcomRequest[];
}

export interface PedidoGarcomResponse {
  id: number;
  codigo: number;
  canal: string;
  status: string;
  idMesa: number;
  idGarcom: number;
  idAtendimento: number;
}
