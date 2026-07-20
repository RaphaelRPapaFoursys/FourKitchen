import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { HistoricoAtendimento, MesaPainel, PedidoDetalheGestor } from '../../core/models/painel.models';
import { AuthService } from '../../core/services/auth';
import { PainelService } from '../../core/services/painel';
import { Topbar } from '../../shared/components/header/header';
import { Icon } from '../../shared/components/icon/icon';
import { Sidebar } from '../../shared/components/sidebar/sidebar';
import { WaiterLoadItem } from '../../shared/components/waiter-load-item/waiter-load-item';

@Component({
  selector: 'app-gestor-dashboard',
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink, Sidebar, Topbar, Icon, WaiterLoadItem],
  templateUrl: './gestor-dashboard.html',
  styleUrl: './gestor-dashboard.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [PainelService],
})
export class GestorDashboard {
  private static readonly ITENS_HISTORICO_POR_PAGINA = 8;
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly painel = inject(PainelService);
  private alertaAnimacaoTimer: ReturnType<typeof setTimeout> | null = null;
  private alertaReinicioTimer: ReturnType<typeof setTimeout> | null = null;
  private ultimoResumoAlertas: { problemas: number; semGarcom: number; prontos: number } | null = null;
  private sequenciaResumoPedidos = 0;

  protected readonly usuario = toSignal(this.authService.usuario$, {
    initialValue: this.authService.getCurrentUser(),
  });
  protected readonly alertasAnimando = signal(false);
  protected readonly atendimentoSelecionado = signal<HistoricoAtendimento | null>(null);
  protected readonly atendimentoResumoPedidos = signal<HistoricoAtendimento | null>(null);
  protected readonly pedidosResumo = signal<PedidoDetalheGestor[]>([]);
  protected readonly carregandoResumoPedidos = signal(false);
  protected readonly erroResumoPedidos = signal<string | null>(null);
  protected readonly historicoCompletoAberto = signal(false);
  protected readonly filtrosHistoricoAbertos = signal(false);
  protected readonly buscaHistorico = signal('');
  protected readonly garcomHistorico = signal<number | null>(null);
  protected readonly periodoHistorico = signal<'TODOS' | 'HOJE' | '7_DIAS' | '30_DIAS'>('TODOS');
  protected readonly dataInicialHistorico = signal('');
  protected readonly dataFinalHistorico = signal('');
  protected readonly paginaHistorico = signal(1);
  protected readonly etapasPedido = [
    { status: 'ENVIADO_COZINHA', label: 'Recebido' },
    { status: 'EM_PREPARO', label: 'Preparo' },
    { status: 'PRONTO', label: 'Pronto' },
    { status: 'ENTREGUE', label: 'Entregue' },
  ] as const;
  protected readonly resumo = this.painel.resumo;
  protected readonly mesas = this.painel.mesas;
  protected readonly cargaGarcons = this.painel.cargaGarcons;
  protected readonly historicoAtendimentos = this.painel.historicoAtendimentos;
  protected readonly totalMesas = this.painel.totalElementos;
  protected readonly carregando = this.painel.carregando;
  protected readonly carregandoMesas = this.painel.carregandoMesas;

