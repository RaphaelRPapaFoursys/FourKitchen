export type PeriodoDashboard =
  | 'ULTIMA_HORA'
  | 'TURNO_ATUAL'
  | 'HOJE'
  | 'ONTEM'
  | 'ULTIMOS_7_DIAS'
  | 'ULTIMOS_30_DIAS'
  | 'PERSONALIZADO';

export type PeriodoRankingProdutos = 'HOJE' | 'ULTIMOS_7_DIAS' | 'ULTIMOS_30_DIAS';

export type StatusPedidoDashboard =
  | 'ENVIADO_COZINHA'
  | 'EM_PREPARO'
  | 'PRONTO'
  | 'ENTREGUE'
  | 'FINALIZADO'
  | 'CANCELADO'
  | 'PROBLEMA_COZINHA'
  | 'AGUARDANDO_DECISAO';

export interface FiltrosDashboard {
  periodo: PeriodoDashboard;
  dataInicial: string;
  dataFinal: string;
  canal: CanalPedido | null;
  idMesa: number | null;
  status: StatusPedidoDashboard | null;
}

export const FILTROS_DASHBOARD_INICIAIS: FiltrosDashboard = {
  periodo: 'HOJE',
  dataInicial: '',
  dataFinal: '',
  canal: null,
  idMesa: null,
  status: null,
};

export interface VolumePedidoHorarioItem {
  horario: string;
  quantidade: number;
}

export interface VolumePedidosHorarioResponse {
  periodo: PeriodoDashboard;
  totalPedidos: number;
  horarioPico: string | null;
  quantidadeNoPico: number;
  dados: VolumePedidoHorarioItem[];
}

export interface ProblemaCozinhaMotivoItem {
  motivo: string;
  descricao: string;
  quantidade: number;
  percentual: number;
}

export interface ProblemasCozinhaMotivoResponse {
  periodo: PeriodoDashboard;
  totalProblemas: number;
  motivoMaisFrequente: string | null;
  dados: ProblemaCozinhaMotivoItem[];
}

export type CanalPedido = 'TOTEM' | 'MESA' | 'GARCOM';

export interface PedidoCanalItem {
  canal: CanalPedido;
  descricao: string;
  quantidade: number;
  percentual: number;
}

export interface PedidosCanalResponse {
  periodo: PeriodoDashboard;
  totalPedidos: number;
  dados: PedidoCanalItem[];
}

export interface RankingProdutoItem {
  idProduto: number;
  nomeProduto: string;
  quantidade: number;
}

export interface RankingProdutosResponse {
  periodo: PeriodoRankingProdutos;
  dados: RankingProdutoItem[];
}

export type EstadoGrafico<T> =
  | { status: 'carregando'; dados: null }
  | { status: 'sucesso'; dados: T }
  | { status: 'erro'; dados: null; mensagem: string };
