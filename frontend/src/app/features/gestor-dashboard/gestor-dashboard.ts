import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { HistoricoAtendimento, MesaPainel } from '../../core/models/painel.models';
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

  protected readonly usuario = toSignal(this.authService.usuario$, {
    initialValue: this.authService.getCurrentUser(),
  });
  protected readonly alertasAnimando = signal(false);
  protected readonly atendimentoSelecionado = signal<HistoricoAtendimento | null>(null);
  protected readonly historicoCompletoAberto = signal(false);
  protected readonly buscaHistorico = signal('');
  protected readonly garcomHistorico = signal<number | null>(null);
  protected readonly periodoHistorico = signal<'TODOS' | 'HOJE' | '7_DIAS' | '30_DIAS'>('TODOS');
  protected readonly paginaHistorico = signal(1);
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
  protected readonly mesasSemGarcom = computed(() => this.mesas().filter(mesa => mesa.status === 'OCUPADA' && mesa.garcomId === null).length);
  protected readonly historicoOrdenado = computed(() => [...this.historicoAtendimentos()].sort(
    (a, b) => new Date(b.dataFechamento).getTime() - new Date(a.dataFechamento).getTime(),
  ));
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
    const agora = new Date();
    const inicioHoje = new Date(agora.getFullYear(), agora.getMonth(), agora.getDate()).getTime();
    const limitePeriodo = periodo === '7_DIAS'
      ? agora.getTime() - 7 * 86_400_000
      : periodo === '30_DIAS' ? agora.getTime() - 30 * 86_400_000 : null;

    return this.historicoOrdenado().filter(atendimento => {
      const fechamento = new Date(atendimento.dataFechamento).getTime();
      const correspondePeriodo = periodo === 'HOJE'
        ? fechamento >= inicioHoje
        : limitePeriodo === null || fechamento >= limitePeriodo;
      const correspondeGarcom = garcomId === null || atendimento.idGarcom === garcomId;
      const texto = this.normalizarTexto(
        `${atendimento.numeroMesa} ${atendimento.nomeGarcom ?? ''} ${atendimento.codigoSessao}`,
      );
      return correspondePeriodo && correspondeGarcom && (busca === '' || texto.includes(busca));
    });
  });
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
    this.paginaHistorico.set(1);
  }

  protected paginaAnteriorHistorico(): void {
    this.paginaHistorico.update(pagina => Math.max(1, pagina - 1));
  }

  protected proximaPaginaHistorico(): void {
    this.paginaHistorico.update(pagina => Math.min(this.totalPaginasHistorico(), pagina + 1));
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
