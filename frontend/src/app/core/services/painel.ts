import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { computed, DestroyRef, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../environments/environment';
import { nivelCargaGarcom, resolverAcaoPrimaria, resolverCriticidadeMesa } from '../constants/urgencia.constants';
import {
  AcaoMesaPainel,
  CargaGarcom,
  MesaPainel,
  Pedido,
  PedidoRecente,
  ResumoAtendimento,
  StatusPedidoPainel,
} from '../models/painel.models';
import {
  AtendimentoFinalizado,
  contarMesasComContaAberta,
  contarPedidosPendentesEntrega,
  mesasComAtendimentoAtivo,
  montarResumoExpediente,
} from './expediente.util';

interface PedidoGestorApiResponse {
  id: number;
  status: string;
  valor: number;
  criadoEm: string;
  totalItens: number;
}

interface MesaGestorApiResponse {
  id: number;
  numero: number;
  status: 'OCUPADA' | 'DISPONIVEL';
  garcomId: number | null;
  garcomNome: string | null;
  codigoSessao: number | null;
  dataAbertura: string | null;
  dataFechamento: string | null;
  pedidos: PedidoGestorApiResponse[];
}

interface GarcomApiResponse {
  id: number;
  nome: string;
  email: string;
}

/** Status de pedido (ms-pedidos) que ainda representam preparo em andamento. */
const STATUS_PEDIDO_EM_PREPARO = ['ENVIADO_COZINHA', 'EM_PREPARO', 'AGUARDANDO_DECISAO'];

const PALETA_CORES_GARCOM = ['#2f6fed', '#8b5cf6', '#2fb673', '#f59e0b', '#ec4899', '#0891b2'];

const MAXIMO_ULTIMOS_PEDIDOS = 5;

const INTERVALO_POLLING_MS = 2000;

/** Garçons mudam raramente no expediente; a recarga imediata é sob demanda em recarregarGarcons. */
const INTERVALO_POLLING_GARCONS_MS = 20000;

/** Etapa do fluxo de preparo por status de pedido (ms-pedidos) — usada na barra de progresso do card. */
const ETAPA_POR_STATUS_PEDIDO: Record<string, number> = {
  ENVIADO_COZINHA: 1,
  EM_PREPARO: 2,
  AGUARDANDO_DECISAO: 2,
  PRONTO: 3,
  ENTREGUE: 4,
};

const TOTAL_ETAPAS_PEDIDO = 4;

@Injectable({
  providedIn: 'root',
})
export class PainelService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/gestor`;

  private readonly destroyRef = inject(DestroyRef);

  private readonly mesasSignal = signal<MesaPainel[]>([]);
  private readonly garconsSignal = signal<GarcomApiResponse[]>([]);
  private readonly carregandoSignal = signal(true);
  private readonly acaoEmAndamentoSignal = signal(false);
  private readonly descricaoAcaoSignal = signal<string | null>(null);
  private readonly mensagemErroSignal = signal<string | null>(null);

  readonly mesas = this.mesasSignal.asReadonly();
  readonly carregando = this.carregandoSignal.asReadonly();
  readonly acaoEmAndamento = this.acaoEmAndamentoSignal.asReadonly();
  readonly descricaoAcao = this.descricaoAcaoSignal.asReadonly();
  readonly mensagemErro = this.mensagemErroSignal.asReadonly();

  constructor() {
    void Promise.all([this.atualizarMesas(), this.atualizarGarcons()]).finally(() =>
      this.carregandoSignal.set(false),
    );

    const idPollingMesas = setInterval(() => {
      if (this.expedienteFechado() || this.acaoEmAndamentoSignal()) return;
      void this.atualizarMesas();
    }, INTERVALO_POLLING_MS);

    const idPollingGarcons = setInterval(() => {
      if (this.expedienteFechado() || this.acaoEmAndamentoSignal()) return;
      void this.atualizarGarcons();
    }, INTERVALO_POLLING_GARCONS_MS);

    this.destroyRef.onDestroy(() => {
      clearInterval(idPollingMesas);
      clearInterval(idPollingGarcons);
    });
  }

  limparErro(): void {
    this.mensagemErroSignal.set(null);
  }

  async recarregarGarcons(): Promise<void> {
    try {
      await this.atualizarGarcons();
    } catch {
      // uma falha na recarga não deve travar a abertura do modal
    }
  }

  readonly cargaGarcons = computed<CargaGarcom[]>(() => {
    const mesasAtivasPorGarcom = new Map<number, number>();

    for (const mesa of this.mesasSignal()) {
      if (mesa.status !== 'OCUPADA' || mesa.garcomId === null) continue;
      mesasAtivasPorGarcom.set(mesa.garcomId, (mesasAtivasPorGarcom.get(mesa.garcomId) ?? 0) + 1);
    }

    return this.garconsSignal()
      .map(garcom => ({
        id: garcom.id,
        nome: garcom.nome,
        mesasAtivas: mesasAtivasPorGarcom.get(garcom.id) ?? 0,
        cor: PALETA_CORES_GARCOM[garcom.id % PALETA_CORES_GARCOM.length],
      }))
      .sort((a, b) => b.mesasAtivas - a.mesasAtivas || a.nome.localeCompare(b.nome));
  });

  readonly resumo = computed<ResumoAtendimento>(() => {
    const mesas = this.mesasSignal();
    const atendimentosAtivos = this.totalAtendimentosAtivos();

    return {
      mesasLivres: mesas.filter(mesa => mesa.status === 'LIVRE').length,
      emPreparo: mesas.filter(mesa => mesa.statusPedido === 'EM_PREPARO').length,
      prontos: mesas.filter(mesa => mesa.statusPedido === 'PRONTO_ENTREGA').length,
      problemas: mesas.filter(mesa => resolverCriticidadeMesa(mesa) === 'critico').length,
      garconsDisponiveis: this.cargaGarcons().filter(
        garcom => nivelCargaGarcom(garcom.mesasAtivas) !== 'ALTA',
      ).length,
      ticketMedio: atendimentosAtivos === 0 ? null : this.totalArrecadadoAtendimentosAtivos() / atendimentosAtivos,
    };
  });

  /** Pedidos de atendimentos já fechados no expediente (mais recentes primeiro). */
  readonly ultimosPedidos = computed<PedidoRecente[]>(() =>
    [...this.historicoExpediente()]
      .reverse()
      .flatMap(atendimento =>
        atendimento.pedidos.map(pedido => ({
          pedidoId: pedido.id,
          numeroMesa: atendimento.numeroMesa,
          garcom: atendimento.garcom,
          valor: pedido.valor,
          tempoAtendimentoMinutos: atendimento.duracaoMinutos ?? 0,
        })),
      )
      .slice(0, MAXIMO_ULTIMOS_PEDIDOS),
  );

  /** Conta mesas com atendimento ativo (não linhas da lista de últimos pedidos). */
  readonly totalAtendimentosAtivos = computed(() => mesasComAtendimentoAtivo(this.mesasSignal()).length);

  /** Soma o valor de todos os pedidos das mesas ativas, não só dos exibidos em `ultimosPedidos`. */
  readonly totalArrecadadoAtendimentosAtivos = computed(() =>
    mesasComAtendimentoAtivo(this.mesasSignal()).reduce(
      (total, mesa) => total + mesa.pedidos.reduce((soma, pedido) => soma + pedido.valor, 0),
      0,
    ),
  );

  /** Quantidade de pedidos em mesas cujo status ainda não chegou à entrega (bloqueia o fechamento do expediente). */
  readonly pedidosPendentesEntrega = computed(() => contarPedidosPendentesEntrega(this.mesasSignal()));

  /** Mesas ocupadas com conta aberta (receita não cobrada) também bloqueiam o fechamento do expediente. */
  readonly mesasComContaAberta = computed(() => contarMesasComContaAberta(this.mesasSignal()));

  readonly podeFecharExpediente = computed(
    () => this.pedidosPendentesEntrega() === 0 && this.mesasComContaAberta() === 0,
  );

  /** TODO: sem persistência no backend ainda; o estado do expediente não sobrevive a um reload. */
  readonly expedienteFechado = signal(false);

  /** Atendimentos cuja conta já foi fechada — mantém as métricas do expediente após liberar a mesa. */
  private readonly historicoExpediente = signal<AtendimentoFinalizado[]>([]);

  readonly resumoExpediente = computed(() =>
    montarResumoExpediente(this.mesasSignal(), this.historicoExpediente()),
  );

  fecharExpediente(): void {
    if (!this.podeFecharExpediente()) return;
    this.expedienteFechado.set(true);
  }

  reabrirExpediente(): void {
    this.expedienteFechado.set(false);
  }

  acaoPrimaria(mesa: MesaPainel): { tipo: AcaoMesaPainel; label: string } {
    return resolverAcaoPrimaria(mesa);
  }

  valorContaMesa(mesa: MesaPainel): number | null {
    if (mesa.pedidos.length === 0) return null;
    return mesa.pedidos.reduce((total, pedido) => total + pedido.valor, 0);
  }

  totalItensMesa(mesa: MesaPainel): number | null {
    if (mesa.pedidos.length === 0) return null;
    return mesa.pedidos.reduce((total, pedido) => total + pedido.totalItens, 0);
  }

  async abrirMesa(idMesa: number, idGarcom: number): Promise<void> {
    if (this.expedienteFechado()) return;

    await this.executarComFeedback('Abrindo mesa...', async () => {
      await firstValueFrom(this.http.patch(`${this.baseUrl}/mesas/${idMesa}/abrir`, {}));
      await firstValueFrom(this.http.patch(`${this.baseUrl}/mesas/${idMesa}/atribuir-garcom`, { garcomId: idGarcom }));
      await this.atualizarMesas();
    });
  }

  async fecharConta(idMesa: number): Promise<void> {
    if (this.expedienteFechado()) return;

    this.registrarNoHistoricoAntesDeFechar(idMesa);

    await this.executarComFeedback('Fechando conta...', async () => {
      await firstValueFrom(this.http.patch(`${this.baseUrl}/mesas/${idMesa}/fechar`, {}));
      await this.atualizarMesas();
    });
  }

  async marcarEntregue(idMesa: number): Promise<void> {
    if (this.expedienteFechado()) return;

    await this.executarComFeedback('Confirmando entrega...', async () => {
      await firstValueFrom(this.http.patch(`${this.baseUrl}/mesas/${idMesa}/marcar-entregue`, {}));
      await this.atualizarMesas();
    });
  }

  async reatribuirGarcom(idMesa: number, idGarcom: number): Promise<void> {
    if (this.expedienteFechado()) return;

    await this.executarComFeedback('Atribuindo garçom...', async () => {
      await firstValueFrom(this.http.patch(`${this.baseUrl}/mesas/${idMesa}/atribuir-garcom`, { garcomId: idGarcom }));
      await this.atualizarMesas();
    });
  }

  private async executarComFeedback(descricao: string, acao: () => Promise<void>): Promise<void> {
    this.acaoEmAndamentoSignal.set(true);
    this.descricaoAcaoSignal.set(descricao);
    this.mensagemErroSignal.set(null);

    try {
      await acao();
    } catch (erro) {
      this.mensagemErroSignal.set(this.extrairMensagemErro(erro));
    } finally {
      this.acaoEmAndamentoSignal.set(false);
      this.descricaoAcaoSignal.set(null);
    }
  }

  private extrairMensagemErro(erro: unknown): string {
    if (erro instanceof HttpErrorResponse) {
      const corpo = erro.error as { msgError?: string } | null;
      if (corpo?.msgError) return corpo.msgError;
    }

    return 'Ocorreu um erro. Tente novamente.';
  }

  private registrarNoHistoricoAntesDeFechar(idMesa: number): void {
    const mesa = this.mesasSignal().find(item => item.id === idMesa);
    if (!mesa || mesa.garcom === null || mesa.pedidos.length === 0) return;

    this.historicoExpediente.update(historico => [
      ...historico,
      {
        numeroMesa: mesa.numero,
        garcom: mesa.garcom as string,
        pedidos: mesa.pedidos,
        duracaoMinutos: mesa.abertaEm === null ? 0 : minutosDesde(mesa.abertaEm),
      },
    ]);
  }

  private async atualizarMesas(): Promise<void> {
    const mesas = await firstValueFrom(this.http.get<MesaGestorApiResponse[]>(`${this.baseUrl}/mesas`));
    this.mesasSignal.set(mesas.map(mapearMesa));
  }

  private async atualizarGarcons(): Promise<void> {
    const garcons = await firstValueFrom(this.http.get<GarcomApiResponse[]>(`${this.baseUrl}/garcons`));
    this.garconsSignal.set(garcons);
  }
}

function mapearMesa(mesa: MesaGestorApiResponse): MesaPainel {
  const pedidos = mesa.pedidos.map(mapearPedido);
  const statusPedido = derivarStatusPedido(pedidos);
  const tempoMinutos = pedidos.length === 0 ? null : Math.min(...pedidos.map(pedido => pedido.criadoMinutosAtras));

  return {
    id: mesa.id,
    numero: mesa.numero,
    status: mesa.status === 'OCUPADA' ? 'OCUPADA' : 'LIVRE',
    garcomId: mesa.garcomId,
    garcom: mesa.garcomNome,
    abertaEm: mesa.dataAbertura,
    statusPedido,
    tempoLabel: tempoMinutos === null ? null : 'Em andamento há',
    tempoMinutos,
    etapaAtual: etapaAtualPedidos(pedidos),
    totalEtapas: pedidos.length === 0 ? null : TOTAL_ETAPAS_PEDIDO,
    pedidos,
  };
}

/** Etapa mais atrasada entre os pedidos ativos da mesa — representa o pior caso do atendimento. */
function etapaAtualPedidos(pedidos: Pedido[]): number | null {
  const etapas = pedidos
    .map(pedido => ETAPA_POR_STATUS_PEDIDO[pedido.status])
    .filter((etapa): etapa is number => etapa !== undefined);

  return etapas.length === 0 ? null : Math.min(...etapas);
}

function mapearPedido(pedido: PedidoGestorApiResponse): Pedido {
  return {
    id: pedido.id,
    status: pedido.status,
    valor: pedido.valor,
    criadoMinutosAtras: minutosDesde(pedido.criadoEm),
    // TODO: backend ainda não registra o instante em que o pedido fica pronto (item de métricas, fora do escopo atual).
    tempoPreparoMinutos: null,
    totalItens: pedido.totalItens,
  };
}

function minutosDesde(dataIso: string): number {
  return Math.max(0, Math.round((Date.now() - new Date(dataIso).getTime()) / 60000));
}

function derivarStatusPedido(pedidos: Pedido[]): StatusPedidoPainel | null {
  if (pedidos.length === 0) return null;
  if (pedidos.some(pedido => STATUS_PEDIDO_EM_PREPARO.includes(pedido.status))) return 'EM_PREPARO';
  if (pedidos.some(pedido => pedido.status === 'PRONTO')) return 'PRONTO_ENTREGA';
  return 'CONTA_ABERTA';
}
