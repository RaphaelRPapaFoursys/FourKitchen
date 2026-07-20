import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { debounceTime, finalize, forkJoin } from 'rxjs';

import {
  CriarPedidoGarcomRequest,
  ItemPedidoGarcomRequest,
  MesaGarcomDetalheResponse,
} from '../../core/models/garcom.models';
import { CategoriaCardapioResponse, ProdutoCardapioResponse } from '../../core/models/menu.models';
import { GarcomMesaService } from '../../core/services/garcom-mesa';
import { GarcomPedidoService } from '../../core/services/garcom-pedido';
import { MenuService } from '../../core/services/menu.service';
import { RealtimeTopic } from '../../core/models/realtime.models';
import { RealtimeService } from '../../core/services/realtime.service';
import { AuthService } from '../../core/services/auth';
import { Badge } from '../../shared/components/badge/badge';
import { Icon } from '../../shared/components/icon/icon';
import { UserMenu } from '../../shared/components/user-menu/user-menu';

interface ItemCarrinhoGarcom extends ItemPedidoGarcomRequest {
  nome: string;
}

@Component({
  selector: 'app-garcom-pedido',
  standalone: true,
  imports: [Badge, Icon, UserMenu],
  templateUrl: './garcom-pedido.html',
  styleUrl: './garcom-pedido.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GarcomPedido {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly garcomMesaService = inject(GarcomMesaService);
  private readonly garcomPedidoService = inject(GarcomPedidoService);
  private readonly menuService = inject(MenuService);
  private readonly realtimeService = inject(RealtimeService);
  private readonly authService = inject(AuthService);

  private readonly idMesa = Number(this.route.snapshot.paramMap.get('id'));

  protected readonly carregando = signal(true);
  protected readonly erro = signal('');
  protected readonly enviando = signal(false);
  protected readonly sucesso = signal('');
  protected readonly detalhe = signal<MesaGarcomDetalheResponse | null>(null);
  protected readonly categorias = signal<CategoriaCardapioResponse[]>([]);
  protected readonly categoriaSelecionadaId = signal<number | null>(null);
  protected readonly busca = signal('');
  protected readonly itens = signal<ItemCarrinhoGarcom[]>([]);
  protected readonly modalLimparCarrinhoAberto = signal(false);
  protected readonly nomeUsuario = this.authService.getCurrentUser()?.nome ?? 'Garçom';
  protected readonly inicialUsuario = this.nomeUsuario.charAt(0).toUpperCase();

  protected readonly categoriaSelecionada = computed(() =>
    this.categorias().find(categoria => categoria.categoriaId === this.categoriaSelecionadaId()) ?? null,
  );
  protected readonly produtosFiltrados = computed(() => {
    const termo = this.busca().trim().toLocaleLowerCase();
    return (this.categoriaSelecionada()?.produtos ?? []).filter(produto =>
      !termo || `${produto.nome} ${produto.descricao}`.toLocaleLowerCase().includes(termo),
    );
  });
  protected readonly totalItens = computed(() => this.itens().reduce((total, item) => total + item.quantidade, 0));
  protected readonly total = computed(() => this.itens().reduce(
    (total, item) => total + item.quantidade * item.precoUnitario,
    0,
  ));
  protected readonly pedidosAtivos = computed(() =>
    (this.detalhe()?.pedidos ?? []).filter(pedido =>
      ['ENVIADO_COZINHA', 'EM_PREPARO', 'PRONTO', 'AGUARDANDO_DECISAO', 'PROBLEMA_COZINHA'].includes(pedido.status),
    ),
  );

  constructor() {
    this.carregar();
    this.realtimeService
      .watch(RealtimeTopic.cardapio)
      .pipe(debounceTime(200), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.atualizarCardapio());
    this.realtimeService.reconnected$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.atualizarCardapio());
  }

  protected carregar(): void {
    if (!Number.isInteger(this.idMesa) || this.idMesa <= 0) {
      this.erro.set('Mesa invalida. Volte ao painel e selecione uma mesa.');
      this.carregando.set(false);
      return;
    }

    this.carregando.set(true);
    this.erro.set('');
    forkJoin({
      detalhe: this.garcomMesaService.detalharMesa(this.idMesa),
      categorias: this.menuService.getMenu('garcom'),
    })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.carregando.set(false)),
      )
      .subscribe({
        next: ({ detalhe, categorias }) => {
          this.detalhe.set(detalhe);
          const categoriasComProdutos = categorias.filter(categoria => categoria.produtos.length > 0);
          this.categorias.set(categoriasComProdutos);
          this.categoriaSelecionadaId.set(categoriasComProdutos[0]?.categoriaId ?? null);
        },
        error: error => this.erro.set(this.getErrorMessage(error, 'Nao foi possivel carregar esta mesa.')),
      });
  }

  protected selecionarCategoria(id: number): void {
    this.categoriaSelecionadaId.set(id);
  }

  private atualizarCardapio(): void {
    this.menuService
      .refreshMenu('garcom')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: categorias => {
          const categoriasComProdutos = categorias.filter(categoria => categoria.produtos.length > 0);
          const categoriaAtual = this.categoriaSelecionadaId();
          this.categorias.set(categoriasComProdutos);
          this.categoriaSelecionadaId.set(
            categoriasComProdutos.some(categoria => categoria.categoriaId === categoriaAtual)
              ? categoriaAtual
              : categoriasComProdutos[0]?.categoriaId ?? null,
          );
        },
      });
  }

  protected atualizarBusca(event: Event): void {
    this.busca.set((event.target as HTMLInputElement).value);
  }

  protected adicionarProduto(produto: ProdutoCardapioResponse): void {
    this.itens.update(itens => {
      const existente = itens.find(item => item.idProduto === produto.id && !item.observacao);
      if (existente) {
        return itens.map(item => item === existente ? { ...item, quantidade: item.quantidade + 1 } : item);
      }

      return [...itens, {
        idProduto: produto.id,
        nome: produto.nome,
        quantidade: 1,
        precoUnitario: produto.preco,
      }];
    });
  }

  protected alterarQuantidade(itemSelecionado: ItemCarrinhoGarcom, quantidade: number): void {
    this.itens.update(itens => itens
      .map(item => item === itemSelecionado ? { ...item, quantidade: Math.max(0, quantidade) } : item)
      .filter(item => item.quantidade > 0),
    );
  }

  protected alterarObservacao(itemSelecionado: ItemCarrinhoGarcom, event: Event): void {
    const observacao = (event.target as HTMLInputElement).value.trim();
    this.itens.update(itens => itens.map(item => item === itemSelecionado
      ? { ...item, observacao: observacao || undefined }
      : item,
    ));
  }

  protected abrirModalLimparCarrinho(): void {
    if (this.itens().length === 0) {
      return;
    }

    this.modalLimparCarrinhoAberto.set(true);
  }

  protected fecharModalLimparCarrinho(): void {
    this.modalLimparCarrinhoAberto.set(false);
  }

  protected confirmarLimparCarrinho(): void {
    this.itens.set([]);
    this.modalLimparCarrinhoAberto.set(false);

    this.erro.set('');
    this.sucesso.set('Carrinho limpo com sucesso!');
  }

  protected quantidadeNoCarrinho(idProduto: number): number {
    return this.itens()
      .filter(item => item.idProduto === idProduto)
      .reduce((total, item) => total + item.quantidade, 0);
  }

  protected enviarPedido(): void {
    if (this.enviando() || this.itens().length === 0) {
      return;
    }

    const request: CriarPedidoGarcomRequest = {
      idMesa: this.idMesa,
      itens: this.itens().map(({ nome, ...item }) => item),
    };

    this.enviando.set(true);
    this.erro.set('');
    this.sucesso.set('');
    this.garcomPedidoService.criarPedido(request)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.enviando.set(false)),
      )
      .subscribe({
        next: pedido => {
          this.itens.set([]);
          this.sucesso.set(`Pedido #${pedido.codigo} enviado para a cozinha.`);
          this.carregarDetalhe();
        },
        error: error => this.erro.set(this.getErrorMessage(error, 'Nao foi possivel enviar o pedido para a cozinha.')),
      });
  }

  protected voltar(): void {
    void this.router.navigateByUrl('/garcom');
  }

  protected formatarMoeda(valor: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
  }

  protected statusPedido(status: string): string {
    return ({ ENVIADO_COZINHA: 'Enviado a cozinha', EM_PREPARO: 'Em preparo', PRONTO: 'Pronto' }[status] ?? status.replaceAll('_', ' ').toLowerCase());
  }

  private carregarDetalhe(): void {
    this.garcomMesaService.detalharMesa(this.idMesa)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ next: detalhe => this.detalhe.set(detalhe) });
  }

  private getErrorMessage(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse && error.error && typeof error.error === 'object') {
      const mensagem = (error.error as { msgError?: unknown }).msgError;
      if (typeof mensagem === 'string' && mensagem.trim()) return mensagem;
      if (error.status === 403) return 'Esta mesa nao esta atribuida a voce.';
    }
    return fallback;
  }
}
