import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { computed, DestroyRef, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../environments/environment';
import { nivelCargaGarcom, resolverAcaoPrimaria } from '../constants/urgencia.constants';
import {
  AcaoMesaPainel,
  CargaGarcom,
  HistoricoAtendimento,
  MesaPainel,
  Pedido,
  PedidoDetalheGestor,
  PedidoRecente,
  ResumoAtendimento,
  StatusPedidoPainel,
} from '../models/painel.models';
import {
  contarMesasComContaAberta,
  contarPedidosPendentesEntrega,
  mesasComAtendimentoAtivo,
  montarResumoExpediente,
} from './expediente.util';
import { mesaCorrespondeBuscaParcial, numeroMesaBusca } from '../utils/operational-search';

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

interface MesaGestorPaginadaApiResponse {
  content: MesaGestorApiResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

interface CargaGarcomApiResponse {
  id: number;
  nome: string;
  mesasAtivas: number;
}

interface ResumoPainelApiResponse {
  mesasLivres: number;
  mesasSemGarcom: number;
  emPreparo: number;
  prontos: number;
  problemas: number;
  ticketMedio: number | null;
  cargaGarcons: CargaGarcomApiResponse[];
}

export type FiltroEstadoPainel =
  | 'ATENCAO'
  | 'PROBLEMAS'
  | 'PRONTOS'
  | 'EM_PREPARO'
  | 'LIVRE'
  | 'SEM_GARCOM'
  | 'CONTA_ABERTA'
  | 'ATRASADAS'
  | 'AGUARDANDO_PEDIDO';

export type OrdenacaoPainel = 'criticidade' | 'numero,asc' | 'numero,desc' | 'valor,desc' | 'valor,asc';

export interface ConsultaMesasPainel {
  page: number;
  size: number;
  sort: OrdenacaoPainel;
  filtroEstado: FiltroEstadoPainel | null;
  garcomId: number | null;
  busca: string;
}

interface PaginacaoMesasPainel {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

/** Status de pedido (ms-pedidos) que ainda representam preparo em andamento. */
const STATUS_PEDIDO_EM_PREPARO = ['ENVIADO_COZINHA', 'EM_PREPARO', 'AGUARDANDO_DECISAO'];

const PALETA_CORES_GARCOM = ['#2f6fed', '#8b5cf6', '#2fb673', '#f59e0b', '#ec4899', '#0891b2'];

const MAXIMO_ULTIMOS_PEDIDOS = 5;

/** Chaves de localStorage do estado do expediente (paliativo até o backend persistir o expediente). */
const STORAGE_EXPEDIENTE_FECHADO = 'fk.expediente.fechado';
const STORAGE_EXPEDIENTE_INICIO = 'fk.expediente.inicio';

const INTERVALO_POLLING_MS = 10000;

/** Intervalo entre os prefetch das páginas vizinhas — evita disparar as duas requisições coladas. */
const PREFETCH_INTERVALO_MS = 400;

/** De quanto em quanto o prefetch confere se a ação do usuário terminou para retomar. */
const PREFETCH_CHECK_MS = 300;

/** A partir desse tamanho de página (mesas exibidas) o prefetch/cache encolhe para 1 vizinha de cada lado. */
const LIMIAR_PAGINA_GRANDE = 30;

/** Etapa do fluxo de preparo por status de pedido (ms-pedidos) — usada na barra de progresso do card. */
const ETAPA_POR_STATUS_PEDIDO: Record<string, number> = {
  ENVIADO_COZINHA: 1,
  EM_PREPARO: 2,
  AGUARDANDO_DECISAO: 2,
  PRONTO: 3,
  ENTREGUE: 4,
};

const TOTAL_ETAPAS_PEDIDO = 4;

const CONSULTA_INICIAL: ConsultaMesasPainel = {
  page: 0,
  size: 12,
  sort: 'criticidade',
  filtroEstado: null,
  garcomId: null,
  busca: '',
};

const RESUMO_INICIAL: ResumoAtendimento = {
  mesasLivres: 0,
  mesasSemGarcom: 0,
  emPreparo: 0,
  prontos: 0,
  problemas: 0,
  garconsDisponiveis: 0,
  ticketMedio: null,
};

const PAGINACAO_INICIAL: PaginacaoMesasPainel = {
  page: 0,
  size: 12,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
};

/**
 * Fornecido no escopo do componente Gestor (providers: [PainelService]), não em 'root'.
 * Assim o polling só roda enquanto a tela do gestor está montada e o DestroyRef
 * limpa os intervalos ao sair dela.
 */
@Injectable()
export class PainelService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/gestor`;

  private readonly destroyRef = inject(DestroyRef);

  private readonly mesasSignal = signal<MesaPainel[]>([]);
  private readonly cargaGarconsSignal = signal<CargaGarcom[]>([]);
  private readonly resumoSignal = signal<ResumoAtendimento>(RESUMO_INICIAL);
  private readonly paginacaoSignal = signal<PaginacaoMesasPainel>(PAGINACAO_INICIAL);
  private readonly consultaSignal = signal<ConsultaMesasPainel>(CONSULTA_INICIAL);
  private readonly carregandoSignal = signal(true);
  private readonly carregandoMesasSignal = signal(false);
  private readonly acaoEmAndamentoSignal = signal(false);
  private readonly descricaoAcaoSignal = signal<string | null>(null);
  private readonly mensagemErroSignal = signal<string | null>(null);

  /** Atendimentos finalizados vindos do backend (mais recentes primeiro), fonte de últimos pedidos e do resumo do expediente. */
  private readonly historicoAtendimentosSignal = signal<HistoricoAtendimento[]>([]);

  /** Sequência das requisições em voo: só a mais recente pode escrever na tela (evita piscar por resposta atrasada). */
  private sequenciaMesas = 0;
  private sequenciaResumo = 0;
  private sequenciaHistorico = 0;
  private atualizacaoPainelPromise: Promise<void> | null = null;
  private consultaMesasEmAndamento = false;

  /** Cache das páginas de mesas: guarda só a atual e as vizinhas dentro do raio (ver raioPrefetch) do filtro corrente. */
  private readonly cachePaginas = new Map<string, MesaGestorPaginadaApiResponse>();

  /** Base atual (filtro/ordenação/busca/tamanho, sem a página): mudou ⇒ o cache antigo é descartado. */
  private baseAtual: string | null = baseConsulta(CONSULTA_INICIAL);

  /** Geração do prefetch em voo: incrementar cancela o loop anterior (nova consulta, mutação ou destroy). */
  private geracaoPrefetch = 0;

  readonly mesas = this.mesasSignal.asReadonly();
  readonly resumo = this.resumoSignal.asReadonly();
  readonly cargaGarcons = this.cargaGarconsSignal.asReadonly();
  readonly historicoAtendimentos = this.historicoAtendimentosSignal.asReadonly();
  readonly totalElementos = computed(() => this.paginacaoSignal().totalElements);
  readonly totalPaginas = computed(() => Math.max(1, this.paginacaoSignal().totalPages));
  readonly paginaEfetiva = computed(() => Math.min(this.paginacaoSignal().page + 1, this.totalPaginas()));
  readonly temPaginaAnterior = computed(() => !this.paginacaoSignal().first);
  readonly temProximaPagina = computed(() => !this.paginacaoSignal().last);
  readonly carregando = this.carregandoSignal.asReadonly();
  /** Loading da grade: só liga quando o usuário troca filtro/página e a página não está em cache. */
  readonly carregandoMesas = this.carregandoMesasSignal.asReadonly();
  readonly acaoEmAndamento = this.acaoEmAndamentoSignal.asReadonly();
  readonly descricaoAcao = this.descricaoAcaoSignal.asReadonly();
  readonly mensagemErro = this.mensagemErroSignal.asReadonly();

  constructor() {
    void this.atualizarPainel()
      // Terminado o load inicial, começa a puxar as páginas vizinhas para o cache em segundo plano.
      .then(() => this.agendarPrefetchVizinhos())
      .catch(() => this.mensagemErroSignal.set('Não foi possível carregar o painel. Verifique os serviços e tente novamente.'))
      .finally(() => this.carregandoSignal.set(false));

    const idPollingMesas = setInterval(() => {
      if (
        this.carregandoSignal() ||
        this.expedienteFechado() ||
        this.acaoEmAndamentoSignal() ||
        this.atualizacaoPainelPromise !== null ||
        this.consultaMesasEmAndamento
      ) {
        return;
      }

      void this.atualizarPainelSilencioso();
    }, INTERVALO_POLLING_MS);

    this.destroyRef.onDestroy(() => {
      clearInterval(idPollingMesas);
      // Mata qualquer loop de prefetch ainda em voo ao sair da tela.
      this.geracaoPrefetch++;
    });
  }

  limparErro(): void {
    this.mensagemErroSignal.set(null);
  }

  async atualizarConsulta(consulta: ConsultaMesasPainel): Promise<void> {
    const normalizada = normalizarConsulta(consulta);
    if (consultasIguais(this.consultaSignal(), normalizada)) {
      return;
    }

    const baseNova = baseConsulta(normalizada);
    if (baseNova !== this.baseAtual) {
      // Filtro/ordenação/busca/tamanho mudou: o cache das páginas antigas não vale mais.
      this.invalidarCache();
      this.baseAtual = baseNova;
    }

    this.consultaSignal.set(normalizada);
    this.consultaMesasEmAndamento = true;

    try {
      const emCache = this.cachePaginas.get(chaveConsulta(normalizada));
      if (emCache) {
        // Página já em cache: troca instantânea, sem spinner, e revalida em segundo plano.
        this.aplicarPagina(emCache);
        void this.atualizarMesas().catch(() => {
          // revalidação silenciosa; um erro aqui não deve trocar a tela que já está exibida
        });
      } else {
        this.carregandoMesasSignal.set(true);
        try {
          await this.atualizarMesas();
        } finally {
          this.carregandoMesasSignal.set(false);
        }
      }
    } catch (erro) {
      this.mensagemErroSignal.set(this.extrairMensagemErro(erro));
    } finally {
      this.consultaMesasEmAndamento = false;
      // Já na nova página, começa a puxar a anterior e a próxima para o cache.
      this.agendarPrefetchVizinhos();
    }
  }

  async recarregarGarcons(): Promise<void> {
    try {
      await this.atualizarResumoPainel();
    } catch {
      // uma falha na recarga não deve travar a abertura do modal
    }
  }

  /** Força a atualização dos dados visíveis, usado pelo botão de atualização das telas do gestor. */
  async recarregarPainel(): Promise<void> {
    this.invalidarCache();
    this.carregandoMesasSignal.set(true);
    this.mensagemErroSignal.set(null);
    try {
      await this.atualizarPainelForcado();
    } catch (erro) {
      this.mensagemErroSignal.set(this.extrairMensagemErro(erro));
    } finally {
      this.carregandoMesasSignal.set(false);
    }
  }

  buscarPedidosDetalhados(idMesa: number): Promise<PedidoDetalheGestor[]> {
    return firstValueFrom(
      this.http.get<PedidoDetalheGestor[]>(`${this.baseUrl}/mesas/${idMesa}/pedidos`),
    );
  }

  buscarPedidosDetalhadosPorAtendimento(idAtendimento: number): Promise<PedidoDetalheGestor[]> {
    return firstValueFrom(
      this.http.get<PedidoDetalheGestor[]>(`${this.baseUrl}/atendimentos/${idAtendimento}/pedidos`),
    );
  }

  /** Últimos atendimentos fechados no expediente atual (mais recentes primeiro, já ordenados pelo backend). */
  readonly ultimosPedidos = computed<PedidoRecente[]>(() =>
    this.historicoExpedienteAtual()
      .slice(0, MAXIMO_ULTIMOS_PEDIDOS)
      .map(atendimento => ({
        pedidoId: atendimento.id,
        numeroMesa: atendimento.numeroMesa,
        garcom: atendimento.nomeGarcom ?? '—',
        valor: atendimento.valorFinal,
        tempoAtendimentoMinutos: atendimento.duracaoMinutos,
      })),
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

  /**
   * Estado aberto/fechado do expediente. TODO: ainda não há expediente persistido no backend;
   * mantemos em localStorage para sobreviver ao reload (limitação: é por navegador, não compartilhado entre gestores).
   */
  readonly expedienteFechado = signal(lerExpedienteFechado());

  /**
   * Marco (epoch ms) do início do expediente atual. O histórico do backend é permanente, então
   * "zerar as métricas" ao abrir um novo expediente vira um filtro por data de fechamento.
   */
  private readonly inicioExpedienteSignal = signal<number | null>(lerInicioExpediente());

  /** Histórico do backend restrito ao expediente atual (fechados a partir do início do expediente). */
  private readonly historicoExpedienteAtual = computed(() => {
    const inicio = this.inicioExpedienteSignal();
    const historico = this.historicoAtendimentosSignal();
    if (inicio === null) return historico;
    return historico.filter(item => new Date(item.dataFechamento).getTime() >= inicio);
  });

  readonly resumoExpediente = computed(() => montarResumoExpediente(this.historicoExpedienteAtual()));

  fecharExpediente(): void {
    if (!this.podeFecharExpediente()) return;
    this.expedienteFechado.set(true);
    localStorage.setItem(STORAGE_EXPEDIENTE_FECHADO, 'true');
  }

  /** Inicia um expediente do zero: reabre a operação e marca o início para descartar as métricas anteriores. */
  abrirNovoExpediente(): void {
    const inicio = Date.now();
    this.inicioExpedienteSignal.set(inicio);
    this.expedienteFechado.set(false);
    localStorage.setItem(STORAGE_EXPEDIENTE_INICIO, String(inicio));
    localStorage.setItem(STORAGE_EXPEDIENTE_FECHADO, 'false');
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
      try {
        await firstValueFrom(this.http.patch(`${this.baseUrl}/mesas/${idMesa}/abrir`, {}));
        await firstValueFrom(this.http.patch(`${this.baseUrl}/mesas/${idMesa}/atribuir-garcom`, { garcomId: idGarcom }));
      } finally {
        // Reflete o estado real mesmo se a atribuição falhar (mesa pode ter aberto sem garçom).
        await this.atualizarPainelForcado();
      }
    });
  }

  async fecharConta(idMesa: number): Promise<void> {
    if (this.expedienteFechado()) return;

    await this.executarComFeedback('Fechando conta...', async () => {
      await firstValueFrom(this.http.patch(`${this.baseUrl}/mesas/${idMesa}/fechar`, {}));
      // O fechamento grava o atendimento no histórico do ms-mesas; recarregar o painel puxa esse registro.
      await this.atualizarPainelForcado();
    });
  }

  async reatribuirGarcom(idMesa: number, idGarcom: number): Promise<void> {
    if (this.expedienteFechado()) return;

    await this.executarComFeedback('Atribuindo garçom...', async () => {
      await firstValueFrom(this.http.patch(`${this.baseUrl}/mesas/${idMesa}/atribuir-garcom`, { garcomId: idGarcom }));
      await this.atualizarPainelForcado();
    });
  }

  private async executarComFeedback(descricao: string, acao: () => Promise<void>): Promise<void> {
    this.acaoEmAndamentoSignal.set(true);
    this.descricaoAcaoSignal.set(descricao);
    this.mensagemErroSignal.set(null);
    // A mutação muda o estado das mesas: o cache das outras páginas fica obsoleto e o prefetch é cancelado.
    this.invalidarCache();

    try {
      await acao();
    } catch (erro) {
      this.mensagemErroSignal.set(this.extrairMensagemErro(erro));
    } finally {
      this.acaoEmAndamentoSignal.set(false);
      this.descricaoAcaoSignal.set(null);
      // Terminada a ação, retoma o prefetch das vizinhas em cima do estado já atualizado.
      this.agendarPrefetchVizinhos();
    }
  }

  private extrairMensagemErro(erro: unknown): string {
    if (erro instanceof HttpErrorResponse) {
      const corpo = erro.error as { msgError?: string } | null;
      if (corpo?.msgError) return corpo.msgError;
    }

    return 'Ocorreu um erro. Tente novamente.';
  }

  private async atualizarMesas(): Promise<void> {
    const consulta = this.consultaSignal();
    const seq = ++this.sequenciaMesas;
    const pagina = await this.buscarPagina(consulta);

    // Resposta atrasada de uma consulta anterior não pode sobrescrever a página atual.
    if (seq !== this.sequenciaMesas) return;

    this.aplicarPagina(pagina);
    this.podarCache(consulta);
  }

  /** Busca uma página de mesas e grava no cache; não mexe nos signals de exibição. */
  private async buscarPagina(consulta: ConsultaMesasPainel): Promise<MesaGestorPaginadaApiResponse> {
    const numeroBusca = numeroMesaBusca(consulta.busca);
    if (numeroBusca !== null && (numeroBusca === '0' || numeroBusca.length >= 2)) {
      const pagina = await this.buscarPaginaComBuscaMesa(consulta);
      this.cachePaginas.set(chaveConsulta(consulta), pagina);
      return pagina;
    }

    const pagina = await this.buscarPaginaApi(consulta);
    this.cachePaginas.set(chaveConsulta(consulta), pagina);
    return pagina;
  }

  private async buscarPaginaComBuscaMesa(consulta: ConsultaMesasPainel): Promise<MesaGestorPaginadaApiResponse> {
    const consultaBase = { ...consulta, page: 0, busca: '' };
    const primeiraPagina = await this.buscarPaginaApi(consultaBase);
    const paginas = await Promise.all(
      Array.from({ length: Math.max(0, primeiraPagina.totalPages - 1) }, (_, indice) =>
        this.buscarPaginaApi({ ...consultaBase, page: indice + 1 }),
      ),
    );
    const mesas = [primeiraPagina, ...paginas]
      .flatMap(pagina => pagina.content)
      .filter(mesa => mesaCorrespondeBuscaParcial(mesa.numero, consulta.busca));
    const totalPages = Math.max(1, Math.ceil(mesas.length / consulta.size));
    const page = Math.min(consulta.page, totalPages - 1);

    return {
      content: mesas.slice(page * consulta.size, (page + 1) * consulta.size),
      page,
      size: consulta.size,
      totalElements: mesas.length,
      totalPages,
      first: page === 0,
      last: page === totalPages - 1,
    };
  }

  private async buscarPaginaApi(consulta: ConsultaMesasPainel): Promise<MesaGestorPaginadaApiResponse> {
    const pagina = await firstValueFrom(
      this.http.get<MesaGestorPaginadaApiResponse>(`${this.baseUrl}/mesas/paginadas`, {
        params: montarParamsConsulta(consulta),
      }),
    );
    return pagina;
  }

  private aplicarPagina(pagina: MesaGestorPaginadaApiResponse): void {
    this.mesasSignal.set(pagina.content.map(mapearMesa));
    this.paginacaoSignal.set({
      page: pagina.page,
      size: pagina.size,
      totalElements: pagina.totalElements,
      totalPages: pagina.totalPages,
      first: pagina.first,
      last: pagina.last,
    });
  }

  /** Mantém em cache apenas a página atual e as vizinhas dentro do raio (2 páginas, ou 1 se a página for grande). */
  private podarCache(consulta: ConsultaMesasPainel): void {
    const raio = raioPrefetch(consulta.size);
    const paginas: number[] = [];
    for (let page = consulta.page - raio; page <= consulta.page + raio; page++) {
      paginas.push(page);
    }
    const manter = new Set(paginas.map(page => chaveConsulta({ ...consulta, page })));

    for (const chave of this.cachePaginas.keys()) {
      if (!manter.has(chave)) {
        this.cachePaginas.delete(chave);
      }
    }
  }

  private invalidarCache(): void {
    this.cachePaginas.clear();
    this.geracaoPrefetch++;
  }

  /** Dispara um novo ciclo de prefetch das páginas vizinhas à atual (cancela o anterior). */
  private agendarPrefetchVizinhos(): void {
    const geracao = ++this.geracaoPrefetch;
    void this.prefetchVizinhos(geracao);
  }

  private async prefetchVizinhos(geracao: number): Promise<void> {
    const consulta = this.consultaSignal();
    const numeroBusca = numeroMesaBusca(consulta.busca);
    if (numeroBusca !== null && (numeroBusca === '0' || numeroBusca.length >= 2)) {
      return;
    }

    const totalPaginas = this.paginacaoSignal().totalPages;
    const raio = raioPrefetch(consulta.size);

    // Do mais próximo ao mais distante: [-1, +1, -2, +2] — puxa primeiro o que o usuário provavelmente abrirá.
    const alvos: number[] = [];
    for (let delta = 1; delta <= raio; delta++) {
      alvos.push(consulta.page - delta, consulta.page + delta);
    }
    const alvosValidos = alvos.filter(page => page >= 0 && page < totalPaginas);

    for (const page of alvosValidos) {
      if (geracao !== this.geracaoPrefetch) return;

      const consultaVizinha = { ...consulta, page };
      if (this.cachePaginas.has(chaveConsulta(consultaVizinha))) continue;

      // Pausa enquanto o usuário faz uma ação (abrir mesa, fechar conta, etc.).
      await this.esperarSemAcao(geracao);
      if (geracao !== this.geracaoPrefetch) return;

      try {
        await this.buscarPagina(consultaVizinha);
      } catch {
        // prefetch é best-effort: uma falha não afeta a tela nem interrompe o restante.
      }

      await this.aguardar(PREFETCH_INTERVALO_MS);
    }

    if (geracao === this.geracaoPrefetch) {
      this.podarCache(this.consultaSignal());
    }
  }

  private async esperarSemAcao(geracao: number): Promise<void> {
    while (this.acaoEmAndamentoSignal() && geracao === this.geracaoPrefetch) {
      await this.aguardar(PREFETCH_CHECK_MS);
    }
  }

  private aguardar(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  private async atualizarResumoPainel(): Promise<void> {
    const seq = ++this.sequenciaResumo;
    const resumo = await firstValueFrom(this.http.get<ResumoPainelApiResponse>(`${this.baseUrl}/mesas/resumo`));

    // Resposta atrasada não pode sobrescrever KPIs/carga mais recentes.
    if (seq !== this.sequenciaResumo) return;

    const cargaGarcons = resumo.cargaGarcons
      .map(garcom => ({
        id: garcom.id,
        nome: garcom.nome,
        mesasAtivas: garcom.mesasAtivas,
        cor: PALETA_CORES_GARCOM[garcom.id % PALETA_CORES_GARCOM.length],
      }))
      .sort((a, b) => b.mesasAtivas - a.mesasAtivas || a.nome.localeCompare(b.nome));

    this.cargaGarconsSignal.set(cargaGarcons);
    this.resumoSignal.set({
      mesasLivres: resumo.mesasLivres,
      mesasSemGarcom: resumo.mesasSemGarcom,
      emPreparo: resumo.emPreparo,
      prontos: resumo.prontos,
      problemas: resumo.problemas,
      garconsDisponiveis: cargaGarcons.filter(garcom => nivelCargaGarcom(garcom.mesasAtivas) !== 'ALTA').length,
      ticketMedio: resumo.ticketMedio,
    });
  }

  private async atualizarHistoricoAtendimentos(): Promise<void> {
    const seq = ++this.sequenciaHistorico;
    const historico = await firstValueFrom(
        this.http.get<HistoricoAtendimento[]>(`${this.baseUrl}/atendimentos/historico`),
    );

    // Resposta atrasada não pode sobrescrever um histórico mais recente.
    if (seq !== this.sequenciaHistorico) return;

    this.historicoAtendimentosSignal.set(historico ?? []);
  }

  private async atualizarPainel(): Promise<void> {
    if (this.atualizacaoPainelPromise !== null) {
      return this.atualizacaoPainelPromise;
    }

    return this.iniciarAtualizacaoPainel();
  }

  private async atualizarPainelForcado(): Promise<void> {
    return this.iniciarAtualizacaoPainel();
  }

  private async iniciarAtualizacaoPainel(): Promise<void> {
    const promessa = Promise.all([
      this.atualizarMesas(),
      this.atualizarResumoPainel(),
      this.atualizarHistoricoAtendimentos(),
    ]).then(() => undefined);
    this.atualizacaoPainelPromise = promessa;

    try {
      await promessa;
    } finally {
      if (this.atualizacaoPainelPromise === promessa) {
        this.atualizacaoPainelPromise = null;
      }
    }
  }

  /** Variantes usadas pelo polling: uma falha transitória não deve incomodar o usuário. */
  private async atualizarPainelSilencioso(): Promise<void> {
    try {
      await this.atualizarPainel();
    } catch {
      // ignora falha de polling (o próximo tick tenta de novo)
    }
  }
}

/** Lê o estado fechado/aberto do expediente persistido no navegador (paliativo até o backend expor isso). */
function lerExpedienteFechado(): boolean {
  return localStorage.getItem(STORAGE_EXPEDIENTE_FECHADO) === 'true';
}

/** Lê o marco (epoch ms) de início do expediente atual; `null` quando nunca foi definido. */
function lerInicioExpediente(): number | null {
  const raw = localStorage.getItem(STORAGE_EXPEDIENTE_INICIO);
  if (raw === null) return null;
  const valor = Number.parseInt(raw, 10);
  return Number.isNaN(valor) ? null : valor;
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

function montarParamsConsulta(consulta: ConsultaMesasPainel): HttpParams {
  let params = new HttpParams()
    .set('page', consulta.page)
    .set('size', consulta.size)
    .set('sort', consulta.sort);

  if (consulta.filtroEstado !== null) {
    params = params.set('filtroEstado', consulta.filtroEstado);
  }

  if (consulta.garcomId !== null) {
    params = params.set('garcomId', consulta.garcomId);
  }

  const busca = consulta.busca.trim();
  if (busca !== '') {
    params = params.set('busca', busca);
  }

  return params;
}

function normalizarConsulta(consulta: ConsultaMesasPainel): ConsultaMesasPainel {
  return {
    page: Math.max(0, Math.floor(consulta.page)),
    size: Math.max(1, Math.floor(consulta.size)),
    sort: consulta.sort,
    filtroEstado: consulta.filtroEstado,
    garcomId: consulta.garcomId,
    busca: consulta.busca.trim(),
  };
}

/** Quantas páginas vizinhas manter em cache/prefetch: 1 para páginas grandes (>= 30 mesas), 2 caso contrário. */
function raioPrefetch(size: number): number {
  return size >= LIMIAR_PAGINA_GRANDE ? 1 : 2;
}

/** Chave de cache de uma página específica (inclui a página). */
function chaveConsulta(consulta: ConsultaMesasPainel): string {
  return [
    consulta.page,
    consulta.size,
    consulta.sort,
    consulta.filtroEstado ?? '',
    consulta.garcomId ?? '',
    consulta.busca,
  ].join('|');
}

/** Chave da "base" da consulta (tudo menos a página): muda quando filtro/ordenação/busca/tamanho muda. */
function baseConsulta(consulta: ConsultaMesasPainel): string {
  return [
    consulta.size,
    consulta.sort,
    consulta.filtroEstado ?? '',
    consulta.garcomId ?? '',
    consulta.busca,
  ].join('|');
}

function consultasIguais(a: ConsultaMesasPainel, b: ConsultaMesasPainel): boolean {
  return (
    a.page === b.page &&
    a.size === b.size &&
    a.sort === b.sort &&
    a.filtroEstado === b.filtroEstado &&
    a.garcomId === b.garcomId &&
    a.busca === b.busca
  );
}
