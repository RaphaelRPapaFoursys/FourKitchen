import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, effect, inject, signal, untracked } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { NivelCarga, resolverCriticidadeMesa } from '../../core/constants/urgencia.constants';
import { AcaoMesaPainel, MesaPainel, PedidoDetalheGestor } from '../../core/models/painel.models';
import { AuthService } from '../../core/services/auth';
import { FiltroEstadoPainel, OrdenacaoPainel, PainelService } from '../../core/services/painel';
import { numeroMesaBusca } from '../../core/utils/operational-search';
import { Topbar } from '../../shared/components/header/header';
import { Icon } from '../../shared/components/icon/icon';
import { MesaCard } from '../../shared/components/mesa-card/mesa-card';
import { Sidebar } from '../../shared/components/sidebar/sidebar';
import { WaiterLoadItem } from '../../shared/components/waiter-load-item/waiter-load-item';

type Ordenacao = 'CRITICO' | 'NUMERO' | 'MAIOR_VALOR' | 'MENOR_VALOR';
type Criticidade = ReturnType<typeof resolverCriticidadeMesa>;
type FiltroEstado = FiltroEstadoPainel | null;
type ModoSelecaoGarcom = 'ABRIR' | 'REATRIBUIR';
type AcaoCritica = Extract<AcaoMesaPainel, 'FECHAR_CONTA'>;

const FILTROS_ESTADO_VALIDOS: ReadonlySet<Exclude<FiltroEstado, null>> = new Set([
  'ATENCAO',
  'PROBLEMAS',
  'PRONTOS',
  'EM_PREPARO',
  'LIVRE',
  'SEM_GARCOM',
  'CONTA_ABERTA',
  'ATRASADAS',
  'AGUARDANDO_PEDIDO',
]);

function filtroEstadoValido(valor: string | null): valor is Exclude<FiltroEstado, null> {
  return valor !== null && FILTROS_ESTADO_VALIDOS.has(valor as Exclude<FiltroEstado, null>);
}

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

