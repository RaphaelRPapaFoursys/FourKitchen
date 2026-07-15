import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import {
  Subject,
  catchError,
  exhaustMap,
  map,
  merge,
  of,
  switchMap,
  tap,
  timer,
} from 'rxjs';

import { MesaAtendimentoAtualResponse, PedidoMesaStatusResponse, PedidoStatus } from '../../core/models/order.models';
import { CartService } from '../../core/services/cart.service';
import { CustomerContextService } from '../../core/services/customer-context.service';
import { OrderService } from '../../core/services/order.service';
import { MesaHeaderComponent } from '../../shared/components/mesa-header/mesa-header';

type MesaOrdersState =
  | { status: 'loading'; orders: PedidoMesaStatusResponse[]; message: string }
  | { status: 'empty'; orders: PedidoMesaStatusResponse[]; message: string }
  | { status: 'error'; orders: PedidoMesaStatusResponse[]; message: string }
  | { status: 'success'; orders: PedidoMesaStatusResponse[]; message: string };

@Component({
  selector: 'app-customer-orders',
  imports: [CommonModule, MesaHeaderComponent],
  templateUrl: './customer-orders.html',
  styleUrl: './customer-orders.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerOrders {
  private static readonly AUTO_REFRESH_INTERVAL_MS = 10_000;

  private readonly cartService = inject(CartService);
  private readonly customerContextService = inject(CustomerContextService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly orderService = inject(OrderService);
  private readonly router = inject(Router);
  private readonly refreshTrigger = new Subject<void>();
  private readonly terminalStatuses = new Set<PedidoStatus>([
    'ENTREGUE',
    'FINALIZADO',
    'CANCELADO',
  ]);
  private hasLoadedOrders = false;

  protected readonly homeRoute = '/mesa';
  protected readonly cartRoute = '/mesa/carrinho';
  protected readonly ordersRoute = '/mesa/pedidos';
  protected readonly totalItems = computed(() => this.cartService.getSummary('mesa').totalItems);
  protected readonly atendimentoAtual = signal<MesaAtendimentoAtualResponse | null>(null);
  protected readonly carregandoAtendimento = signal(true);
  protected readonly resumoConta = signal<ResumoContaMesaResponse | null>(null);
  protected readonly state = signal<MesaOrdersState>({
    status: 'loading',
    orders: [],
    message: 'Carregando pedidos...',
  });

  constructor() {
    this.startMesaOrdersUpdates();
  }

  protected loadMesaOrders(): void {
    this.carregandoAtendimento.set(true);
    this.resumoConta.set(null);
    this.state.set({
      status: 'loading',
      orders: [],
      message: 'Carregando pedidos...',
    });

    this.refreshTrigger.next();
  }

  private startMesaOrdersUpdates(): void {
    merge(
      timer(
        CustomerOrders.AUTO_REFRESH_INTERVAL_MS,
        CustomerOrders.AUTO_REFRESH_INTERVAL_MS,
      ),
      this.refreshTrigger,
    )
      .pipe(
        exhaustMap(() => this.fetchMesaOrders().pipe(
          map(orders => ({ orders })),
          catchError(() => of(null)),
        )),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(result => {
        this.carregandoAtendimento.set(false);

        if (result === null) {
          if (!this.hasLoadedOrders) {
            this.atendimentoAtual.set(null);
            this.state.set({
              status: 'error',
              orders: [],
              message: 'Nao foi possivel carregar seus pedidos. Tente novamente.',
            });
          }

          return;
        }

        this.hasLoadedOrders = true;
        const orders = this.sortOrders(result.orders);

        this.state.set(
          orders.length > 0
            ? { status: 'success', orders, message: '' }
            : {
              status: 'empty',
              orders: [],
              message: 'Nenhum pedido encontrado para esta mesa.',
            },
        );
      });

    this.refreshTrigger.next();
  }

  private fetchMesaOrders() {
    return this.orderService.getCurrentTableAttendance()
      .pipe(
        tap(atendimento => this.atendimentoAtual.set(atendimento)),
        switchMap(attendance => this.orderService.getMesaOrders(attendance.codigoAtendimento)),
      );
  }

  private sortOrders(orders: PedidoMesaStatusResponse[]): PedidoMesaStatusResponse[] {
    return [...orders].sort((left, right) => (
      this.getStatusPriority(left.status) - this.getStatusPriority(right.status)
    ));
  }

  private getStatusPriority(status: PedidoStatus): number {
    return this.terminalStatuses.has(status) ? 1 : 0;
  }

  protected retryLoadMesaOrders(): void {
    this.loadMesaOrders();
  }

  protected backToMenu(): void {
    this.router.navigate([this.customerContextService.getHomeRoute('mesa')]);
  }

  protected goToMenu(event: Event): void {
    event.preventDefault();
    this.backToMenu();
  }

  protected goToCart(event: Event): void {
    event.preventDefault();
    this.router.navigate([this.customerContextService.getCartRoute('mesa')]);
  }

  protected goToOrders(event: Event): void {
    event.preventDefault();
  }

  protected getStatusLabel(status: PedidoStatus): string {
    const labels: Record<PedidoStatus, string> = {
      ENVIADO_COZINHA: 'Enviado para cozinha',
      EM_PREPARO: 'Em preparo',
      PRONTO: 'Pronto',
      ENTREGUE: 'Entregue',
      FINALIZADO: 'Finalizado',
      CANCELADO: 'Cancelado',
      AGUARDANDO_DECISAO: 'Aguardando decisao',
      PROBLEMA_COZINHA: 'Problema na cozinha',
    };

    return labels[status];
  }

  protected getStatusClass(status: PedidoStatus): string {
    return `orders-status--${status.toLowerCase().replace(/_/g, '-')}`;
  }

  protected formatDate(date?: string): string {
    if (!date) {
      return 'Horario indisponivel';
    }

    return new Intl.DateTimeFormat('pt-BR', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(new Date(date));
  }

  protected trackByOrderId(_index: number, order: PedidoMesaStatusResponse): number {
    return order.id;
  }

  protected getItemName(item: PedidoMesaStatusResponse['itens'][number]): string {
    return item.nome?.trim() || `Produto #${item.idProduto}`;
  }

  protected formatPrice(price: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(price);
  }
}
