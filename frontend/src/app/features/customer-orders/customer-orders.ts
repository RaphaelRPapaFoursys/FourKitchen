import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { finalize, switchMap, tap } from 'rxjs';

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
  private readonly cartService = inject(CartService);
  private readonly customerContextService = inject(CustomerContextService);
  private readonly orderService = inject(OrderService);
  private readonly router = inject(Router);

  protected readonly homeRoute = '/mesa';
  protected readonly cartRoute = '/mesa/carrinho';
  protected readonly ordersRoute = '/mesa/pedidos';
  protected readonly totalItems = computed(() => this.cartService.getSummary('mesa').totalItems);
  protected readonly atendimentoAtual = signal<MesaAtendimentoAtualResponse | null>(null);
  protected readonly carregandoAtendimento = signal(true);
  protected readonly state = signal<MesaOrdersState>({
    status: 'loading',
    orders: [],
    message: 'Carregando pedidos...',
  });

  constructor() {
    this.loadMesaOrders();
  }

  protected loadMesaOrders(): void {
    this.state.set({
      status: 'loading',
      orders: [],
      message: 'Carregando pedidos...',
    });

    this.orderService.getCurrentTableAttendance()
      .pipe(
        tap(atendimento => this.atendimentoAtual.set(atendimento)),
        switchMap(attendance => this.orderService.getMesaOrders(attendance.codigoAtendimento)),
        finalize(() => this.carregandoAtendimento.set(false)),
      )
      .subscribe({
        next: orders => {
          this.state.set(
            orders.length > 0
              ? { status: 'success', orders, message: '' }
              : {
                status: 'empty',
                orders: [],
                message: 'Nenhum pedido encontrado para esta mesa.',
              },
          );
        },
        error: () => {
          this.atendimentoAtual.set(null);
          this.state.set({
            status: 'error',
            orders: [],
            message: 'Nao foi possivel carregar seus pedidos. Tente novamente.',
          });
        },
      });
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
}