@Component({
  selector: 'app-gestor',
  imports: [FormsModule, CurrencyPipe, DatePipe, Icon, WaiterLoadItem, Sidebar, Topbar, MesaCard],
  templateUrl: './gestor.html',
  styleUrl: './gestor.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [PainelService],
})
export class Gestor {
  private readonly authService = inject(AuthService);
  private readonly painelService = inject(PainelService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private buscaDebounce: ReturnType<typeof setTimeout> | null = null;
  private sequenciaDetalhes = 0;
  private mesaDaRotaAberta = false;

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
  protected readonly carregandoMesas = this.painelService.carregandoMesas;
  protected readonly acaoEmAndamento = this.painelService.acaoEmAndamento;
  protected readonly descricaoAcao = this.painelService.descricaoAcao;
  protected readonly mensagemErro = this.painelService.mensagemErro;

  protected readonly buscaTermo = signal('');
  private readonly buscaAplicada = signal('');
  protected readonly filtroGarcom = signal<number | null>(null);
  protected readonly ordenacao = signal<Ordenacao>('CRITICO');
  protected readonly filtroEstado = signal<FiltroEstado>(null);
  protected readonly paginaAtual = signal(1);
  protected readonly itensPorPagina = signal(12);
  protected readonly limiarCargaLaranja = signal(5);
  protected readonly limiarCargaVermelho = signal(10);
  protected readonly filtrosAvancadosAbertos = signal(false);
  protected readonly configCargaAberta = signal(false);
  protected readonly fechamentoExpandido = signal(false);
  protected readonly selecaoGarcom = signal<SelecaoGarcomEstado | null>(null);
  protected readonly mesaDestacada = signal<number | null>(null);
  protected readonly confirmandoFechamento = signal(false);
  protected readonly confirmacaoAcao = signal<ConfirmacaoAcaoEstado | null>(null);
  protected readonly mesaDetalhes = signal<MesaPainel | null>(null);
  protected readonly pedidosDetalhes = signal<PedidoDetalheGestor[]>([]);
  protected readonly carregandoDetalhes = signal(false);
  protected readonly erroDetalhes = signal<string | null>(null);
  protected readonly etapasPedido = [
    { status: 'ENVIADO_COZINHA', label: 'Recebido' },
    { status: 'EM_PREPARO', label: 'Preparo' },
    { status: 'PRONTO', label: 'Pronto' },
    { status: 'ENTREGUE', label: 'Entregue' },
  ] as const;

  protected readonly mesasPagina = this.painelService.mesas;
  protected readonly totalPaginas = this.painelService.totalPaginas;
  protected readonly totalElementos = this.painelService.totalElementos;
  protected readonly paginaEfetiva = this.painelService.paginaEfetiva;
  protected readonly temPaginaAnterior = this.painelService.temPaginaAnterior;
  protected readonly temProximaPagina = this.painelService.temProximaPagina;

  constructor() {
    const filtroRota = this.route.snapshot.queryParamMap.get('filtro');
    if (filtroEstadoValido(filtroRota)) {
      this.filtroEstado.set(filtroRota);
    }

    const garcomIdRota = Number(this.route.snapshot.queryParamMap.get('garcomId'));
    if (Number.isInteger(garcomIdRota) && garcomIdRota > 0) {
      this.filtroGarcom.set(garcomIdRota);
    }

    const mesaIdRota = Number(this.route.snapshot.queryParamMap.get('mesa'));
    const deveAbrirDetalhes = this.route.snapshot.queryParamMap.get('abrirDetalhes') === 'true';

    this.destroyRef.onDestroy(() => {
      if (this.buscaDebounce !== null) {
        clearTimeout(this.buscaDebounce);
      }
    });

    // Qualquer mudança de busca/filtro/ordenação/tamanho volta para a primeira página.
    effect(() => {
      this.buscaAplicada();
      this.filtroGarcom();
      this.filtroEstado();
      this.ordenacao();
      this.itensPorPagina();
      this.paginaAtual.set(1);
    });

    effect(() => {
      const consulta = {
        page: this.paginaAtual() - 1,
        size: this.itensPorPagina(),
        sort: this.ordenacaoApi(),
        filtroEstado: this.filtroEstado(),
        garcomId: this.filtroGarcom(),
        busca: this.buscaAplicada(),
      };

      untracked(() => {
        void this.painelService.atualizarConsulta(consulta);
      });
    });

    effect(() => {
      const mesa = deveAbrirDetalhes && Number.isInteger(mesaIdRota) && mesaIdRota > 0
        ? this.mesasPagina().find(item => item.id === mesaIdRota)
        : null;

      if (mesa && !this.mesaDaRotaAberta) {
        this.mesaDaRotaAberta = true;
        untracked(() => this.verPedido(mesa));
      }
    });
  }

  protected readonly temFiltrosAtivos = computed(
    () =>
      this.buscaTermo().trim() !== '' ||
      this.filtroGarcom() !== null ||
      this.filtroEstado() !== null,
  );

  protected readonly quantidadeFiltrosAtivos = computed(
    () => Number(this.filtroGarcom() !== null) + Number(this.filtroEstado() !== null),
  );

  protected atualizarBusca(valor: string): void {
    this.buscaTermo.set(valor);

    if (this.buscaDebounce !== null) {
      clearTimeout(this.buscaDebounce);
    }

    if (valor.trim() === '') {
      this.buscaAplicada.set('');
      return;
    }

    this.buscaDebounce = setTimeout(() => {
      this.buscaAplicada.set(numeroMesaBusca(valor) ?? valor.trim());
      this.buscaDebounce = null;
    }, 250);
  }

  protected readonly mesaSelecaoGarcom = computed(() => {
    const selecao = this.selecaoGarcom();
    return selecao === null ? null : (this.painelService.mesas().find(mesa => mesa.id === selecao.mesaId) ?? null);
  });

  protected criticidadeCard(mesa: MesaPainel): Criticidade {
    return resolverCriticidadeMesa(mesa);
  }

  protected acaoPrimaria(mesa: MesaPainel): { tipo: AcaoMesaPainel; label: string } {
    return this.painelService.acaoPrimaria(mesa);
  }

  protected executarAcao(mesa: MesaPainel): void {
    if (this.acaoPrimaria(mesa).tipo === 'VER_PEDIDO') {
      this.verPedido(mesa);
      return;
    }

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
      case 'VER_PEDIDO': break;
    }
  }

  protected fecharErro(): void {
    this.painelService.limparErro();
  }

  protected verPedido(mesa: MesaPainel): void {
    this.mesaDetalhes.set(mesa);
    void this.carregarDetalhes(mesa);
  }

