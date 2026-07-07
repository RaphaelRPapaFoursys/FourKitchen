import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { NivelCarga, resolverCriticidadeMesa } from '../../core/constants/urgencia.constants';
import { AcaoMesaPainel, MesaPainel, StatusMesaPainel } from '../../core/models/painel.models';
import { AuthService } from '../../core/services/auth';
import { PainelService } from '../../core/services/painel';
import { Avatar } from '../../shared/components/avatar/avatar';
import { Badge } from '../../shared/components/badge/badge';
import { Icon } from '../../shared/components/icon/icon';
import { KpiCard } from '../../shared/components/kpi-card/kpi-card';
import { ProgressBar } from '../../shared/components/progress-bar/progress-bar';
import { WaiterLoadItem } from '../../shared/components/waiter-load-item/waiter-load-item';

type Ordenacao = 'CRITICO' | 'NUMERO' | 'MAIOR_VALOR' | 'MENOR_VALOR';
type Criticidade = ReturnType<typeof resolverCriticidadeMesa>;
type FiltroEstado =
  | 'PROBLEMAS'
  | 'PRONTOS'
  | 'EM_PREPARO'
  | 'LIVRE'
  | 'SEM_GARCOM'
  | 'CONTA_ABERTA'
  | 'ATRASADAS'
  | 'AGUARDANDO_PEDIDO'
  | null;
type ModoSelecaoGarcom = 'ABRIR' | 'REATRIBUIR';
type AcaoCritica = Extract<AcaoMesaPainel, 'FECHAR_CONTA' | 'MARCAR_ENTREGUE'>;

interface SelecaoGarcomEstado {
  mesaId: number;
  numeroMesa: number;
  modo: ModoSelecaoGarcom;
}

interface ConfirmacaoAcaoEstado {
  mesaId: number;
  numeroMesa: number;
  tipo: AcaoCritica;
  titulo: string;
  mensagem: string;
  confirmarLabel: string;
}

const NOMES_ETAPAS: Record<number, string> = {
  1: 'Pedido enviado à cozinha',
  2: 'Em preparo',
  3: 'Finalização',
  4: 'Pronto para entrega',
};

const CRITICIDADE_RANK: Record<StatusMesaPainel, (mesa: MesaPainel) => number> = {
  OCUPADA: mesa => {
    if (mesa.statusPedido === 'EM_PREPARO') return 1;
    if (mesa.statusPedido === 'PRONTO_ENTREGA') return 2;
    return 3;
  },
  LIVRE: () => 4,
};

@Component({
  selector: 'app-gestor',
  imports: [FormsModule, CurrencyPipe, Avatar, Badge, Icon, KpiCard, ProgressBar, WaiterLoadItem],
  templateUrl: './gestor.html',
  styleUrl: './gestor.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [PainelService],
})
export class Gestor {
  private readonly authService = inject(AuthService);
  private readonly painelService = inject(PainelService);
  private readonly router = inject(Router);

  protected readonly usuario = toSignal(this.authService.usuario$, {
    initialValue: this.authService.getCurrentUser(),
  });
  protected readonly resumo = this.painelService.resumo;
  protected readonly cargaGarcons = this.painelService.cargaGarcons;
  protected readonly ultimosPedidos = this.painelService.ultimosPedidos;
  protected readonly totalAtendimentosAtivos = this.painelService.totalAtendimentosAtivos;
  protected readonly totalArrecadadoAtendimentosAtivos = this.painelService.totalArrecadadoAtendimentosAtivos;
  protected readonly expedienteFechado = this.painelService.expedienteFechado;
  protected readonly podeFecharExpediente = this.painelService.podeFecharExpediente;
  protected readonly pedidosPendentesEntrega = this.painelService.pedidosPendentesEntrega;
  protected readonly resumoExpediente = this.painelService.resumoExpediente;
  protected readonly carregando = this.painelService.carregando;
  protected readonly acaoEmAndamento = this.painelService.acaoEmAndamento;
  protected readonly descricaoAcao = this.painelService.descricaoAcao;
  protected readonly mensagemErro = this.painelService.mensagemErro;

