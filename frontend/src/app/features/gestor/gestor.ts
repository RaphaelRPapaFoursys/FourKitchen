import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { nivelCargaGarcom, NivelCarga, resolverCriticidadeMesa } from '../../core/constants/urgencia.constants';
import { MesaPainel, StatusMesaPainel } from '../../core/models/painel.models';
import { AuthService } from '../../core/services/auth';
import { PainelService } from '../../core/services/painel';

type Ordenacao = 'CRITICO' | 'NUMERO';
type Criticidade = ReturnType<typeof resolverCriticidadeMesa>;
type FiltroEstado = 'PROBLEMAS' | 'PRONTOS' | 'EM_PREPARO' | null;
type ModoSelecaoGarcom = 'ABRIR' | 'REATRIBUIR';

interface SelecaoGarcomEstado {
  mesaId: number;
  numeroMesa: number;
  modo: ModoSelecaoGarcom;
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
  imports: [FormsModule, CurrencyPipe],
  templateUrl: './gestor.html',
  styleUrl: './gestor.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
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
  protected readonly mensagemErro = this.painelService.mensagemErro;

  protected readonly buscaTermo = signal('');
  protected readonly filtroGarcom = signal<string | null>(null);
  protected readonly valorMin = signal<number | null>(null);
  protected readonly valorMax = signal<number | null>(null);
  protected readonly ordenacao = signal<Ordenacao>('CRITICO');
  protected readonly filtroEstado = signal<FiltroEstado>(null);
  protected readonly fechamentoExpandido = signal(false);
  protected readonly selecaoGarcom = signal<SelecaoGarcomEstado | null>(null);
  protected readonly mesaDestacada = signal<number | null>(null);
  protected readonly confirmandoFechamento = signal(false);

  protected readonly nomesGarcons = computed(() => this.cargaGarcons().map(garcom => garcom.nome));

  protected readonly mesasFiltradas = computed(() => {
    const termo = this.buscaTermo().trim().toLowerCase();
    const garcom = this.filtroGarcom();
    const min = this.valorMin();
    const max = this.valorMax();
    const estado = this.filtroEstado();
    const ordenacao = this.ordenacao();

    const filtradas = this.painelService.mesas().filter(item => {
      if (termo) {
        const correspondeNumero = item.numero.toString().includes(termo);
        const correspondeGarcom = item.garcom?.toLowerCase().includes(termo) ?? false;
        const correspondePedido = this.statusPedidoLabel(item).toLowerCase().includes(termo);
        if (!correspondeNumero && !correspondeGarcom && !correspondePedido) return false;
      }
      if (garcom && item.garcom !== garcom) return false;
      const valorConta = this.valorContaMesa(item);
      if (min !== null && (valorConta === null || valorConta < min)) return false;
      if (max !== null && (valorConta === null || valorConta > max)) return false;
      if (estado === 'PROBLEMAS' && this.criticidadeCard(item) !== 'critico') return false;
      if (estado === 'PRONTOS' && item.statusPedido !== 'PRONTO_ENTREGA') return false;
      if (estado === 'EM_PREPARO' && item.statusPedido !== 'EM_PREPARO') return false;
      return true;
    });

    return [...filtradas].sort((a, b) => {
      if (ordenacao === 'NUMERO') return a.numero - b.numero;
      return CRITICIDADE_RANK[a.status](a) - CRITICIDADE_RANK[b.status](b);
    });
  });

  protected readonly temFiltrosAtivos = computed(
    () =>
      this.buscaTermo().trim() !== '' ||
      this.filtroGarcom() !== null ||
      this.valorMin() !== null ||
      this.valorMax() !== null ||
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

  protected acaoPrimaria(mesa: MesaPainel): { tipo: string; label: string } {
    return this.painelService.acaoPrimaria(mesa);
  }

  protected acaoPrimariaIndisponivel(mesa: MesaPainel): boolean {
    return this.expedienteFechado() || this.acaoEmAndamento() || this.acaoPrimaria(mesa).tipo === 'VER_PEDIDO';
  }

  protected executarAcao(mesa: MesaPainel): void {
    if (this.expedienteFechado() || this.acaoEmAndamento()) return;

    switch (this.acaoPrimaria(mesa).tipo) {
      case 'ABRIR_MESA':
        this.selecaoGarcom.set({ mesaId: mesa.id, numeroMesa: mesa.numero, modo: 'ABRIR' });
        break;
      case 'REATRIBUIR_GARCOM':
        this.abrirReatribuicao(mesa);
        break;
      case 'FECHAR_CONTA':
        void this.painelService.fecharConta(mesa.id);
        break;
      case 'MARCAR_ENTREGUE':
        void this.painelService.marcarEntregue(mesa.id);
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
    this.selecaoGarcom.set({ mesaId: mesa.id, numeroMesa: mesa.numero, modo: 'REATRIBUIR' });
  }

  protected fecharSelecaoGarcom(): void {
    this.selecaoGarcom.set(null);
  }

  protected garcomJaAtribuido(nome: string): boolean {
    const selecao = this.selecaoGarcom();
    if (!selecao || selecao.modo !== 'REATRIBUIR') return false;
    return this.mesaSelecaoGarcom()?.garcom === nome;
  }

  protected confirmarSelecaoGarcom(nome: string): void {
    const selecao = this.selecaoGarcom();
    if (!selecao) return;

    const garcom = this.cargaGarcons().find(item => item.nome === nome);
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

  protected limparFiltros(): void {
    this.buscaTermo.set('');
    this.filtroGarcom.set(null);
    this.valorMin.set(null);
    this.valorMax.set(null);
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

  protected reabrirExpediente(): void {
    this.painelService.reabrirExpediente();
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
    return nivelCargaGarcom(mesasAtivas);
  }
}
