import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { TimeoutError, finalize, timeout } from 'rxjs';

import {
  ChamadaPendenteMesaResponse,
  DecisaoProblemaGarcomRequest,
  MesaGarcomDetalheResponse,
  MesaGarcomResponse,
  MesaProblemasGarcomResponse,
  PedidoDetalheGarcomResponse,
  ProblemaPedidoGarcomResponse,
} from '../../core/models/garcom.models';
import { PedidoStatus } from '../../core/models/order.models';
import { CategoriaCardapioResponse, ProdutoCardapioResponse } from '../../core/models/menu.models';
import { AuthService } from '../../core/services/auth';
import { GarcomChamadaService } from '../../core/services/garcom-chamada';
import { GarcomMesaService } from '../../core/services/garcom-mesa';
import { MenuService } from '../../core/services/menu.service';
import {
  normalizarBuscaOperacional,
  mesaCorrespondeBuscaParcial,
  numeroContemBusca,
  numeroBuscaOperacional,
} from '../../core/utils/operational-search';
import { Badge, BadgeVariant } from '../../shared/components/badge/badge';
import { Icon } from '../../shared/components/icon/icon';
import { KpiCard } from '../../shared/components/kpi-card/kpi-card';
import { UserMenu } from '../../shared/components/user-menu/user-menu';

type FiltroMesa = 'todas' | 'chamadas' | 'problemas';
type AcaoDecisao = 'REMOVER_ITEM' | 'SUBSTITUIR_ITEM' | 'CANCELAR_PEDIDO';
type ModoDivisaoConta = 'UNICA' | 'DIVIDIDA';
type CancelamentoDireto = {
  mesa: MesaGarcomResponse;
  pedido: PedidoDetalheGarcomResponse;
};

const STATUS_PROBLEMA = new Set(['AGUARDANDO_DECISAO', 'PROBLEMA_COZINHA']);
const INTERVALO_ATUALIZACAO_MS = 10_000;

