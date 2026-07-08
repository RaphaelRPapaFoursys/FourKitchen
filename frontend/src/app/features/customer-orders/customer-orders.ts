import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { PedidoMesaStatusResponse, PedidoStatus } from '../../core/models/order.models';
import { CustomerOrderCacheService } from '../../core/services/customer-order-cache.service';

type MesaOrdersState =
  | { status: 'loading'; orders: PedidoMesaStatusResponse[]; message: string }
  | { status: 'empty'; orders: PedidoMesaStatusResponse[]; message: string }
  | { status: 'error'; orders: PedidoMesaStatusResponse[]; message: string }
  | { status: 'success'; orders: PedidoMesaStatusResponse[]; message: string };

@Component({
  selector: 'app-customer-orders',
  imports: [CommonModule],
  templateUrl: './customer-orders.html',
  styleUrl: './customer-orders.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerOrders {
  private readonly orderCacheService = inject(CustomerOrderCacheService);
  private readonly router = inject(Router);

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

    try {
      // TODO: trocar pelo endpoint oficial do BFF quando a consulta de pedidos da mesa existir.
      const orders = this.orderCacheService.getMesaOrders();

      this.state.set(
        orders.length > 0
          ? { status: 'success', orders, message: '' }
          : {
            status: 'empty',
            orders: [],
            message: 'Nenhum pedido encontrado para esta mesa.',
          },
      );
    } catch {
      this.state.set({
        status: 'error',
        orders: [],
        message: 'Nao foi possivel carregar seus pedidos. Tente novamente.',
      });
    }
  }

  protected retryLoadMesaOrders(): void {
    this.loadMesaOrders();
  }

  protected backToMenu(): void {
    this.router.navigate(['/mesa']);
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
}