  protected readonly buscaTermo = signal('');
  protected readonly filtroGarcom = signal<number | null>(null);
  protected readonly ordenacao = signal<Ordenacao>('CRITICO');
  protected readonly filtroEstado = signal<FiltroEstado>(null);
  protected readonly paginaAtual = signal(1);
  protected readonly itensPorPagina = signal(10);
  protected readonly limiarCargaLaranja = signal(5);
  protected readonly limiarCargaVermelho = signal(10);
  protected readonly configCargaAberta = signal(false);
  protected readonly fechamentoExpandido = signal(false);
  protected readonly selecaoGarcom = signal<SelecaoGarcomEstado | null>(null);
  protected readonly mesaDestacada = signal<number | null>(null);
  protected readonly confirmandoFechamento = signal(false);
  protected readonly confirmacaoAcao = signal<ConfirmacaoAcaoEstado | null>(null);

  protected readonly mesasFiltradas = computed(() => {
    const termo = this.buscaTermo().trim().toLowerCase();
    const garcom = this.filtroGarcom();
    const estado = this.filtroEstado();
    const ordenacao = this.ordenacao();

    const filtradas = this.painelService.mesas().filter(item => {
      if (termo) {
        const correspondeNumero = item.numero.toString().includes(termo);
        const correspondeGarcom = item.garcom?.toLowerCase().includes(termo) ?? false;
        const correspondePedido = this.statusPedidoLabel(item).toLowerCase().includes(termo);
        if (!correspondeNumero && !correspondeGarcom && !correspondePedido) return false;
      }
      if (garcom !== null && item.garcomId !== garcom) return false;
      if (estado === 'PROBLEMAS' && this.criticidadeCard(item) !== 'critico') return false;
      if (estado === 'PRONTOS' && item.statusPedido !== 'PRONTO_ENTREGA') return false;
      if (estado === 'EM_PREPARO' && item.statusPedido !== 'EM_PREPARO') return false;
      if (estado === 'LIVRE' && item.status !== 'LIVRE') return false;
      if (estado === 'SEM_GARCOM' && (item.status !== 'OCUPADA' || item.garcomId !== null)) return false;
      if (estado === 'CONTA_ABERTA' && item.statusPedido !== 'CONTA_ABERTA') return false;
      if (estado === 'ATRASADAS' && this.criticidadeCard(item) !== 'critico') return false;
      if (
        estado === 'AGUARDANDO_PEDIDO' &&
        (item.status !== 'OCUPADA' || item.garcomId === null || item.pedidos.length > 0)
      ) {
        return false;
      }
      return true;
    });

    return [...filtradas].sort((a, b) => {
      if (ordenacao === 'NUMERO') return a.numero - b.numero;
      if (ordenacao === 'MAIOR_VALOR') {
        return (this.valorContaMesa(b) ?? -Infinity) - (this.valorContaMesa(a) ?? -Infinity);
      }
      if (ordenacao === 'MENOR_VALOR') {
        return (this.valorContaMesa(a) ?? Infinity) - (this.valorContaMesa(b) ?? Infinity);
      }
      return CRITICIDADE_RANK[a.status](a) - CRITICIDADE_RANK[b.status](b);
    });
  });

  protected readonly totalPaginas = computed(() =>
    Math.max(1, Math.ceil(this.mesasFiltradas().length / this.itensPorPagina())),
  );

  /** Página válida: nunca abaixo de 1 nem acima do total (protege contra lista encolher). */
  protected readonly paginaEfetiva = computed(() =>
    Math.min(Math.max(1, this.paginaAtual()), this.totalPaginas()),
  );

  /** Fatia da lista filtrada/ordenada correspondente à página atual. */
  protected readonly mesasPagina = computed(() => {
    const inicio = (this.paginaEfetiva() - 1) * this.itensPorPagina();
    return this.mesasFiltradas().slice(inicio, inicio + this.itensPorPagina());
  });

