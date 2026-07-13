import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { TimeoutError, finalize, timeout } from 'rxjs';

import {
  ChamadaPendenteMesaResponse,
  DecisaoProblemaGarcomRequest,
  MesaGarcomResponse,
  MesaProblemasGarcomResponse,
  ProblemaPedidoGarcomResponse,
} from '../../core/models/garcom.models';
import { CategoriaCardapioResponse, ProdutoCardapioResponse } from '../../core/models/menu.models';
import { AuthService } from '../../core/services/auth';
import { GarcomChamadaService } from '../../core/services/garcom-chamada';
import { GarcomMesaService } from '../../core/services/garcom-mesa';
import { MenuService } from '../../core/services/menu.service';

type FiltroMesa = 'todas' | 'chamadas' | 'problemas';
type AcaoDecisao = 'REMOVER_ITEM' | 'SUBSTITUIR_ITEM' | 'CANCELAR_PEDIDO';

const STATUS_PROBLEMA = new Set(['AGUARDANDO_DECISAO', 'PROBLEMA_COZINHA']);
const INTERVALO_ATUALIZACAO_MS = 10_000;

@Component({
  selector: 'app-garcom',
  standalone: true,
  imports: [],
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
  private intervaloAtualizacao: ReturnType<typeof setInterval> | null = null;

  protected readonly mesas = signal<MesaGarcomResponse[]>([]);
  protected readonly busca = signal('');
  protected readonly filtro = signal<FiltroMesa>('todas');
  protected readonly carregando = signal(false);
  protected readonly erro = signal('');
  protected readonly sucesso = signal('');
  protected readonly notificacaoEmAcao = signal<number | null>(null);

  protected readonly mesaEmDetalhe = signal<MesaGarcomResponse | null>(null);
  protected readonly detalheSelecionado = signal<MesaProblemasGarcomResponse | null>(null);
  protected readonly problemaSelecionado = signal<ProblemaPedidoGarcomResponse | null>(null);
  protected readonly carregandoDetalhe = signal(false);
  protected readonly erroDetalhe = signal('');
  protected readonly salvandoDecisao = signal(false);
  protected readonly acaoDecisao = signal<AcaoDecisao>('REMOVER_ITEM');

  protected readonly categorias = signal<CategoriaCardapioResponse[]>([]);
  protected readonly carregandoCardapio = signal(false);
  protected readonly erroCardapio = signal('');
  protected readonly categoriaSelecionadaId = signal<number | null>(null);
  protected readonly produtoSelecionadoId = signal<number | null>(null);

  protected readonly nomeUsuario = computed(() =>
    this.authService.getCurrentUser()?.nome ?? 'Garcom'
  );

  protected readonly produtosDaCategoria = computed<ProdutoCardapioResponse[]>(() => {
    const categoriaId = this.categoriaSelecionadaId();
    return this.categorias().find(categoria => categoria.categoriaId === categoriaId)?.produtos ?? [];
  });

  protected readonly mesasFiltradas = computed(() => {
    const termo = this.busca().trim().toLowerCase();

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

        return [
          mesa.numero,
          mesa.status,
          ...mesa.pedidosAtivos.flatMap(pedido => [pedido.codigo, pedido.status]),
          ...mesa.chamadasPendentes.map(chamada => chamada.mensagem),
        ].join(' ').toLowerCase().includes(termo);
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

  protected carregarDashboard(silencioso = false): void {
    if (this.dashboardEmCarregamento) {
      return;
    }

    this.dashboardEmCarregamento = true;
    this.erro.set('');
    if (!silencioso) {
      this.carregando.set(true);
    }

    this.garcomMesaService
      .listarMesas()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.dashboardEmCarregamento = false;
          if (!silencioso) {
            this.carregando.set(false);
          }
        })
      )
      .subscribe({
        next: mesas => this.mesas.set(mesas),
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
    this.erro.set('');
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
    if (this.salvandoDecisao()) {
      return;
    }

    this.mesaEmDetalhe.set(null);
    this.detalheSelecionado.set(null);
    this.problemaSelecionado.set(null);
  }

  protected selecionarProblema(problema: ProblemaPedidoGarcomResponse): void {
    this.problemaSelecionado.set(problema);
    this.acaoDecisao.set('REMOVER_ITEM');
    this.categoriaSelecionadaId.set(null);
    this.produtoSelecionadoId.set(null);
  }

  protected alterarAcaoDecisao(acao: AcaoDecisao): void {
    this.acaoDecisao.set(acao);
    if (acao === 'SUBSTITUIR_ITEM' && this.categorias().length === 0 && !this.carregandoCardapio()) {
      this.carregarCardapio();
    }

    if (acao !== 'SUBSTITUIR_ITEM') {
      this.categoriaSelecionadaId.set(null);
      this.produtoSelecionadoId.set(null);
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

    const request = this.montarDecisao(problema);
    this.salvandoDecisao.set(true);
    this.erro.set('');
    this.sucesso.set('');

    this.garcomMesaService
      .registrarDecisao(mesa.idMesa, request)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.salvandoDecisao.set(false))
      )
      .subscribe({
        next: () => {
          this.mesaEmDetalhe.set(null);
          this.detalheSelecionado.set(null);
          this.problemaSelecionado.set(null);
          this.sucesso.set(`Decisao da mesa ${mesa.numero} enviada para a cozinha.`);
          this.carregarDashboard(true);
        },
        error: error => this.erro.set(this.getErrorMessage(error, 'Nao foi possivel registrar a decisao do cliente.')),
      });
  }

  protected mesaPossuiProblema(mesa: MesaGarcomResponse): boolean {
    return mesa.pedidosAtivos.some(pedido => STATUS_PROBLEMA.has(pedido.status));
  }

  protected pedidosComProblema(mesa: MesaGarcomResponse): MesaGarcomResponse['pedidosAtivos'] {
    return mesa.pedidosAtivos.filter(pedido => STATUS_PROBLEMA.has(pedido.status));
  }

  protected tempoAberta(mesa: MesaGarcomResponse): string {
    if (!mesa.dataAbertura) {
      return '';
    }

    const dataAbertura = new Date(mesa.dataAbertura).getTime();
    if (Number.isNaN(dataAbertura)) {
      return '';
    }

    return `${Math.max(0, Math.round((Date.now() - dataAbertura) / 60000))} min`;
  }

  protected pedidosAtivosLabel(mesa: MesaGarcomResponse): string {
    const total = mesa.pedidosAtivos.length;
    return `${total} pedido${total === 1 ? '' : 's'} ativo${total === 1 ? '' : 's'}`;
  }

  protected statusPedidoLabel(status: string): string {
    const labels: Record<string, string> = {
      ENVIADO_COZINHA: 'Enviado a cozinha',
      EM_PREPARO: 'Em preparo',
      PRONTO: 'Pronto',
      ENTREGUE: 'Entregue',
      AGUARDANDO_DECISAO: 'Aguardando decisao',
      PROBLEMA_COZINHA: 'Problema na cozinha',
    };
    return labels[status] ?? status.replaceAll('_', ' ').toLowerCase();
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

  private montarDecisao(problema: ProblemaPedidoGarcomResponse): DecisaoProblemaGarcomRequest {
    const acao = this.acaoDecisao();
    return {
      idPedido: problema.idPedido,
      idProdutoPedido: problema.idProdutoPedido,
      novoStatusProdutoPedido: acao === 'REMOVER_ITEM' ? 'REMOVIDO' : 'DISPONIVEL',
      pedidoCancelado: acao === 'CANCELAR_PEDIDO',
      idNovoProduto: acao === 'SUBSTITUIR_ITEM' ? this.produtoSelecionadoId() : null,
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
