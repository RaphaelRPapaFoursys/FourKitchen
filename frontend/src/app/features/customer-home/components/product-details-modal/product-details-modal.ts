import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ProdutoCardapioView } from '../../../../core/models/menu.models';

@Component({
  selector: 'app-product-details-modal',
  imports: [FormsModule],
  templateUrl: './product-details-modal.html',
  styleUrl: './product-details-modal.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductDetailsModalComponent {
  readonly product = input.required<ProdutoCardapioView>();
  readonly quantity = input(1);
  readonly observation = input('');
  readonly canAddToCart = input(true);
  readonly productImage = input.required<(product: ProdutoCardapioView) => string>();
  readonly formatPrice = input.required<(price: number) => string>();

  readonly closed = output<void>();
  readonly quantityIncreased = output<void>();
  readonly quantityDecreased = output<void>();
  readonly observationChanged = output<string>();
  readonly addToCart = output<void>();

  protected close(): void {
    this.closed.emit();
  }

  protected increaseQuantity(): void {
    this.quantityIncreased.emit();
  }

  protected decreaseQuantity(): void {
    this.quantityDecreased.emit();
  }

  protected updateObservation(observation: string): void {
    this.observationChanged.emit(observation);
  }

  protected submit(): void {
    this.addToCart.emit();
  }
}