  protected readonly temPaginaAnterior = computed(() => this.paginaEfetiva() > 1);
  protected readonly temProximaPagina = computed(() => this.paginaEfetiva() < this.totalPaginas());

  constructor() {
    // Qualquer mudança de busca/filtro/ordenação/tamanho volta para a primeira página.
    effect(() => {
      this.buscaTermo();
      this.filtroGarcom();
      this.filtroEstado();
      this.ordenacao();
      this.itensPorPagina();
      this.paginaAtual.set(1);
    });
  }

  protected readonly temFiltrosAtivos = computed(
    () =>
      this.buscaTermo().trim() !== '' ||
      this.filtroGarcom() !== null ||
      this.filtroEstado() !== null,
  );

  protected readonly mesaSelecaoGarcom = computed(() => {
    const selecao = this.selecaoGarcom();
    return selecao === null ? null : (this.painelService.mesas().find(mesa => mesa.id === selecao.mesaId) ?? null);
  });

  protected criticidadeCard(mesa: MesaPainel): Criticidade {
    return resolverCriticidadeMesa(mesa);
  }

  protected urgenciaBadgeLabel(mesa: MesaPainel): string | null {
    const criticidade = this.criticidadeCard(mesa);
    if (criticidade === 'critico') return 'Atrasada';
    if (criticidade === 'atencao') return 'Atenção';
    return null;
  }

  protected temAcaoSecundaria(mesa: MesaPainel): boolean {
    return (
      mesa.status === 'OCUPADA' &&
      this.acaoPrimaria(mesa).tipo !== 'VER_PEDIDO' &&
      (this.criticidadeCard(mesa) === 'critico' || this.criticidadeCard(mesa) === 'atencao')
    );
  }

  protected acaoPrimaria(mesa: MesaPainel): { tipo: AcaoMesaPainel; label: string } {
    return this.painelService.acaoPrimaria(mesa);
  }

  protected acaoPrimariaIndisponivel(mesa: MesaPainel): boolean {
    return this.expedienteFechado() || this.acaoEmAndamento() || this.acaoPrimaria(mesa).tipo === 'VER_PEDIDO';
  }

  protected executarAcao(mesa: MesaPainel): void {
    if (this.expedienteFechado() || this.acaoEmAndamento()) return;

    switch (this.acaoPrimaria(mesa).tipo) {
      case 'ABRIR_MESA':
        void this.painelService.recarregarGarcons();
        this.selecaoGarcom.set({ mesaId: mesa.id, numeroMesa: mesa.numero, modo: 'ABRIR' });
        break;
      case 'REATRIBUIR_GARCOM':
        this.abrirReatribuicao(mesa);
        break;
      case 'FECHAR_CONTA':
        this.abrirConfirmacaoAcao(mesa, 'FECHAR_CONTA');
        break;
      case 'MARCAR_ENTREGUE':
        this.abrirConfirmacaoAcao(mesa, 'MARCAR_ENTREGUE');
        break;
      case 'VER_PEDIDO':
        this.verPedido();
        break;
    }
  }

  protected fecharErro(): void {
    this.painelService.limparErro();
  }

  protected verPedido(): void {
    // TODO: sem tela de detalhe do pedido ainda (Fase 2 / integração com backend).
  }

  protected abrirConfirmacaoAcao(mesa: MesaPainel, tipo: AcaoCritica): void {
    const numeroMesa = mesa.numero.toString().padStart(2, '0');

    if (tipo === 'FECHAR_CONTA') {
      this.confirmacaoAcao.set({
        mesaId: mesa.id,
        numeroMesa: mesa.numero,
        tipo,
        titulo: `Fechar conta da Mesa ${numeroMesa}`,
        mensagem: 'Confirme o fechamento da conta. O atendimento será registrado no resumo do expediente e a mesa será liberada.',
        confirmarLabel: 'Fechar conta',
      });
      return;
    }

    this.confirmacaoAcao.set({
      mesaId: mesa.id,
      numeroMesa: mesa.numero,
      tipo,
      titulo: `Marcar entrega da Mesa ${numeroMesa}`,
      mensagem: 'Confirme que o pedido pronto foi entregue ao cliente. Essa ação avança o pedido para entregue.',
      confirmarLabel: 'Marcar entregue',
    });
  }