@Component({
  selector: 'app-garcom',
  standalone: true,
  imports: [Badge, Icon, KpiCard, UserMenu],
  templateUrl: './garcom.html',
  styleUrl: './garcom.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Garcom {
  private readonly garcomMesaService = inject(GarcomMesaService);
  private readonly garcomChamadaService = inject(GarcomChamadaService);
  private readonly menuService = inject(MenuService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  private dashboardEmCarregamento = false;
  private atualizacaoPendente: { silencioso: boolean; forcarDetalhes: boolean } | null = null;
  private intervaloAtualizacao: ReturnType<typeof setInterval> | null = null;

  protected readonly mesas = signal<MesaGarcomResponse[]>([]);
  protected readonly busca = signal('');
  protected readonly filtro = signal<FiltroMesa>('todas');
  protected readonly carregando = signal(false);
  protected readonly atualizandoSilencioso = signal(false);
  protected readonly erro = signal('');
  protected readonly sucesso = signal('');
  protected readonly notificacaoEmAcao = signal<number | null>(null);
  protected readonly detalhesPorMesa = signal<Record<number, MesaGarcomDetalheResponse>>({});
  protected readonly detalhesEmCarregamento = signal<ReadonlySet<number>>(new Set());
  protected readonly errosDetalheMesa = signal<Record<number, string>>({});
  protected readonly mesaDetalhesAberta = signal<MesaGarcomResponse | null>(null);
  protected readonly mesaParaFechar = signal<MesaGarcomResponse | null>(null);
  protected readonly mesaEmFechamento = signal<number | null>(null);
  protected readonly erroFechamento = signal('');
  protected readonly pedidoEmEntrega = signal<number | null>(null);
  protected readonly modoDivisaoConta = signal<ModoDivisaoConta>('UNICA');
  protected readonly quantidadePessoas = signal(2);

  protected readonly mesaEmDetalhe = signal<MesaGarcomResponse | null>(null);
  protected readonly detalheSelecionado = signal<MesaProblemasGarcomResponse | null>(null);
  protected readonly problemaSelecionado = signal<ProblemaPedidoGarcomResponse | null>(null);
  protected readonly carregandoDetalhe = signal(false);
  protected readonly erroDetalhe = signal('');
  protected readonly salvandoDecisao = signal(false);
  protected readonly confirmandoCancelamento = signal(false);
  protected readonly erroCancelamento = signal('');
  protected readonly cancelamentoDireto = signal<CancelamentoDireto | null>(null);
  protected readonly acaoDecisao = signal<AcaoDecisao>('REMOVER_ITEM');

  protected readonly categorias = signal<CategoriaCardapioResponse[]>([]);
  protected readonly carregandoCardapio = signal(false);
  protected readonly erroCardapio = signal('');
  protected readonly categoriaSelecionadaId = signal<number | null>(null);
  protected readonly produtoSelecionadoId = signal<number | null>(null);
  protected readonly observacaoNovoProduto = signal('');

  protected readonly nomeUsuario = computed(() =>
    this.authService.getCurrentUser()?.nome ?? 'Garcom'
  );

  protected readonly inicialUsuario = computed(() => this.nomeUsuario().trim().charAt(0).toUpperCase() || 'G');

  protected readonly produtosDaCategoria = computed<ProdutoCardapioResponse[]>(() => {
    const categoriaId = this.categoriaSelecionadaId();
    return this.categorias().find(categoria => categoria.categoriaId === categoriaId)?.produtos ?? [];
  });

  protected readonly mesaDoCancelamento = computed(() =>
    this.cancelamentoDireto()?.mesa ?? this.mesaEmDetalhe()
  );

  protected readonly pedidoDoCancelamento = computed(() => {
    const cancelamentoDireto = this.cancelamentoDireto();
    if (cancelamentoDireto) {
      return cancelamentoDireto.pedido;
    }

    const problema = this.problemaSelecionado();
    if (!problema) {
      return null;
    }

    return this.detalheSelecionado()?.pedidos.find(pedido => pedido.id === problema.idPedido) ?? null;
  });

  protected readonly mesasFiltradas = computed(() => {
    const termo = normalizarBuscaOperacional(this.busca());

    return this.mesas()
      .filter(mesa => {
        if (this.filtro() === 'chamadas' && !mesa.possuiChamadaPendente) {
          return false;
        }

        if (this.filtro() === 'problemas' && !this.mesaPossuiProblema(mesa)) {
          return false;
        }

        if (!termo) {
          return true;
        }

        if (mesaCorrespondeBuscaParcial(mesa.numero, termo)) {
          return true;
        }

        // A API já limita esta coleção às mesas atribuídas ao garçom;
        // a busca apenas reduz essa coleção, sem consultar outras mesas.
        if (numeroBuscaOperacional(termo) !== null) {
          return mesa.pedidosAtivos.some(pedido =>
            numeroContemBusca(pedido.id, termo) || numeroContemBusca(pedido.codigo, termo),
          );
        }

        return normalizarBuscaOperacional([
          mesa.status,
          ...mesa.pedidosAtivos.flatMap(pedido => [pedido.codigo, pedido.status]),
          ...mesa.chamadasPendentes.map(chamada => chamada.mensagem),
        ].join(' ')).includes(termo);
      })
      .sort((a, b) => a.numero - b.numero);
  });

  protected readonly totalMesas = computed(() => this.mesas().length);
  protected readonly mesasComChamadas = computed(() =>
    this.mesas().filter(mesa => mesa.possuiChamadaPendente).length
  );
  protected readonly mesasComProblemas = computed(() =>
    this.mesas().filter(mesa => this.mesaPossuiProblema(mesa)).length
  );
  protected readonly totalPedidosAbertos = computed(() =>
    this.mesas().reduce((total, mesa) => total + this.pedidosEmAndamento(mesa).length, 0)
  );
  protected readonly totalPedidosProntos = computed(() =>
    this.mesas().reduce((total, mesa) => total + this.pedidosProntos(mesa).length, 0)
  );
  protected readonly totalChamadas = computed(() =>
    this.mesas().reduce((total, mesa) => total + mesa.chamadasPendentes.length, 0)
  );

  constructor() {
    this.carregarDashboard();
    this.intervaloAtualizacao = setInterval(
      () => this.carregarDashboard(true),
      INTERVALO_ATUALIZACAO_MS,
    );
    this.destroyRef.onDestroy(() => {
      if (this.intervaloAtualizacao !== null) {
        clearInterval(this.intervaloAtualizacao);
      }
    });
  }

  protected carregarDashboard(silencioso = false, forcarDetalhes = !silencioso): void {
    if (this.dashboardEmCarregamento) {
      if (forcarDetalhes || !silencioso) {
        this.atualizacaoPendente = {
          silencioso: this.atualizacaoPendente?.silencioso === false ? false : silencioso,
          forcarDetalhes: forcarDetalhes || (this.atualizacaoPendente?.forcarDetalhes ?? false),
        };
      }
      return;
    }

    this.dashboardEmCarregamento = true;
    this.erro.set('');
    if (!silencioso) {
      this.carregando.set(true);
    } else {
      this.atualizandoSilencioso.set(true);
    }

    this.garcomMesaService
      .listarMesas()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.dashboardEmCarregamento = false;
          if (!silencioso) {
            this.carregando.set(false);
          } else {
            this.atualizandoSilencioso.set(false);
          }

          const pendente = this.atualizacaoPendente;
          this.atualizacaoPendente = null;
          if (pendente) {
            queueMicrotask(() => this.carregarDashboard(pendente.silencioso, pendente.forcarDetalhes));
          }
        })
      )
      .subscribe({
        next: mesas => {
          this.mesas.set(mesas);
          const mesaAberta = this.mesaDetalhesAberta();
          if (mesaAberta) {
            this.mesaDetalhesAberta.set(mesas.find(mesa => mesa.idMesa === mesaAberta.idMesa) ?? null);
          }
          this.sincronizarDetalhes(mesas, forcarDetalhes);
        },
        error: error => this.erro.set(this.getErrorMessage(error, 'Nao foi possivel carregar o dashboard do garcom.')),
      });
  }

  protected atualizarBusca(event: Event): void {
    this.busca.set((event.target as HTMLInputElement).value);
  }

  protected alterarFiltro(filtro: FiltroMesa): void {
    this.filtro.set(filtro);
  }

  protected sair(): void {
    this.authService.logout();
    void this.router.navigateByUrl('/login');
  }

  protected atenderNotificacao(mesa: MesaGarcomResponse, chamada: ChamadaPendenteMesaResponse): void {
    this.notificacaoEmAcao.set(chamada.id);
    this.erro.set('');

    this.garcomChamadaService
      .concluirChamada(chamada.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.notificacaoEmAcao.set(null))
      )
      .subscribe({
        next: () => {
          this.removerChamada(mesa.idMesa, chamada.id);
          this.sucesso.set(`Chamada da mesa ${mesa.numero} atendida.`);
        },
        error: error => this.erro.set(this.getErrorMessage(error, 'Nao foi possivel concluir a chamada.')),
      });
  }

  protected fazerPedido(mesa: MesaGarcomResponse): void {
    void this.router.navigate(['/garcom/mesas', mesa.idMesa, 'pedido']);
  }

  protected abrirSolicitacao(mesa: MesaGarcomResponse): void {
    this.mesaEmDetalhe.set(mesa);
    this.acaoDecisao.set('REMOVER_ITEM');
    this.categoriaSelecionadaId.set(null);
    this.produtoSelecionadoId.set(null);
    this.observacaoNovoProduto.set('');
    this.erro.set('');
    this.erroCancelamento.set('');
    this.carregarSolicitacao(mesa);
  }

  protected tentarCarregarSolicitacao(): void {
    const mesa = this.mesaEmDetalhe();
    if (!mesa || this.carregandoDetalhe()) {
      return;
    }

    this.carregarSolicitacao(mesa);
  }

  protected fecharSolicitacao(): void {
    if (this.salvandoDecisao() || this.confirmandoCancelamento()) {
      return;
    }

    this.mesaEmDetalhe.set(null);
    this.detalheSelecionado.set(null);
    this.problemaSelecionado.set(null);
    this.observacaoNovoProduto.set('');
    this.cancelamentoDireto.set(null);
  }

  protected selecionarProblema(problema: ProblemaPedidoGarcomResponse): void {
    this.problemaSelecionado.set(problema);
    this.acaoDecisao.set('REMOVER_ITEM');
    this.categoriaSelecionadaId.set(null);
    this.produtoSelecionadoId.set(null);
    this.observacaoNovoProduto.set('');
  }

  protected alterarAcaoDecisao(acao: AcaoDecisao): void {
    this.acaoDecisao.set(acao);
    this.erroCancelamento.set('');
    if (acao === 'SUBSTITUIR_ITEM' && this.categorias().length === 0 && !this.carregandoCardapio()) {
      this.carregarCardapio();
    }

    if (acao !== 'SUBSTITUIR_ITEM') {
      this.categoriaSelecionadaId.set(null);
      this.produtoSelecionadoId.set(null);
      this.observacaoNovoProduto.set('');
    }
  }

  protected alterarCategoria(event: Event): void {
    const valor = Number((event.target as HTMLSelectElement).value);
    this.categoriaSelecionadaId.set(Number.isFinite(valor) && valor > 0 ? valor : null);
    this.produtoSelecionadoId.set(null);
  }

  protected alterarProduto(event: Event): void {
    const valor = Number((event.target as HTMLSelectElement).value);
    this.produtoSelecionadoId.set(Number.isFinite(valor) && valor > 0 ? valor : null);
  }

  protected alterarObservacaoNovoProduto(event: Event): void {
    this.observacaoNovoProduto.set((event.target as HTMLTextAreaElement).value);
  }

  protected podeRegistrarDecisao(): boolean {
    return this.problemaSelecionado() !== null
      && !this.salvandoDecisao()
      && (this.acaoDecisao() !== 'SUBSTITUIR_ITEM' || this.produtoSelecionadoId() !== null);
  }

  protected registrarDecisao(): void {
    const mesa = this.mesaEmDetalhe();
    const problema = this.problemaSelecionado();
    if (!mesa || !problema || !this.podeRegistrarDecisao()) {
      return;
    }

    if (this.acaoDecisao() === 'CANCELAR_PEDIDO') {
      this.erroCancelamento.set('');
      this.cancelamentoDireto.set(null);
      this.confirmandoCancelamento.set(true);
      return;
    }

    this.enviarDecisao(mesa, problema);
  }

  protected voltarCancelamento(): void {
    if (!this.salvandoDecisao()) {
      this.confirmandoCancelamento.set(false);
      this.erroCancelamento.set('');
      this.cancelamentoDireto.set(null);
    }
  }

  protected confirmarCancelamentoPedido(): void {
    const cancelamentoDireto = this.cancelamentoDireto();
    if (cancelamentoDireto) {
      this.enviarCancelamentoDireto(cancelamentoDireto);
      return;
    }

    const mesa = this.mesaEmDetalhe();
    const problema = this.problemaSelecionado();
    if (!mesa || !problema || this.salvandoDecisao()) {
      return;
    }

    this.enviarDecisao(mesa, problema, true);
  }

  protected solicitarCancelamentoPedido(mesa: MesaGarcomResponse, pedido: PedidoDetalheGarcomResponse): void {
    if (!this.podeCancelarPedidoDiretamente(pedido) || this.salvandoDecisao() || this.confirmandoCancelamento()) {
      return;
    }

    this.erroCancelamento.set('');
    this.cancelamentoDireto.set({ mesa, pedido });
    this.confirmandoCancelamento.set(true);
  }

  private enviarDecisao(
    mesa: MesaGarcomResponse,
    problema: ProblemaPedidoGarcomResponse,
    cancelamento = false,
  ): void {
    const codigoPedido = this.pedidoCodigo(problema);
    const request = this.montarDecisao(problema);
    this.salvandoDecisao.set(true);
    this.erro.set('');
    this.erroCancelamento.set('');
    this.sucesso.set('');

    this.garcomMesaService
      .registrarDecisao(mesa.idMesa, request)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.salvandoDecisao.set(false))
      )
      .subscribe({
        next: () => {
          this.confirmandoCancelamento.set(false);
          this.cancelamentoDireto.set(null);
          this.mesaEmDetalhe.set(null);
          this.detalheSelecionado.set(null);
          this.problemaSelecionado.set(null);
          this.sucesso.set(cancelamento
            ? `Pedido #${codigoPedido} cancelado com sucesso.`
            : `Decisao da mesa ${mesa.numero} enviada para a cozinha.`);
          this.carregarDashboard(true, true);
        },
        error: error => {
          const mensagem = this.getErrorMessage(
            error,
            cancelamento
              ? 'Nao foi possivel cancelar o pedido. Tente novamente.'
              : 'Nao foi possivel registrar a decisao do cliente.',
          );
          if (cancelamento) {
            this.erroCancelamento.set(mensagem);
          } else {
            this.erro.set(mensagem);
          }
        },
      });
  }

  private enviarCancelamentoDireto(cancelamento: CancelamentoDireto): void {
    this.salvandoDecisao.set(true);
    this.erro.set('');
    this.erroCancelamento.set('');
    this.sucesso.set('');

    this.garcomMesaService
      .cancelarPedidoAntesDoPreparo(cancelamento.mesa.idMesa, cancelamento.pedido.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.salvandoDecisao.set(false)),
      )
      .subscribe({
        next: () => {
          this.confirmandoCancelamento.set(false);
          this.cancelamentoDireto.set(null);
          this.sucesso.set(`Pedido #${cancelamento.pedido.codigo} cancelado com sucesso.`);
          this.carregarDashboard(true, true);
        },
        error: error => this.erroCancelamento.set(this.getErrorMessage(
          error,
          'Nao foi possivel cancelar o pedido. O preparo pode ja ter sido iniciado.',
        )),
      });
  }

  protected abrirDetalhes(mesa: MesaGarcomResponse): void {
    this.mesaDetalhesAberta.set(mesa);
    if (!this.detalhesPorMesa()[mesa.idMesa]) {
      this.carregarDetalheMesa(mesa.idMesa);
    }
  }

  protected fecharDetalhes(): void {
    if (this.mesaEmFechamento() === null && this.mesaParaFechar() === null) {
      this.mesaDetalhesAberta.set(null);
    }
  }

  protected detalheMesa(mesa: MesaGarcomResponse): MesaGarcomDetalheResponse | null {
    return this.detalhesPorMesa()[mesa.idMesa] ?? null;
  }

  protected recarregarDetalhe(mesa: MesaGarcomResponse): void {
    this.carregarDetalheMesa(mesa.idMesa, true);
  }

  protected pedidosEmAndamento(mesa: MesaGarcomResponse): MesaGarcomResponse['pedidosAtivos'] {
    return mesa.pedidosAtivos.filter(pedido => pedido.status !== 'PRONTO' && pedido.status !== 'ENTREGUE');
  }

  protected pedidosProntos(mesa: MesaGarcomResponse): MesaGarcomResponse['pedidosAtivos'] {
    return mesa.pedidosAtivos.filter(pedido => pedido.status === 'PRONTO');
  }

  protected pedidosAtivosDetalhe(detalhe: MesaGarcomDetalheResponse): PedidoDetalheGarcomResponse[] {
    return detalhe.pedidos.filter(pedido =>
      pedido.status !== 'PRONTO'
      && pedido.status !== 'ENTREGUE'
      && pedido.status !== 'FINALIZADO'
      && pedido.status !== 'CANCELADO'
    );
  }

  protected pedidosProntosDetalhe(detalhe: MesaGarcomDetalheResponse): PedidoDetalheGarcomResponse[] {
    return detalhe.pedidos.filter(pedido => pedido.status === 'PRONTO');
  }

  protected pedidosHistoricoDetalhe(detalhe: MesaGarcomDetalheResponse): PedidoDetalheGarcomResponse[] {
    return detalhe.pedidos.filter(pedido =>
      pedido.status === 'ENTREGUE' || pedido.status === 'FINALIZADO' || pedido.status === 'CANCELADO'
    );
  }

  protected itensAtivosPedido(pedido: PedidoDetalheGarcomResponse): PedidoDetalheGarcomResponse['itens'] {
    return pedido.itens.filter(item => !this.itemRemovido(item));
  }

  protected itemRemovido(item: PedidoDetalheGarcomResponse['itens'][number]): boolean {
    return item.status === 'REMOVIDO' || item.status === 'CANCELADO';
  }

  protected pedidoCancelado(pedido: PedidoDetalheGarcomResponse): boolean {
    return pedido.status === 'CANCELADO';
  }

  protected pedidoAguardaDecisao(pedido: PedidoDetalheGarcomResponse): boolean {
    return STATUS_PROBLEMA.has(pedido.status);
  }

  protected podeCancelarPedidoDiretamente(pedido: PedidoDetalheGarcomResponse): boolean {
    return pedido.status === 'ENVIADO_COZINHA';
  }

  protected podeFecharConta(mesa: MesaGarcomResponse): boolean {
    // O endpoint de mesas ja retorna somente pedidos ativos; no contrato atual,
    // ENTREGUE e o unico status ativo que nao bloqueia o fechamento.
    return mesa.pedidosAtivos.every(pedido => pedido.status === 'ENTREGUE');
  }

  protected motivoBloqueioFechamento(mesa: MesaGarcomResponse): string {
    const bloqueadores = mesa.pedidosAtivos.filter(pedido => pedido.status !== 'ENTREGUE');
    if (bloqueadores.length === 0) {
      return '';
    }

    if (bloqueadores.some(pedido => pedido.status === 'PRONTO')) {
      return 'Entregue os pedidos prontos antes de fechar a conta.';
    }

    return `${bloqueadores.length} pedido${bloqueadores.length === 1 ? '' : 's'} ainda impede${bloqueadores.length === 1 ? '' : 'm'} o fechamento.`;
  }

  protected abrirFechamento(mesa: MesaGarcomResponse): void {
    if (!this.podeFecharConta(mesa) || this.mesaEmFechamento() !== null) {
      return;
    }
    this.erroFechamento.set('');
    this.modoDivisaoConta.set('UNICA');
    this.quantidadePessoas.set(2);
    this.mesaParaFechar.set(mesa);
  }

  protected marcarPedidoComoEntregue(mesa: MesaGarcomResponse, pedido: PedidoDetalheGarcomResponse): void {
    if (pedido.status !== 'PRONTO' || this.pedidoEmEntrega() !== null) {
      return;
    }

    this.pedidoEmEntrega.set(pedido.id);
    this.erro.set('');
    this.sucesso.set('');
    this.garcomMesaService
      .marcarPedidoComoEntregue(mesa.idMesa, pedido.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.pedidoEmEntrega.set(null)),
      )
      .subscribe({
        next: () => {
          this.sucesso.set(`Pedido #${pedido.codigo} marcado como entregue.`);
          this.carregarDashboard(true, true);
        },
        error: error => this.erro.set(this.getErrorMessage(
          error,
          'Nao foi possivel registrar a entrega do pedido. Tente novamente.',
        )),
      });
  }

  protected cancelarFechamento(): void {
    if (this.mesaEmFechamento() === null) {
      this.mesaParaFechar.set(null);
      this.erroFechamento.set('');
      this.modoDivisaoConta.set('UNICA');
    }
  }

  protected selecionarModoDivisao(modo: ModoDivisaoConta): void {
    this.modoDivisaoConta.set(modo);
  }

  protected alterarQuantidadePessoas(variacao: number): void {
    this.quantidadePessoas.update(quantidade => Math.min(20, Math.max(2, quantidade + variacao)));
  }

  protected parcelasConta(total: number): number[] {
    if (this.modoDivisaoConta() === 'UNICA') {
      return [total];
    }

    const totalEmCentavos = Math.round(total * 100);
    const quantidade = this.quantidadePessoas();
    const valorBase = Math.floor(totalEmCentavos / quantidade);
    const centavosRestantes = totalEmCentavos % quantidade;

    return Array.from(
      { length: quantidade },
      (_, indice) => (valorBase + (indice < centavosRestantes ? 1 : 0)) / 100,
    );
  }

  protected possuiAjusteDeCentavos(total: number): boolean {
    return this.modoDivisaoConta() === 'DIVIDIDA'
      && Math.round(total * 100) % this.quantidadePessoas() !== 0;
  }

  protected confirmarFechamento(): void {
    const mesa = this.mesaParaFechar();
    if (!mesa || this.mesaEmFechamento() !== null) {
      return;
    }

    this.mesaEmFechamento.set(mesa.idMesa);
    this.erroFechamento.set('');
    this.sucesso.set('');
    this.garcomMesaService
      .fecharConta(mesa.idMesa)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.mesaEmFechamento.set(null)),
      )
      .subscribe({
        next: response => {
          this.mesaParaFechar.set(null);
          this.sucesso.set(`Conta da mesa ${response.numero} fechada com sucesso.`);
          this.carregarDashboard(true);
        },
        error: error => this.erroFechamento.set(this.getErrorMessage(
          error,
          'Nao foi possivel fechar a conta. O estado da mesa pode ter mudado; atualize e tente novamente.',
        )),
      });
  }

  protected mesaPossuiProblema(mesa: MesaGarcomResponse): boolean {
    return mesa.pedidosAtivos.some(pedido => STATUS_PROBLEMA.has(pedido.status));
  }

  protected pedidosComProblema(mesa: MesaGarcomResponse): MesaGarcomResponse['pedidosAtivos'] {
    return mesa.pedidosAtivos.filter(pedido => STATUS_PROBLEMA.has(pedido.status));
  }

  protected statusPedidoLabel(status: PedidoStatus): string {
    if (status === 'AGUARDANDO_DECISAO' || status === 'PROBLEMA_COZINHA') {
      return 'Aguardando decisão';
    }

    const labels: Record<PedidoStatus, string> = {
      ENVIADO_COZINHA: 'Enviado à cozinha',
      EM_PREPARO: 'Em preparo',
      PRONTO: 'Pronto',
      ENTREGUE: 'Entregue',
      AGUARDANDO_DECISAO: 'Aguardando decisão',
      PROBLEMA_COZINHA: 'Problema na cozinha',
      FINALIZADO: 'Finalizado',
      CANCELADO: 'Cancelado',
    };
    return labels[status];
  }

  protected statusPedidoVariant(status: PedidoStatus): BadgeVariant {
    if (status === 'PRONTO' || status === 'ENTREGUE' || status === 'FINALIZADO') return 'livre';
    if (status === 'AGUARDANDO_DECISAO' || status === 'PROBLEMA_COZINHA') return 'critico';
    if (status === 'EM_PREPARO') return 'atencao';
    return 'neutral';
  }

  protected pedidoCodigo(problema: ProblemaPedidoGarcomResponse): number | string {
    return this.detalheSelecionado()?.pedidos.find(pedido => pedido.id === problema.idPedido)?.codigo ?? problema.idPedido;
  }

  protected nomeItemProblema(problema: ProblemaPedidoGarcomResponse): string {
    const pedido = this.detalheSelecionado()?.pedidos.find(item => item.id === problema.idPedido);
    return pedido?.itens.find(item => item.id === problema.idProdutoPedido)?.nomeProduto
      ?? `Item #${problema.idProdutoPedido}`;
  }

  protected formatarMoeda(valor: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
  }

  protected valorPedido(pedido: PedidoDetalheGarcomResponse): number {
    return this.itensAtivosPedido(pedido).reduce(
      (total, item) => total + item.precoUnitario * item.quantidade,
      0,
    );
  }

  protected formatarDataHora(valor: string | null): string {
    if (!valor) return 'Não informado';
    const data = new Date(valor);
    if (Number.isNaN(data.getTime())) return 'Não informado';
    const dataFormatada = new Intl.DateTimeFormat('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    }).format(data);
    const horario = new Intl.DateTimeFormat('pt-BR', {
      hour: '2-digit',
      minute: '2-digit',
    }).format(data);
    return `${dataFormatada} | ${horario}`;
  }

  protected trackMesa(_: number, mesa: MesaGarcomResponse): number {
    return mesa.idMesa;
  }

  protected trackChamada(_: number, chamada: ChamadaPendenteMesaResponse): number {
    return chamada.id;
  }

  protected trackProblema(_: number, problema: ProblemaPedidoGarcomResponse): string {
    return `${problema.idPedido}-${problema.idProdutoPedido}`;
  }

  protected trackCategoria(_: number, categoria: CategoriaCardapioResponse): number {
    return categoria.categoriaId;
  }

  protected trackProduto(_: number, produto: ProdutoCardapioResponse): number {
    return produto.id;
  }

  private sincronizarDetalhes(mesas: MesaGarcomResponse[], forcar: boolean): void {
    const idsAtuais = new Set(mesas.map(mesa => mesa.idMesa));
    this.detalhesPorMesa.update(detalhes => Object.fromEntries(
      Object.entries(detalhes).filter(([id]) => idsAtuais.has(Number(id))),
    ));

    for (const mesa of mesas) {
      if (mesa.idAtendimento !== null && (forcar || !this.detalhesPorMesa()[mesa.idMesa])) {
        this.carregarDetalheMesa(mesa.idMesa, forcar);
      }
    }
  }

  private carregarDetalheMesa(idMesa: number, forcar = false): void {
    if (this.detalhesEmCarregamento().has(idMesa) || (!forcar && this.detalhesPorMesa()[idMesa])) {
      return;
    }

    this.detalhesEmCarregamento.update(ids => new Set(ids).add(idMesa));
    this.errosDetalheMesa.update(erros => ({ ...erros, [idMesa]: '' }));
    this.garcomMesaService
      .detalharMesa(idMesa)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.detalhesEmCarregamento.update(ids => {
          const atualizados = new Set(ids);
          atualizados.delete(idMesa);
          return atualizados;
        })),
      )
      .subscribe({
        next: detalhe => this.detalhesPorMesa.update(detalhes => ({ ...detalhes, [idMesa]: detalhe })),
        error: error => this.errosDetalheMesa.update(erros => ({
          ...erros,
          [idMesa]: this.getErrorMessage(error, 'Detalhes e valor da conta indisponiveis.'),
        })),
      });
  }

  private montarDecisao(problema: ProblemaPedidoGarcomResponse): DecisaoProblemaGarcomRequest {
    const acao = this.acaoDecisao();
    return {
      idPedido: problema.idPedido,
      idProdutoPedido: problema.idProdutoPedido,
      novoStatusProdutoPedido: acao === 'REMOVER_ITEM' ? 'REMOVIDO' : 'DISPONIVEL',
      pedidoCancelado: acao === 'CANCELAR_PEDIDO',
      idNovoProduto: acao === 'SUBSTITUIR_ITEM' ? this.produtoSelecionadoId() : null,
      observacaoNovoProduto: acao === 'SUBSTITUIR_ITEM'
        ? this.observacaoNovoProduto().trim() || null
        : null,
    };
  }

  protected carregarCardapio(): void {
    this.carregandoCardapio.set(true);
    this.erroCardapio.set('');
    this.menuService
      .getMenu('garcom')
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.carregandoCardapio.set(false))
      )
      .subscribe({
        next: categorias => this.categorias.set(categorias.filter(categoria => categoria.produtos.length > 0)),
        error: error => this.erroCardapio.set(
          this.getErrorMessage(error, 'Nao foi possivel carregar os produtos disponiveis.')
        ),
      });
  }

  private carregarSolicitacao(mesa: MesaGarcomResponse): void {
    this.carregandoDetalhe.set(true);
    this.erroDetalhe.set('');
    this.detalheSelecionado.set(null);
    this.problemaSelecionado.set(null);

    this.garcomMesaService
      .listarProblemas(mesa.idMesa)
      .pipe(
        timeout({ first: 10_000 }),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.carregandoDetalhe.set(false))
      )
      .subscribe({
        next: detalhe => {
          this.detalheSelecionado.set(detalhe);
          this.problemaSelecionado.set(detalhe.problemas[0] ?? null);
        },
        error: error => this.erroDetalhe.set(
          error instanceof TimeoutError
            ? 'A consulta demorou mais de 10 segundos. Verifique os servicos e tente novamente.'
            : this.getErrorMessage(error, 'Nao foi possivel carregar a solicitacao da cozinha.')
        ),
      });
  }

  private removerChamada(idMesa: number, idChamada: number): void {
    this.mesas.update(mesas =>
      mesas.map(mesa => {
        if (mesa.idMesa !== idMesa) {
          return mesa;
        }

        const chamadasPendentes = mesa.chamadasPendentes.filter(chamada => chamada.id !== idChamada);
        return { ...mesa, chamadasPendentes, possuiChamadaPendente: chamadasPendentes.length > 0 };
      })
    );

    this.mesaDetalhesAberta.update(mesa => {
      if (!mesa || mesa.idMesa !== idMesa) return mesa;
      const chamadasPendentes = mesa.chamadasPendentes.filter(chamada => chamada.id !== idChamada);
      return { ...mesa, chamadasPendentes, possuiChamadaPendente: chamadasPendentes.length > 0 };
    });
  }

  private getErrorMessage(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      const apiError = this.getApiError(error.error);
      if (apiError?.msgError) {
        return apiError.msgError;
      }

      if (error.status === 401) {
        return 'Sessao expirada. Faca login novamente.';
      }

      if (error.status === 403) {
        return 'Voce nao tem permissao para executar esta acao.';
      }
    }

    return fallback;
  }

  private getApiError(error: unknown): { msgError: string } | null {
    if (typeof error !== 'object' || error === null || !('msgError' in error)) {
      return null;
    }

    const msgError = error['msgError'];
    return typeof msgError === 'string' ? { msgError } : null;
  }
}
