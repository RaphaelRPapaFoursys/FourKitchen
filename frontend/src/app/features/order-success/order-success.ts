import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Router } from '@angular/router';

import { CustomerContext } from '../../core/models/cart.models';
import { CartService } from '../../core/services/cart.service';
import { CustomerContextService } from '../../core/services/customer-context.service';
import { CustomerCartHeaderComponent } from '../customer-cart/components/customer-cart-header/customer-cart-header';
import { OrderSuccessContentComponent } from './components/order-success-content/order-success-content';

@Component({
  selector: 'app-order-success',
  imports: [CommonModule, CustomerCartHeaderComponent, OrderSuccessContentComponent],
  templateUrl: './order-success.html',
  styleUrl: './order-success.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrderSuccess {
  private readonly cartService = inject(CartService);
  private readonly customerContextService = inject(CustomerContextService);
  private readonly router = inject(Router);

  protected readonly homeRoute = computed(() =>
    this.customerContextService.getHomeRoute(this.getCurrentContext()),
  );
  protected readonly cartRoute = computed(() =>
    this.customerContextService.getCartRoute(this.getCurrentContext()),
  );
  protected readonly ordersRoute = computed(() =>
    this.customerContextService.getOrdersRoute(this.getCurrentContext()),
  );
  protected readonly showOrdersLink = computed(() => this.isMesaContext());
  protected readonly totalItems = computed(() =>
    this.cartService.getSummary(this.getCurrentContext()).totalItems,
  );

  constructor() {
    if (!this.hasConfirmedOrder()) {
      this.router.navigate([this.customerContextService.getHomeRoute(this.getCurrentContext())]);
    }
  }

  protected startNewOrder(): void {
    const context = this.getCurrentContext();

    this.router.navigate([this.customerContextService.getHomeRoute(context)]);
  }

  protected followOrder(): void {
    if (this.isMesaContext()) {
      this.router.navigate([this.customerContextService.getOrdersRoute('mesa')]);
    }
  }

  protected goToMenu(event: Event): void {
    event.preventDefault();
    this.startNewOrder();
  }

  protected goToCart(event: Event): void {
    event.preventDefault();
    this.router.navigate([this.customerContextService.getCartRoute(this.getCurrentContext())]);
  }

  protected goToOrders(event: Event): void {
    event.preventDefault();
    this.followOrder();
  }

  protected isMesaContext(): boolean {
    return this.getCurrentContext() === 'mesa';
  }

  private getCurrentContext(): CustomerContext {
    return this.customerContextService.getCurrentContext(this.router.url);
  }

  private hasConfirmedOrder(): boolean {
    const state = window.history.state as { order?: { status?: unknown } };

    return state.order?.status === 'ENVIADO_COZINHA';
  }
}