  protected readonly mesasOcupadas = computed(() => Math.max(0, this.totalMesas() - this.resumo().mesasLivres));
  protected readonly taxaOcupacao = computed(() => {
    const total = this.totalMesas();
    return total === 0 ? 0 : Math.round((this.mesasOcupadas() / total) * 100);
  });
  protected readonly arcoOcupacao = computed(() => `${this.taxaOcupacao() * 3.52} 352`);
  protected readonly maiorStatus = computed(() => Math.max(
    this.mesasOcupadas(),
    this.resumo().emPreparo,
    this.resumo().prontos,
    this.resumo().problemas,
    1,
  ));
  protected readonly mesasPreview = computed(() => this.mesas().slice(0, 5));
  protected readonly mesasSemGarcom = computed(() => this.resumo().mesasSemGarcom);
  protected readonly historicoOrdenado = computed(() => [...this.historicoAtendimentos()].sort((a, b) => {
    const diferencaData = this.timestampHistorico(b.dataFechamento) - this.timestampHistorico(a.dataFechamento);
    return diferencaData || b.id - a.id;
  }));
  protected readonly historicoRecente = computed(() => this.historicoOrdenado().slice(0, 5));
  protected readonly garconsDoHistorico = computed(() => {
    const garcons = new Map<number, string>();
    for (const atendimento of this.historicoOrdenado()) {
      if (atendimento.idGarcom !== null) {
        garcons.set(atendimento.idGarcom, atendimento.nomeGarcom ?? `Garçom ${atendimento.idGarcom}`);
      }
    }
    return [...garcons.entries()]
      .map(([id, nome]) => ({ id, nome }))
      .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
  });
  protected readonly historicoFiltrado = computed(() => {
    const busca = this.normalizarTexto(this.buscaHistorico());
    const garcomId = this.garcomHistorico();
    const periodo = this.periodoHistorico();
    const dataInicial = this.dataInicialHistorico();
    const dataFinal = this.dataFinalHistorico();
    const agora = new Date();
    const inicioHoje = new Date(agora.getFullYear(), agora.getMonth(), agora.getDate()).getTime();
    const limitePeriodo = periodo === '7_DIAS'
      ? agora.getTime() - 7 * 86_400_000
      : periodo === '30_DIAS' ? agora.getTime() - 30 * 86_400_000 : null;

    return this.historicoOrdenado().filter(atendimento => {
      const fechamento = new Date(atendimento.dataFechamento).getTime();
      const inicioPersonalizado = dataInicial ? new Date(`${dataInicial}T00:00:00`).getTime() : null;
      const fimPersonalizado = dataFinal ? new Date(`${dataFinal}T23:59:59.999`).getTime() : null;
      const correspondePeriodoRapido = periodo === 'HOJE'
        ? fechamento >= inicioHoje
        : limitePeriodo === null || fechamento >= limitePeriodo;
      const correspondePeriodo = correspondePeriodoRapido
        && (inicioPersonalizado === null || fechamento >= inicioPersonalizado)
        && (fimPersonalizado === null || fechamento <= fimPersonalizado);
      const correspondeGarcom = garcomId === null || atendimento.idGarcom === garcomId;
      return correspondePeriodo && correspondeGarcom && this.correspondeBuscaHistorico(atendimento, busca);
    });
  });
  protected readonly quantidadeFiltrosHistorico = computed(() =>
    (this.garcomHistorico() === null ? 0 : 1)
    + (this.periodoHistorico() === 'TODOS' ? 0 : 1)
    + (this.dataInicialHistorico() || this.dataFinalHistorico() ? 1 : 0),
  );
  protected readonly totalPaginasHistorico = computed(() => Math.max(
    1,
    Math.ceil(this.historicoFiltrado().length / GestorDashboard.ITENS_HISTORICO_POR_PAGINA),
  ));
  protected readonly historicoPagina = computed(() => {
    const pagina = Math.min(this.paginaHistorico(), this.totalPaginasHistorico());
    const inicio = (pagina - 1) * GestorDashboard.ITENS_HISTORICO_POR_PAGINA;
    return this.historicoFiltrado().slice(inicio, inicio + GestorDashboard.ITENS_HISTORICO_POR_PAGINA);
  });

  constructor() {
    effect(() => {
      const atual = {
        problemas: this.resumo().problemas,
        semGarcom: this.mesasSemGarcom(),
        prontos: this.resumo().prontos,
      };
      const anterior = this.ultimoResumoAlertas;
      this.ultimoResumoAlertas = atual;

      if (anterior && (
        atual.problemas > anterior.problemas
        || atual.semGarcom > anterior.semGarcom
        || atual.prontos > anterior.prontos
      )) {
        this.dispararAnimacaoAlerta();
      }
    });

    this.destroyRef.onDestroy(() => {
      if (this.alertaAnimacaoTimer) clearTimeout(this.alertaAnimacaoTimer);
      if (this.alertaReinicioTimer) clearTimeout(this.alertaReinicioTimer);
    });
  }

  protected percentualStatus(valor: number): number {
    return Math.max(valor > 0 ? 8 : 0, (valor / this.maiorStatus()) * 100);
  }

  protected valorMesa(mesa: MesaPainel): number {
    return mesa.pedidos.reduce((total, pedido) => total + pedido.valor, 0);
  }

  protected statusMesa(mesa: MesaPainel): string {
    if (mesa.status === 'LIVRE') return 'Disponível';
    if (mesa.garcomId === null) return 'Sem garçom';
    if (mesa.statusPedido === 'EM_PREPARO') return 'Em preparo';
    if (mesa.statusPedido === 'PRONTO_ENTREGA') return 'Pedido pronto';
    if (mesa.statusPedido === 'CONTA_ABERTA') return 'Conta aberta';
    return 'Em atendimento';
  }

