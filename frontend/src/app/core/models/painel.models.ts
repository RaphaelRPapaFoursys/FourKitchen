export type StatusMesaPainel = 'OCUPADA' | 'LIVRE';

export type StatusPedidoPainel = 'EM_PREPARO' | 'PRONTO_ENTREGA' | 'CONTA_ABERTA';

/** Ação primária sugerida pelo card, sempre derivada de status/statusPedido — nunca armazenada. */
export type AcaoMesaPainel = 'VER_PEDIDO' | 'MARCAR_ENTREGUE' | 'FECHAR_CONTA' | 'ABRIR_MESA' | 'REATRIBUIR_GARCOM';

export interface ResumoAtendimento {
  mesasLivres: number;
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

export interface MesaPainel {
  /** Identificador da mesa no ms-mesas — usado nas chamadas de API (não confundir com o número exibido). */
  id: number;
  numero: number;
  status: StatusMesaPainel;
  garcomId: number | null;
  garcom: string | null;
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
  minutosAtras: number;
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
