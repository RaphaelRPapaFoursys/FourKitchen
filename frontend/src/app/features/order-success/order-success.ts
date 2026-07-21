import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Router } from '@angular/router';

import { CustomerContext } from '../../core/models/cart.models';
import { CartService } from '../../core/services/cart.service';
import { CustomerContextService } from '../../core/services/customer-context.service';
import { CustomerCartHeaderComponent } from '../customer-cart/components/customer-cart-header/customer-cart-header';
import { MesaHeaderComponent } from '../../shared/components/mesa-header/mesa-header';
import { OrderSuccessContentComponent } from './components/order-success-content/order-success-content';

@Component({
  selector: 'app-order-success',
  imports: [CommonModule, CustomerCartHeaderComponent, MesaHeaderComponent, OrderSuccessContentComponent],
  templateUrl: './order-success.html',
  styleUrl: './order-success.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrderSuccess {
  private readonly cartService = inject(CartService);
  private readonly customerContextService = inject(CustomerContextService);
  private readonly router = inject(Router);
  private readonly confirmedOrder = this.readConfirmedOrder();

  protected readonly orderCode = this.confirmedOrder?.codigo ?? null;

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
    if (!this.confirmedOrder) {
      this.router.navigate([this.customerContextService.getHomeRoute(this.getCurrentContext())]);
    }
  }

  protected startNewOrder(): void {
    const context = this.getCurrentContext();

    this.router.navigate(
      [this.customerContextService.getHomeRoute(context)],
      context === 'totem' ? { replaceUrl: true } : undefined,
    );
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

  private readConfirmedOrder(): { codigo: number; status: string } | null {
    const state = window.history.state as {
      order?: { codigo?: unknown; status?: unknown };
    };
    const order = state.order;

    if (
      order?.status !== 'ENVIADO_COZINHA'
      || typeof order.codigo !== 'number'
      || !Number.isInteger(order.codigo)
      || order.codigo <= 0
    ) {
      return null;
    }

    return { codigo: order.codigo, status: order.status };
  }
}