  protected statusClasse(mesa: MesaPainel): string {
    if (mesa.status === 'LIVRE') return 'livre';
    if (mesa.garcomId === null) return 'critico';
    if (mesa.statusPedido === 'PRONTO_ENTREGA') return 'pronto';
    if (mesa.statusPedido === 'EM_PREPARO') return 'preparo';
    return 'ocupada';
  }

  protected tempoLabel(minutos: number): string {
    if (minutos < 60) return `${minutos} min`;
    const horas = Math.floor(minutos / 60);
    return horas < 24 ? `${horas}h` : `${Math.floor(horas / 24)}d`;
  }

  protected tempoMesaAberta(mesa: MesaPainel): string {
    if (mesa.status === 'LIVRE' || !mesa.abertaEm) return '—';
    const inicio = new Date(mesa.abertaEm).getTime();
    if (Number.isNaN(inicio)) return '—';
    const minutos = Math.max(0, Math.floor((Date.now() - inicio) / 60_000));
    return this.tempoLabel(minutos);
  }

  protected iniciais(nome: string | null | undefined): string {
    return nome?.trim().charAt(0).toUpperCase() || '?';
  }

  protected nivelCarga(mesasAtivas: number): 'BAIXA' | 'MEDIA' | 'ALTA' {
    if (mesasAtivas >= 5) return 'ALTA';
    if (mesasAtivas >= 3) return 'MEDIA';
    return 'BAIXA';
  }

  protected larguraCarga(mesasAtivas: number): number {
    const maximo = Math.max(...this.cargaGarcons().map(garcom => garcom.mesasAtivas), 1);
    return (mesasAtivas / maximo) * 100;
  }

  protected atualizarDados(): void {
    void this.painel.recarregarPainel();
  }

  protected abrirDetalhesAtendimento(atendimento: HistoricoAtendimento): void {
    this.atendimentoSelecionado.set(atendimento);
  }

  protected abrirHistoricoCompleto(): void {
    this.paginaHistorico.set(1);
    this.historicoCompletoAberto.set(true);
  }

  protected atualizarBuscaHistorico(valor: string): void {
    this.buscaHistorico.set(valor);
    this.paginaHistorico.set(1);
  }

  protected atualizarGarcomHistorico(valor: number | null): void {
    this.garcomHistorico.set(valor === null ? null : Number(valor));
    this.paginaHistorico.set(1);
  }

  protected atualizarPeriodoHistorico(valor: 'TODOS' | 'HOJE' | '7_DIAS' | '30_DIAS'): void {
    this.periodoHistorico.set(valor);
    if (valor !== 'TODOS') {
      this.dataInicialHistorico.set('');
      this.dataFinalHistorico.set('');
    }
    this.paginaHistorico.set(1);
  }

  protected atualizarDataInicialHistorico(valor: string): void {
    this.dataInicialHistorico.set(valor);
    if (valor) this.periodoHistorico.set('TODOS');
    this.paginaHistorico.set(1);
  }

  protected atualizarDataFinalHistorico(valor: string): void {
    this.dataFinalHistorico.set(valor);
    if (valor) this.periodoHistorico.set('TODOS');
    this.paginaHistorico.set(1);
  }

  protected limparFiltrosHistorico(): void {
    this.garcomHistorico.set(null);
    this.periodoHistorico.set('TODOS');
    this.dataInicialHistorico.set('');
    this.dataFinalHistorico.set('');
    this.paginaHistorico.set(1);
  }

  protected paginaAnteriorHistorico(): void {
    this.paginaHistorico.update(pagina => Math.max(1, pagina - 1));
  }

  protected proximaPaginaHistorico(): void {
    this.paginaHistorico.update(pagina => Math.min(this.totalPaginasHistorico(), pagina + 1));
  }

  protected abrirResumoPedidos(atendimento: HistoricoAtendimento): void {
    this.atendimentoResumoPedidos.set(atendimento);
    void this.carregarResumoPedidos(atendimento);
  }

  protected fecharResumoPedidos(): void {
    this.sequenciaResumoPedidos++;
    this.atendimentoResumoPedidos.set(null);
    this.pedidosResumo.set([]);
    this.erroResumoPedidos.set(null);
  }

  protected recarregarResumoPedidos(): void {
    const atendimento = this.atendimentoResumoPedidos();
    if (atendimento) void this.carregarResumoPedidos(atendimento);
  }

  protected totalItensResumoPedidos(): number {
    return this.pedidosResumo().flatMap(pedido => pedido.itens)
      .filter(item => item.status !== 'REMOVIDO')
      .reduce((total, item) => total + item.quantidade, 0);
  }

