import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { LIMIARES_URGENCIA, nivelCargaGarcom, NivelCarga } from '../../core/constants/urgencia.constants';
import { MesaPainel, StatusMesaPainel } from '../../core/models/painel.models';
import { AuthService } from '../../core/services/auth';
import { PainelService } from '../../core/services/painel';

type Ordenacao = 'CRITICO' | 'NUMERO';

/**
 * critico/atencao: alerta de tempo (vermelho/laranja).
 * ok: sinal positivo explícito (ex.: pedido pronto dentro da meta).
 * emAndamento: fluxo normal, sem julgamento (ex.: em preparo dentro do tempo).
 * info: estado informativo que não é sobre urgência (ex.: conta aberta).
 * livre/fechada: status estrutural da mesa, não do pedido.
 */
type Criticidade = 'critico' | 'atencao' | 'ok' | 'info' | 'emAndamento' | 'livre' | 'fechada';
type FiltroEstado = 'PROBLEMAS' | 'PRONTOS' | 'EM_PREPARO' | null;

const NOMES_ETAPAS: Record<number, string> = {
  1: 'Pedido enviado à cozinha',
  2: 'Em preparo',
  3: 'Finalização',
  4: 'Pronto para entrega',
};

const CRITICIDADE_RANK: Record<StatusMesaPainel, (mesa: MesaPainel) => number> = {
  OCUPADA: mesa => {
    if (mesa.acao.tipo === 'REATRIBUIR_GARCOM') return 0;
    if (mesa.statusPedido === 'AGUARDANDO_ENTREGA') return 1;
    if (mesa.statusPedido === 'EM_PREPARO') return 2;
    if (mesa.statusPedido === 'PRONTO_ENTREGA') return 3;
    return 4;
  },
  LIVRE: () => 5,
  FECHADA: () => 6,
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

  protected readonly buscaTermo = signal('');
  protected readonly filtroGarcom = signal<string | null>(null);
  protected readonly valorMin = signal<number | null>(null);
  protected readonly valorMax = signal<number | null>(null);
  protected readonly ordenacao = signal<Ordenacao>('CRITICO');
  protected readonly filtroEstado = signal<FiltroEstado>(null);
  protected readonly fechamentoExpandido = signal(false);
  protected readonly mesaEmReatribuicao = signal<number | null>(null);
  protected readonly mesaDestacada = signal<number | null>(null);
  protected readonly confirmandoFechamento = signal(false);

  protected readonly nomesGarcons = computed(() =>
    Array.from(
      new Set(
        this.painelService
          .mesas()
          .map(mesa => mesa.garcom)
          .filter((garcom): garcom is string => garcom !== null),
      ),
    ).sort(),
  );

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

  protected readonly garcomEmReatribuicao = computed(() => {
    const numero = this.mesaEmReatribuicao();
    return numero === null ? null : (this.painelService.mesas().find(mesa => mesa.numero === numero) ?? null);
  });

  protected criticidadeCard(mesa: MesaPainel): Criticidade {
    if (mesa.status === 'LIVRE') return 'livre';
    if (mesa.status === 'FECHADA') return 'fechada';
    if (mesa.acao.tipo === 'REATRIBUIR_GARCOM') return 'critico';

    if (mesa.statusPedido === 'AGUARDANDO_ENTREGA' || mesa.statusPedido === 'EM_PREPARO') {
      return (mesa.tempoMinutos ?? 0) >= LIMIARES_URGENCIA.preparoAtencaoMinutos ? 'atencao' : 'emAndamento';
    }

    if (mesa.statusPedido === 'PRONTO_ENTREGA') {
      const minutos = mesa.tempoMinutos ?? 0;
      if (minutos > LIMIARES_URGENCIA.prontoAtencaoMaxMinutos) return 'critico';
      if (minutos > LIMIARES_URGENCIA.prontoOkMaxMinutos) return 'atencao';
      return 'ok';
    }

    if (mesa.statusPedido === 'CONTA_ABERTA') return 'info';
    return 'emAndamento';
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
      mesa.acao.tipo !== 'VER_PEDIDO' &&
      (this.criticidadeCard(mesa) === 'critico' || this.criticidadeCard(mesa) === 'atencao')
    );
  }

  protected executarAcao(mesa: MesaPainel): void {
    if (this.expedienteFechado()) return;

    switch (mesa.acao.tipo) {
      case 'ABRIR_MESA':
        this.painelService.abrirMesa(mesa.numero);
        break;
      case 'FECHAR_CONTA':
        this.painelService.fecharConta(mesa.numero);
        break;
      case 'LIBERAR_MESA':
        this.painelService.liberarMesa(mesa.numero);
        break;
      case 'MARCAR_ENTREGUE':
        this.painelService.marcarEntregue(mesa.numero);
        break;
      case 'REATRIBUIR_GARCOM':
        this.mesaEmReatribuicao.set(mesa.numero);
        break;
      case 'VER_PEDIDO':
        this.verPedido();
        break;
    }
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

  protected fecharReatribuicao(): void {
    this.mesaEmReatribuicao.set(null);
  }

  protected confirmarReatribuicao(nomeGarcom: string): void {
    const numero = this.mesaEmReatribuicao();
    if (numero === null) return;

    this.painelService.reatribuirGarcom(numero, nomeGarcom);
    this.mesaEmReatribuicao.set(null);
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
    const itens = this.painelService.itensAgregadosMesa(mesa);
    if (!itens) return '';

    const partes = [`${itens.totalItens} itens`];

    if (itens.pratos > 0) {
      partes.push(`${itens.pratos} prato${itens.pratos > 1 ? 's' : ''}`);
    }
    if (itens.bebidas > 0) {
      partes.push(`${itens.bebidas} bebida${itens.bebidas > 1 ? 's' : ''}`);
    }
    if (itens.sobremesas > 0) {
      partes.push(`${itens.sobremesas} sobremesa${itens.sobremesas > 1 ? 's' : ''}`);
    }

    return partes.join(' · ');
  }

  protected statusPedidoLabel(mesa: MesaPainel): string {
    switch (mesa.statusPedido) {
      case 'EM_PREPARO':
        return 'Em preparo';
      case 'AGUARDANDO_ENTREGA':
        return 'Aguardando entrega';
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

  protected iniciais(nome: string | null | undefined): string {
    if (!nome) return '?';
    return nome.charAt(0).toUpperCase();
  }

  protected nivelCarga(mesasAtivas: number): NivelCarga {
    return nivelCargaGarcom(mesasAtivas);

  }

  protected variacaoAbs(valor: number): number {
    return Math.abs(valor);
  }
}

