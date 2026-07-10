import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

import { ProdutoCardapioView } from '../../../../core/models/menu.models';

@Component({
  selector: 'app-product-grid',
  templateUrl: './product-grid.html',
  styleUrl: './product-grid.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductGridComponent {
  readonly products = input.required<ProdutoCardapioView[]>();
  readonly productImage = input.required<(product: ProdutoCardapioView) => string>();
  readonly formatPrice = input.required<(price: number) => string>();
  readonly trackProduct = input.required<(index: number, product: ProdutoCardapioView) => number>();

  readonly productSelected = output<ProdutoCardapioView>();
  readonly quickAddSelected = output<{ product: ProdutoCardapioView; event: Event }>();

  protected selectProduct(product: ProdutoCardapioView): void {
    this.productSelected.emit(product);
  }

  protected quickAddProduct(product: ProdutoCardapioView, event: Event): void {
    this.quickAddSelected.emit({ product, event });
  }
}
