import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

import { CART_OBSERVATION_MAX_LENGTH, CartItem } from '../../../../core/models/cart.models';

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
  readonly observationChanged = output<{ cartItemId: string; observation: string }>();
  readonly observationEditable = input(false);

  protected readonly observationMaxLength = CART_OBSERVATION_MAX_LENGTH;

  protected increaseQuantity(): void {
    this.quantityIncreased.emit(this.item());
  }

  protected decreaseQuantity(): void {
    this.quantityDecreased.emit(this.item());
  }

  protected removeItem(): void {
    this.itemRemoved.emit(this.item().cartItemId);
  }

  protected updateObservation(event: Event): void {
    this.observationChanged.emit({
      cartItemId: this.item().cartItemId,
      observation: (event.target as HTMLInputElement).value,
    });
  }
}