  protected cancelarConfirmacaoAcao(): void {
    this.confirmacaoAcao.set(null);
  }

  protected confirmarAcaoCritica(): void {
    const confirmacao = this.confirmacaoAcao();
    if (!confirmacao || this.expedienteFechado() || this.acaoEmAndamento()) return;

    this.confirmacaoAcao.set(null);

    if (confirmacao.tipo === 'FECHAR_CONTA') {
      void this.painelService.fecharConta(confirmacao.mesaId);
      return;
    }

    void this.painelService.marcarEntregue(confirmacao.mesaId);
  }

  protected destacarMesa(numero: number): void {
    this.mesaDestacada.set(numero);
    document.getElementById('mesa-card-' + numero)?.scrollIntoView({ behavior: 'smooth', block: 'center' });

    setTimeout(() => {
      if (this.mesaDestacada() === numero) {
        this.mesaDestacada.set(null);
      }
    }, 1600);
  }

  protected abrirReatribuicao(mesa: MesaPainel): void {
    void this.painelService.recarregarGarcons();
    this.selecaoGarcom.set({ mesaId: mesa.id, numeroMesa: mesa.numero, modo: 'REATRIBUIR' });
  }

  protected fecharSelecaoGarcom(): void {
    this.selecaoGarcom.set(null);
  }

  protected garcomJaAtribuido(id: number): boolean {
    const selecao = this.selecaoGarcom();
    if (!selecao || selecao.modo !== 'REATRIBUIR') return false;
    return this.mesaSelecaoGarcom()?.garcomId === id;
  }

  protected confirmarSelecaoGarcom(id: number): void {
    const selecao = this.selecaoGarcom();
    if (!selecao) return;

    const garcom = this.cargaGarcons().find(item => item.id === id);
    if (!garcom) return;

    if (selecao.modo === 'ABRIR') {
      void this.painelService.abrirMesa(selecao.mesaId, garcom.id);
    } else {
      void this.painelService.reatribuirGarcom(selecao.mesaId, garcom.id);
    }

    this.selecaoGarcom.set(null);
  }

  protected alternarFiltroEstado(estado: Exclude<FiltroEstado, null>): void {
    this.filtroEstado.update(atual => (atual === estado ? null : estado));
  }

  protected paginaAnterior(): void {
    this.paginaAtual.set(Math.max(1, this.paginaEfetiva() - 1));
  }

  protected proximaPagina(): void {
    this.paginaAtual.set(Math.min(this.totalPaginas(), this.paginaEfetiva() + 1));
  }

  protected aoDigitarItensPorPagina(raw: string): void {
    if (raw.trim() === '') return; // vazio é permitido enquanto digita; normaliza só no blur
    const numero = Number.parseInt(raw, 10);
    if (Number.isNaN(numero)) return;
    this.itensPorPagina.set(this.limitarItens(numero));
  }

  protected aoSairItensPorPagina(raw: string): void {
    const numero = Number.parseInt(raw, 10);
    this.itensPorPagina.set(Number.isNaN(numero) ? 1 : this.limitarItens(numero));
  }

  private limitarItens(valor: number): number {
    const total = this.painelService.mesas().length;
    const minimo = Math.max(1, Math.floor(valor));
    return total > 0 ? Math.min(minimo, total) : minimo;
  }

  protected limparFiltros(): void {
    this.buscaTermo.set('');
    this.filtroGarcom.set(null);
    this.filtroEstado.set(null);
  }

  protected alternarFechamentoExpediente(): void {
    this.fechamentoExpandido.update(valor => !valor);
  }

