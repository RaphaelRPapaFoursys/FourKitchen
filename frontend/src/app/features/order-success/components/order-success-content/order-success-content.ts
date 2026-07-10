import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

@Component({
  selector: 'app-order-success-content',
  templateUrl: './order-success-content.html',
  styleUrl: './order-success-content.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrderSuccessContentComponent {
  readonly showFollowOrder = input(false);
  readonly isTotem = input(false);

  readonly followOrderSelected = output<void>();
  readonly newOrderSelected = output<void>();

  protected followOrder(): void {
    this.followOrderSelected.emit();
  }

  protected startNewOrder(): void {
    this.newOrderSelected.emit();
  }
}
