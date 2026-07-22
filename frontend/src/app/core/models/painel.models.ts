export type StatusMesaPainel = 'OCUPADA' | 'LIVRE';

export type StatusPedidoPainel = 'EM_PREPARO' | 'PRONTO_ENTREGA' | 'CONTA_ABERTA';

/** Ação primária sugerida pelo card, sempre derivada de status/statusPedido — nunca armazenada. */
export type AcaoMesaPainel = 'VER_PEDIDO' | 'FECHAR_CONTA' | 'ABRIR_MESA' | 'REATRIBUIR_GARCOM';

export interface ResumoAtendimento {
  mesasLivres: number;
  mesasSemGarcom: number;
  emPreparo: number;
  prontos: number;
  problemas: number;
  garconsDisponiveis: number;
  /** Arrecadado ÷ atendimentos ativos; `null` quando não há atendimento ativo. */
  ticketMedio: number | null;
}

export interface Pedido {
  id: number;
  status: string;
  valor: number;
  criadoMinutosAtras: number;
  /** TODO: backend ainda não registra o instante em que o pedido fica pronto; sem esse dado não dá pra calcular. */
  tempoPreparoMinutos: number | null;
  totalItens: number;
}

export interface ItemPedidoDetalheGestor {
  id: number;
  idProduto: number;
  nomeProduto: string;
  quantidade: number;
  precoUnitario: number;
  observacao: string | null;
  status: string;
}

export interface PedidoDetalheGestor {
  id: number;
  codigo: number;
  canal: string;
  status: string;
  dataCriacao: string;
  dataInicioPreparo: string | null;
  dataPronto: string | null;
  itens: ItemPedidoDetalheGestor[];
}

export interface MesaPainel {
  /** Identificador da mesa no ms-mesas — usado nas chamadas de API (não confundir com o número exibido). */
  id: number;
  numero: number;
  status: StatusMesaPainel;
  garcomId: number | null;
  garcom: string | null;
  /** Instante de abertura do atendimento (ISO), vindo do ms-mesas; usado para medir a duração. */
  abertaEm: string | null;
  statusPedido: StatusPedidoPainel | null;
  tempoLabel: string | null;
  tempoMinutos: number | null;
  etapaAtual: number | null;
  totalEtapas: number | null;
  /** Uma mesa pode ter vários pedidos ao longo do atendimento (rodadas de pedido). */
  pedidos: Pedido[];
}

export interface CargaGarcom {
  id: number;
  nome: string;
  mesasAtivas: number;
  cor: string;
}

export interface PedidoRecente {
  pedidoId: number;
  numeroMesa: number;
  garcom: string;
  valor: number;
  /** Duração do atendimento (em minutos) a que este pedido pertence, medida até o fechamento. */
  tempoAtendimentoMinutos: number;
}

/** Atendimento finalizado e persistido pelo serviço de mesas. */
export interface HistoricoAtendimento {
  id: number;
  idAtendimento: number;
  codigoSessao: number;
  idMesa: number;
  numeroMesa: number;
  idGarcom: number | null;
  nomeGarcom: string | null;
  valorFinal: number;
  totalPedidos: number;
  totalItens: number;
  dataAbertura: string;
  dataFechamento: string;
  duracaoMinutos: number;
}

export interface MesaMaisOcupada {
  numeroMesa: number;
  pedidos: number;
}

export interface ResumoExpediente {
  valorTotal: number;
  atendimentosRealizados: number;
  pedidosFeitos: number;
  problemas: number;
  garcomDestaque: { nome: string; atendimentos: number } | null;
  tempoMedioPreparoMin: number | null;
  mesasMaisOcupadas: MesaMaisOcupada[];
}