  protected abrirConfirmacaoFechamento(): void {
    if (!this.podeFecharExpediente()) return;
    this.confirmandoFechamento.set(true);
  }

  protected cancelarFechamento(): void {
    this.confirmandoFechamento.set(false);
  }

  protected confirmarFechamento(): void {
    this.painelService.fecharExpediente();
    this.confirmandoFechamento.set(false);
  }

  protected abrirNovoExpediente(): void {
    this.painelService.abrirNovoExpediente();
  }

  protected mensagemBloqueioFechamento(): string {
    const pendentes = this.pedidosPendentesEntrega();
    const pedidoOuPedidos = pendentes === 1 ? 'pedido ainda não foi entregue' : 'pedidos ainda não foram entregues';
    return `Não é possível fechar: ${pendentes} ${pedidoOuPedidos}.`;
  }

  protected garcomDestaqueLabel(): string {
    const destaque = this.resumoExpediente().garcomDestaque;
    return destaque ? `${destaque.nome} (${destaque.atendimentos})` : '—';
  }

  protected tempoMedioPreparoLabel(): string {
    const tempo = this.resumoExpediente().tempoMedioPreparoMin;
    return tempo === null ? '—' : `${tempo} min`;
  }

  protected larguraBarraCarga(mesasAtivas: number): number {
    const maximo = Math.max(...this.cargaGarcons().map(garcom => garcom.mesasAtivas), 1);
    return (mesasAtivas / maximo) * 100;
  }

  protected larguraBarraMesaOcupada(pedidos: number): number {
    const maximo = Math.max(...this.resumoExpediente().mesasMaisOcupadas.map(item => item.pedidos), 1);
    return (pedidos / maximo) * 100;
  }

  protected sair(): void {
    this.authService.logout();
    void this.router.navigateByUrl('/login');
  }

  protected valorContaMesa(mesa: MesaPainel): number | null {
    return this.painelService.valorContaMesa(mesa);
  }

  protected itensResumoLabel(mesa: MesaPainel): string {
    const totalItens = this.painelService.totalItensMesa(mesa);
    return totalItens === null ? '' : `${totalItens} itens`;
  }

  protected statusPedidoLabel(mesa: MesaPainel): string {
    switch (mesa.statusPedido) {
      case 'EM_PREPARO':
        return 'Em preparo';
      case 'PRONTO_ENTREGA':
        return 'Pronto para entrega';
      case 'CONTA_ABERTA':
        return 'Conta aberta';
      default:
        return '';
    }
  }

  protected nomeEtapa(etapa: number | null): string {
    if (etapa === null) return '';
    return NOMES_ETAPAS[etapa] ?? '';
  }

  protected tempoAtrasLabel(minutos: number): string {
    if (minutos < 60) return `${minutos} min`;

    const horas = Math.floor(minutos / 60);
    if (horas < 24) return `${horas}h`;

    const dias = Math.floor(horas / 24);
    return `${dias}d`;
  }

  protected iniciais(nome: string | null | undefined): string {
    if (!nome) return '?';
    return nome.charAt(0).toUpperCase();
  }

  protected nivelCarga(mesasAtivas: number): NivelCarga {
    if (mesasAtivas >= this.limiarCargaVermelho()) return 'ALTA';
    if (mesasAtivas >= this.limiarCargaLaranja()) return 'MEDIA';
    return 'BAIXA';
  }

  protected alternarConfigCarga(): void {
    this.configCargaAberta.update(aberta => !aberta);
  }

  protected atualizarLimiarLaranja(valor: number): void {
    const laranja = Math.max(1, Math.floor(valor) || 1);
    this.limiarCargaLaranja.set(laranja);
    if (this.limiarCargaVermelho() <= laranja) {
      this.limiarCargaVermelho.set(laranja + 1);
    }
  }

  protected atualizarLimiarVermelho(valor: number): void {
    const minimo = this.limiarCargaLaranja() + 1;
    this.limiarCargaVermelho.set(Math.max(minimo, Math.floor(valor) || minimo));
  }
}
