import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { finalize, switchMap } from 'rxjs';

import { CartItem } from '../../core/models/cart.models';
import { PagamentoResponse, PaymentMethod } from '../../core/models/payment.models';
import { PedidoTotemResponse } from '../../core/models/order.models';
import { CartService } from '../../core/services/cart.service';
import { CustomerOrderCacheService } from '../../core/services/customer-order-cache.service';
import { OrderService } from '../../core/services/order.service';
import { PaymentService } from '../../core/services/payment.service';

type PaymentState = 'idle' | 'processing' | 'declined' | 'payment-error' | 'order-error';

@Component({
  selector: 'app-totem-payment',
  imports: [CommonModule],
  templateUrl: './totem-payment.html',
  styleUrl: './totem-payment.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TotemPayment {
  private readonly cartService = inject(CartService);
  private readonly orderService = inject(OrderService);
  private readonly orderCacheService = inject(CustomerOrderCacheService);
  private readonly paymentService = inject(PaymentService);
  private readonly router = inject(Router);

  protected readonly items = signal<CartItem[]>(this.cartService.getCart('totem'));
  protected readonly selectedMethod = signal<PaymentMethod>('CREDITO');
  protected readonly state = signal<PaymentState>('idle');
  protected readonly errorMessage = signal('');
  private readonly approvedPayment = signal<PagamentoResponse | null>(null);
  protected readonly totalItems = computed(() =>
    this.items().reduce((total, item) => total + item.quantity, 0),
  );
  protected readonly total = computed(() =>
    this.items().reduce((sum, item) => sum + item.unitPrice * item.quantity, 0),
  );
  protected readonly isProcessing = computed(() => this.state() === 'processing');
  protected readonly canRetryPayment = computed(() =>
    this.state() === 'declined' || this.state() === 'payment-error',
  );

  constructor() {
    if (this.items().length === 0) {
      void this.router.navigate(['/totem/carrinho']);
    }
  }

  protected selectMethod(method: PaymentMethod): void {
    if (!this.isProcessing()) {
      this.selectedMethod.set(method);
    }
  }

  protected processPayment(): void {
    if (this.items().length === 0 || this.isProcessing() || this.state() === 'order-error') {
      return;
    }

    this.state.set('processing');
    this.errorMessage.set('');

    this.paymentService.processPayment().pipe(
      switchMap(payment => {
        this.approvedPayment.set(payment);
        return this.orderService.createTotemOrder({ itens: this.buildOrderItems() });
      }),
      finalize(() => {
        if (this.state() === 'processing') {
          this.state.set('idle');
        }
      }),
    ).subscribe({
      next: order => this.handleCreatedOrder(order),
      error: (error: unknown) => this.handleFlowError(error),
    });
  }

  protected backToCart(): void {
    if (!this.isProcessing()) {
      void this.router.navigate(['/totem/carrinho']);
    }
  }

  protected formatPrice(value: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(value);
  }

  protected trackByCartItemId(_index: number, item: CartItem): string {
    return item.cartItemId;
  }

  private buildOrderItems(): { idProduto: number; quantidade: number; observacao?: string }[] {
    return this.items().map(item => ({
      idProduto: item.productId,
      quantidade: item.quantity,
      observacao: item.observation,
    }));
  }

  private handleCreatedOrder(order: PedidoTotemResponse): void {
    if (order.status !== 'ENVIADO_COZINHA') {
      this.showOrderError();
      return;
    }

    this.orderCacheService.addOrder('totem', order);
    this.cartService.clearCart('totem');
    this.items.set([]);
    void this.router.navigate(['/totem/pedido-criado'], { state: { order } });
  }

  private handleFlowError(error: unknown): void {
    if (this.approvedPayment()) {
      this.showOrderError();
      return;
    }

    if (error instanceof HttpErrorResponse && error.status === 402) {
      this.state.set('declined');
      this.errorMessage.set(this.getApiMessage(error, 'Pagamento recusado. Escolha outra forma e tente novamente.'));
      return;
    }

    this.state.set('payment-error');
    this.errorMessage.set(
      error instanceof HttpErrorResponse
        ? this.getApiMessage(error, 'Falha ao processar o pagamento. Tente novamente.')
        : 'Falha ao processar o pagamento. Tente novamente.',
    );
  }

  private showOrderError(): void {
    this.state.set('order-error');
    this.errorMessage.set(
      'O pagamento foi aprovado, mas não foi possível enviar o pedido para a cozinha. Volte ao carrinho e procure atendimento antes de tentar novamente.',
    );
  }

  private getApiMessage(error: HttpErrorResponse, fallback: string): string {
    const body = error.error as { mensagem?: unknown; msgError?: unknown } | null;
    const message = body?.mensagem ?? body?.msgError;

    return typeof message === 'string' && message.trim() ? message : fallback;
  }
}
