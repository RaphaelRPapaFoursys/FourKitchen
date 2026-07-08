import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';

import { CustomerContext } from '../../core/models/cart.models';
import { CartService } from '../../core/services/cart.service';
import { CustomerContextService } from '../../core/services/customer-context.service';
import { OrderSuccessContentComponent } from './components/order-success-content/order-success-content';

@Component({
  selector: 'app-order-success',
  imports: [CommonModule, OrderSuccessContentComponent],
  templateUrl: './order-success.html',
  styleUrl: './order-success.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrderSuccess {
  private readonly cartService = inject(CartService);
  private readonly customerContextService = inject(CustomerContextService);
  private readonly router = inject(Router);

  protected startNewOrder(): void {
    const context = this.getCurrentContext();

    this.cartService.clearCart(context);
    this.router.navigate([this.customerContextService.getHomeRoute(context)]);
  }

  protected followOrder(): void {
    if (this.isMesaContext()) {
      this.router.navigate([this.customerContextService.getOrdersRoute('mesa')]);
    }
  }

  protected isMesaContext(): boolean {
    return this.getCurrentContext() === 'mesa';
  }

  private getCurrentContext(): CustomerContext {
    return this.customerContextService.getCurrentContext(this.router.url);
  }
}
