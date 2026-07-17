import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-cart-summary',
  templateUrl: './cart-summary.html',
  styleUrl: './cart-summary.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslatePipe],
})
export class CartSummaryComponent {
  readonly subtotal = input(0);
  readonly total = input(0);
  readonly errorMessage = input('');
  readonly formatPrice = input.required<(price: number) => string>();
}
