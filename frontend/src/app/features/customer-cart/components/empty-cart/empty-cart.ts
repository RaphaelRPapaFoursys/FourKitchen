import { ChangeDetectionStrategy, Component, output } from '@angular/core';

@Component({
  selector: 'app-empty-cart',
  templateUrl: './empty-cart.html',
  styleUrl: './empty-cart.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmptyCartComponent {
  readonly continueSelected = output<void>();

  protected continueShopping(): void {
    this.continueSelected.emit();
  }
}
