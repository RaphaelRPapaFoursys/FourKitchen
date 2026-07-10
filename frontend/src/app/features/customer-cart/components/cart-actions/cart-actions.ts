import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

@Component({
  selector: 'app-cart-actions',
  templateUrl: './cart-actions.html',
  styleUrl: './cart-actions.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CartActionsComponent {
  readonly isConfirming = input(false);
  readonly confirmLabel = input('Confirmar Pedido');
  readonly confirmingLabel = input('Confirmando...');
  readonly channel = input<'mesa' | 'totem'>('mesa');

  readonly confirmSelected = output<void>();
  readonly continueSelected = output<void>();

  protected confirm(): void {
    this.confirmSelected.emit();
  }

  protected continueShopping(): void {
    this.continueSelected.emit();
  }
}
