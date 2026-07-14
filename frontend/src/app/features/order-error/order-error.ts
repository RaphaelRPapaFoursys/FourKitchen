import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Router } from '@angular/router';

import { CustomerContext } from '../../core/models/cart.models';
import { CartService } from '../../core/services/cart.service';
import { CustomerContextService } from '../../core/services/customer-context.service';
import { CustomerCartHeaderComponent } from '../customer-cart/components/customer-cart-header/customer-cart-header';
import { MesaHeaderComponent } from '../../shared/components/mesa-header/mesa-header';

@Component({
  selector: 'app-order-error',
  imports: [CommonModule, CustomerCartHeaderComponent, MesaHeaderComponent],
  templateUrl: './order-error.html',
  styleUrl: './order-error.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrderError {
  private readonly cartService = inject(CartService);
  private readonly customerContextService = inject(CustomerContextService);
  private readonly router = inject(Router);

  protected readonly message = this.getNavigationMessage();
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
  protected readonly totalItems = computed(() =>
    this.cartService.getSummary(this.getCurrentContext()).totalItems,
  );

  protected backToCart(): void {
    this.router.navigate([this.customerContextService.getCartRoute(this.getCurrentContext())]);
  }

  protected goToMenu(event: Event): void {
    event.preventDefault();
    this.router.navigate([this.customerContextService.getHomeRoute(this.getCurrentContext())]);
  }

  protected goToCart(event: Event): void {
    event.preventDefault();
    this.backToCart();
  }

  protected goToOrders(event: Event): void {
    event.preventDefault();

    if (this.getCurrentContext() === 'mesa') {
      this.router.navigate([this.customerContextService.getOrdersRoute('mesa')]);
    }
  }

  protected isMesaContext(): boolean {
    return this.getCurrentContext() === 'mesa';
  }

  private getCurrentContext(): CustomerContext {
    return this.customerContextService.getCurrentContext(this.router.url);
  }

  private getNavigationMessage(): string {
    const fallbackMessage = 'Nao foi possivel enviar seu pedido para a cozinha.';
    const state = window.history.state as { message?: unknown };

    return typeof state.message === 'string' && state.message.trim()
      ? state.message
      : fallbackMessage;
  }
}
