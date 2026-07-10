import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

import { CartItem } from '../../../../core/models/cart.models';

@Component({
  selector: 'app-cart-item-card',
  templateUrl: './cart-item-card.html',
  styleUrl: './cart-item-card.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CartItemCardComponent {
  readonly item = input.required<CartItem>();
  readonly productImage = input.required<(item: CartItem) => string>();
  readonly formatPrice = input.required<(price: number) => string>();

  readonly quantityIncreased = output<CartItem>();
  readonly quantityDecreased = output<CartItem>();
  readonly itemRemoved = output<string>();

  protected increaseQuantity(): void {
    this.quantityIncreased.emit(this.item());
  }

  protected decreaseQuantity(): void {
    this.quantityDecreased.emit(this.item());
  }

  protected removeItem(): void {
    this.itemRemoved.emit(this.item().cartItemId);
  }
}
