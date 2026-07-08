import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

@Component({
  selector: 'app-customer-cart-header',
  templateUrl: './customer-cart-header.html',
  styleUrl: './customer-cart-header.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerCartHeaderComponent {
  readonly homeRoute = input.required<string>();
  readonly cartRoute = input.required<string>();
  readonly totalItems = input(0);

  readonly menuSelected = output<Event>();
  readonly cartSelected = output<Event>();

  protected selectMenu(event: Event): void {
    this.menuSelected.emit(event);
  }

  protected selectCart(event: Event): void {
    this.cartSelected.emit(event);
  }
}
