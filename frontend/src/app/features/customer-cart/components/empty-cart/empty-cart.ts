import { ChangeDetectionStrategy, Component, output } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-empty-cart',
  templateUrl: './empty-cart.html',
  styleUrl: './empty-cart.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslatePipe],
})
export class EmptyCartComponent {
  readonly continueSelected = output<void>();

  protected continueShopping(): void {
    this.continueSelected.emit();
  }
}