  protected fecharDetalhes(): void {
    this.sequenciaDetalhes++;
    this.mesaDetalhes.set(null);
    this.pedidosDetalhes.set([]);
    this.erroDetalhes.set(null);
  }

  protected recarregarDetalhes(): void {
    const mesa = this.mesaDetalhes();
    if (mesa) void this.carregarDetalhes(mesa);
  }

  private async carregarDetalhes(mesa: MesaPainel): Promise<void> {
    const sequencia = ++this.sequenciaDetalhes;
    this.carregandoDetalhes.set(true);
    this.erroDetalhes.set(null);
    this.pedidosDetalhes.set([]);

    try {
      const pedidos = await this.painelService.buscarPedidosDetalhados(mesa.id);
      if (sequencia !== this.sequenciaDetalhes) return;
      this.pedidosDetalhes.set(this.ordenarPedidosMaisNovos(pedidos ?? []));
    } catch {
      if (sequencia !== this.sequenciaDetalhes) return;
      this.erroDetalhes.set('Não foi possível carregar os itens dos pedidos. Tente novamente.');
    } finally {
      if (sequencia === this.sequenciaDetalhes) this.carregandoDetalhes.set(false);
    }
  }

  protected totalItensDetalhes(): number {
    return this.pedidosDetalhes().flatMap(pedido => pedido.itens)
      .filter(item => item.status !== 'REMOVIDO')
      .reduce((total, item) => total + item.quantidade, 0);
  }

  protected valorTotalDetalhes(): number {
    return this.pedidosDetalhes().reduce((total, pedido) => total + this.valorPedidoDetalhado(pedido), 0);
  }

  protected valorPedidoDetalhado(pedido: PedidoDetalheGestor): number {
    if (pedido.status === 'CANCELADO') return 0;
    return pedido.itens
      .filter(item => item.status !== 'REMOVIDO')
      .reduce((total, item) => total + item.precoUnitario * item.quantidade, 0);
  }

  private ordenarPedidosMaisNovos(pedidos: PedidoDetalheGestor[]): PedidoDetalheGestor[] {
    return [...pedidos].sort((a, b) => {
      const dataA = new Date(a.dataCriacao).getTime();
      const dataB = new Date(b.dataCriacao).getTime();
      const timestampA = Number.isNaN(dataA) ? 0 : dataA;
      const timestampB = Number.isNaN(dataB) ? 0 : dataB;
      return timestampB - timestampA || b.id - a.id;
    });
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

  protected abrirConfirmacaoAcao(mesa: MesaPainel, tipo: AcaoCritica): void {
    const numeroMesa = mesa.numero.toString().padStart(2, '0');

    this.confirmacaoAcao.set({
      mesaId: mesa.id,
      numeroMesa: mesa.numero,
      tipo,
      titulo: `Fechar conta da Mesa ${numeroMesa}`,
      mensagem: 'Confirme o fechamento da conta. O atendimento será registrado no resumo do expediente e a mesa será liberada.',
      confirmarLabel: 'Fechar conta',
    });
  }

  protected cancelarConfirmacaoAcao(): void {
    this.confirmacaoAcao.set(null);
  }

  protected confirmarAcaoCritica(): void {
    const confirmacao = this.confirmacaoAcao();
    if (!confirmacao || this.expedienteFechado() || this.acaoEmAndamento()) return;

    this.confirmacaoAcao.set(null);

    void this.painelService.fecharConta(confirmacao.mesaId);
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

  protected alternarFiltrosAvancados(): void {
    this.filtrosAvancadosAbertos.update(abertos => !abertos);
  }

  protected filtrarPorGarcom(id: number): void {
    this.filtroGarcom.update(atual => (atual === id ? null : id));
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
    return Math.max(1, Math.floor(valor));
  }

  private ordenacaoApi(): OrdenacaoPainel {
    switch (this.ordenacao()) {
      case 'NUMERO':
        return 'numero,asc';
      case 'MAIOR_VALOR':
        return 'valor,desc';
      case 'MENOR_VALOR':
        return 'valor,asc';
      case 'CRITICO':
      default:
        return 'criticidade';
    }
  }

  protected limparFiltros(): void {
    this.atualizarBusca('');
    this.filtroGarcom.set(null);
    this.filtroEstado.set(null);
  }

  protected atualizarDados(): void {
    void this.painelService.recarregarPainel();
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