  protected valorTotalResumoPedidos(): number {
    return this.pedidosResumo().reduce((total, pedido) => total + this.valorPedidoDetalhado(pedido), 0);
  }

  protected valorPedidoDetalhado(pedido: PedidoDetalheGestor): number {
    if (pedido.status === 'CANCELADO') return 0;
    return pedido.itens
      .filter(item => item.status !== 'REMOVIDO')
      .reduce((total, item) => total + item.precoUnitario * item.quantidade, 0);
  }

  protected etapaConcluida(statusAtual: string, statusEtapa: string): boolean {
    const ordem: Record<string, number> = {
      ENVIADO_COZINHA: 0,
      EM_PREPARO: 1,
      AGUARDANDO_DECISAO: 1,
      PROBLEMA_COZINHA: 1,
      PRONTO: 2,
      ENTREGUE: 3,
      FINALIZADO: 3,
    };
    return (ordem[statusAtual] ?? -1) >= (ordem[statusEtapa] ?? 0);
  }

  protected statusPedidoClasse(status: string): string {
    return status.toLocaleLowerCase('pt-BR').replaceAll('_', '-');
  }

  protected statusPedidoDetalhe(status: string): string {
    switch (status) {
      case 'ENVIADO_COZINHA': return 'Enviado à cozinha';
      case 'EM_PREPARO': return 'Em preparo';
      case 'AGUARDANDO_DECISAO': return 'Aguardando decisão';
      case 'PROBLEMA_COZINHA': return 'Problema na cozinha';
      case 'PRONTO': return 'Pronto';
      case 'ENTREGUE': return 'Entregue';
      case 'FINALIZADO': return 'Finalizado';
      case 'CANCELADO': return 'Cancelado';
      default: return status.replaceAll('_', ' ').toLocaleLowerCase('pt-BR');
    }
  }

  private async carregarResumoPedidos(atendimento: HistoricoAtendimento): Promise<void> {
    const sequencia = ++this.sequenciaResumoPedidos;
    this.carregandoResumoPedidos.set(true);
    this.erroResumoPedidos.set(null);
    this.pedidosResumo.set([]);

    try {
      const pedidos = await this.painel.buscarPedidosDetalhadosPorAtendimento(atendimento.idAtendimento);
      if (sequencia !== this.sequenciaResumoPedidos) return;
      this.pedidosResumo.set([...pedidos].sort((a, b) => {
        const diferencaData = this.timestampHistorico(b.dataCriacao) - this.timestampHistorico(a.dataCriacao);
        return diferencaData || b.id - a.id;
      }));
    } catch {
      if (sequencia !== this.sequenciaResumoPedidos) return;
      this.erroResumoPedidos.set('Não foi possível carregar os pedidos deste atendimento.');
    } finally {
      if (sequencia === this.sequenciaResumoPedidos) this.carregandoResumoPedidos.set(false);
    }
  }

  private correspondeBuscaHistorico(atendimento: HistoricoAtendimento, buscaNormalizada: string): boolean {
    if (buscaNormalizada === '') return true;

    const buscaMesa = buscaNormalizada.match(/^(?:mesa\s*#?|#)?0*(\d{1,3})$/);
    if (buscaMesa) {
      return atendimento.numeroMesa === Number(buscaMesa[1]);
    }

    if (/^\d+$/.test(buscaNormalizada)) {
      return String(atendimento.codigoSessao) === buscaNormalizada
        || String(atendimento.idAtendimento) === buscaNormalizada;
    }

    return this.normalizarTexto(atendimento.nomeGarcom ?? '').includes(buscaNormalizada);
  }

  private timestampHistorico(valor: string): number {
    const timestamp = new Date(valor).getTime();
    return Number.isNaN(timestamp) ? 0 : timestamp;
  }

  private normalizarTexto(valor: string): string {
    return valor.normalize('NFD').replace(/[\u0300-\u036f]/g, '').trim().toLocaleLowerCase('pt-BR');
  }

  private dispararAnimacaoAlerta(): void {
    this.alertasAnimando.set(false);
    if (this.alertaReinicioTimer) clearTimeout(this.alertaReinicioTimer);
    if (this.alertaAnimacaoTimer) clearTimeout(this.alertaAnimacaoTimer);

    this.alertaReinicioTimer = setTimeout(() => {
      this.alertasAnimando.set(true);
      this.alertaAnimacaoTimer = setTimeout(() => this.alertasAnimando.set(false), 2200);
    }, 0);
  }
}
