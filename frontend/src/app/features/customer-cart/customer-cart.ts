import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { finalize, Observable, switchMap } from 'rxjs';

import { CartItem, CustomerContext } from '../../core/models/cart.models';
import { PedidoResponse } from '../../core/models/order.models';
import { CartService } from '../../core/services/cart.service';
import { CustomerContextService } from '../../core/services/customer-context.service';
import { CustomerOrderCacheService } from '../../core/services/customer-order-cache.service';
import { OrderService } from '../../core/services/order.service';
import { CartActionsComponent } from './components/cart-actions/cart-actions';
import { CartItemCardComponent } from './components/cart-item-card/cart-item-card';
import { CartSummaryComponent } from './components/cart-summary/cart-summary';
import { CustomerCartHeaderComponent } from './components/customer-cart-header/customer-cart-header';
import { EmptyCartComponent } from './components/empty-cart/empty-cart';

@Component({
  selector: 'app-customer-cart',
  imports: [
    CommonModule,
    CustomerCartHeaderComponent,
    CartItemCardComponent,
    CartSummaryComponent,
    CartActionsComponent,
    EmptyCartComponent,
  ],
  templateUrl: './customer-cart.html',
  styleUrl: './customer-cart.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerCart {
  private readonly cartService = inject(CartService);
  private readonly orderService = inject(OrderService);
  private readonly orderCacheService = inject(CustomerOrderCacheService);
  private readonly customerContextService = inject(CustomerContextService);
  private readonly router = inject(Router);

  protected readonly items = signal<CartItem[]>(this.cartService.getCart(this.getCurrentContext()));
  protected readonly subtotal = computed(() =>
    this.items().reduce((total, item) => total + item.unitPrice * item.quantity, 0),
  );
  protected readonly totalItems = computed(() =>
    this.items().reduce((total, item) => total + item.quantity, 0),
  );
  protected readonly isConfirmingOrder = signal(false);
  protected readonly confirmOrderError = signal('');
  protected readonly homeRoute = computed(() =>
    this.customerContextService.getHomeRoute(this.getCurrentContext()),
  );
  protected readonly cartRoute = computed(() =>
    this.customerContextService.getCartRoute(this.getCurrentContext()),
  );
  protected readonly ordersRoute = computed(() =>
    this.customerContextService.getOrdersRoute(this.getCurrentContext()),
  );
  protected readonly showOrdersLink = computed(() => this.getCurrentContext() === 'mesa');

  protected increaseQuantity(item: CartItem): void {
    this.items.set(
      this.cartService.updateQuantity(
        this.getCurrentContext(),
        item.cartItemId,
        item.quantity + 1,
      ),
    );
  }

  protected decreaseQuantity(item: CartItem): void {
    if (item.quantity <= 1) {
      return;
    }

    this.items.set(
      this.cartService.updateQuantity(
        this.getCurrentContext(),
        item.cartItemId,
        item.quantity - 1,
      ),
    );
  }

  protected removeItem(cartItemId: string): void {
    this.items.set(this.cartService.removeItem(this.getCurrentContext(), cartItemId));
  }

  protected continueShopping(): void {
    this.router.navigate([this.customerContextService.getHomeRoute(this.getCurrentContext())]);
  }

  protected goToMenu(event: Event): void {
    event.preventDefault();
    this.continueShopping();
  }

  protected goToCart(event: Event): void {
    event.preventDefault();
  }

  protected goToOrders(event: Event): void {
    event.preventDefault();

    if (this.getCurrentContext() === 'mesa') {
      this.router.navigate([this.customerContextService.getOrdersRoute('mesa')]);
    }
  }

  protected confirmOrder(): void {
    const context = this.getCurrentContext();

    if (this.items().length === 0 || this.isConfirmingOrder()) {
      return;
    }

    if (context === 'totem') {
      void this.router.navigate(['/totem/pagamento']);
      return;
    }

    this.confirmOrderError.set('');
    this.isConfirmingOrder.set(true);

    const submitOrder$: Observable<PedidoResponse> = this.orderService.getCurrentTableAttendance().pipe(
      switchMap(attendance => this.orderService.createMesaOrder({
        codigoAtendimento: attendance.codigoAtendimento,
        itens: this.buildOrderItems(),
      })),
    );

    submitOrder$
      .pipe(finalize(() => this.isConfirmingOrder.set(false)))
      .subscribe({
        next: order => {
          if (!this.isOrderSentToKitchen(order)) {
            this.navigateToOrderError(context, 'Nao foi possivel enviar seu pedido para a cozinha.');

            return;
          }

          this.orderCacheService.addOrder(context, order);
          this.cartService.clearCart(context);
          this.items.set([]);
          this.router.navigate([this.customerContextService.getSuccessRoute(context)], {
            state: { order },
          });
        },
        error: (error: unknown) => {
          this.navigateToOrderError(context, this.getApiErrorMessage(error));
        },
      });
  }

  protected getProductImage(item: CartItem): string {
    if (!item.image) {
      return 'assets/images/product-placeholder.svg';
    }

    return item.image.startsWith('data:image')
      ? item.image
      : `data:image/png;base64,${item.image}`;
  }

  protected formatPrice(price: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(price);
  }

  protected trackByCartItemId(_index: number, item: CartItem): string {
    return item.cartItemId;
  }

  protected getCurrentContext(): CustomerContext {
    return this.customerContextService.getCurrentContext(this.router.url);
  }

  private buildOrderItems(): { idProduto: number; quantidade: number; observacao?: string }[] {
    return this.items().map(item => ({
      idProduto: item.productId,
      quantidade: item.quantity,
      observacao: item.observation,
    }));
  }

  private isOrderSentToKitchen(response: { status?: string }): boolean {
    return response.status === 'ENVIADO_COZINHA';
  }

  private navigateToOrderError(context: CustomerContext, message: string): void {
    this.router.navigate([this.customerContextService.getErrorRoute(context)], {
      state: { message },
    });
  }

  private getApiErrorMessage(error: unknown): string {
    const fallbackMessage = 'Nao foi possivel enviar seu pedido para a cozinha.';

    if (!(error instanceof HttpErrorResponse)) {
      return fallbackMessage;
    }

    const apiError = error.error;

    if (
      apiError
      && typeof apiError === 'object'
      && 'msgError' in apiError
      && typeof apiError.msgError === 'string'
      && apiError.msgError.trim()
    ) {
      return apiError.msgError;
    }

    return fallbackMessage;
  }
}
