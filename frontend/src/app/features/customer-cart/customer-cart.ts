import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { CartItem, CustomerContext } from '../../core/models/cart.models';
import { CartService } from '../../core/services/cart.service';

@Component({
  selector: 'app-customer-cart',
  imports: [CommonModule],
  templateUrl: './customer-cart.html',
  styleUrl: './customer-cart.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerCart {
  private readonly cartService = inject(CartService);
  private readonly router = inject(Router);

  protected readonly items = signal<CartItem[]>(this.cartService.getCart(this.getCurrentContext()));
  protected readonly subtotal = computed(() =>
    this.items().reduce((total, item) => total + item.unitPrice * item.quantity, 0),
  );
  protected readonly totalItems = computed(() =>
    this.items().reduce((total, item) => total + item.quantity, 0),
  );

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
    this.router.navigate([`/${this.getCurrentContext()}`]);
  }

  protected goToMenu(event: Event): void {
    event.preventDefault();
    this.continueShopping();
  }

  protected goToCart(event: Event): void {
    event.preventDefault();
  }

  protected confirmOrder(): void {
    const context = this.getCurrentContext();

    if (context === 'totem') {
      this.items().map(item => ({
        idProduto: item.productId,
        quantidade: item.quantity,
        observacao: item.observation,
      }));
    }

    // TODO: chamar OrderService quando a origem do codigoSessao da mesa estiver definida.
    this.router.navigate([`/${context}/pedido-criado`]);
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
    return this.router.url.startsWith('/totem') ? 'totem' : 'mesa';
  }
}
