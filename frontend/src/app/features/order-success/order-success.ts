import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';

import { CustomerContext } from '../../core/models/cart.models';
import { CartService } from '../../core/services/cart.service';

@Component({
  selector: 'app-order-success',
  imports: [CommonModule],
  templateUrl: './order-success.html',
  styleUrl: './order-success.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrderSuccess {
  private readonly cartService = inject(CartService);
  private readonly router = inject(Router);

  protected startNewOrder(): void {
    const context = this.getCurrentContext();

    this.cartService.clearCart(context);
    this.router.navigate([`/${context}`]);
  }

  protected followOrder(): void {
    // TODO: navegar para acompanhamento quando a rota existir.
    this.router.navigate([`/${this.getCurrentContext()}`]);
  }

  private getCurrentContext(): CustomerContext {
    return this.router.url.startsWith('/totem') ? 'totem' : 'mesa';
  }
}
