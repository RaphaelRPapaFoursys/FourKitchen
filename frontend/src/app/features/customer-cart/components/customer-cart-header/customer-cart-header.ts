import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { LanguageSelector } from '../../../../shared/components/language-selector/language-selector';

export type CustomerMenuActiveLink = 'menu' | 'orders' | 'cart' | null;

@Component({
  selector: 'app-customer-cart-header',
  templateUrl: './customer-cart-header.html',
  styleUrl: './customer-cart-header.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [LanguageSelector],
})
export class CustomerCartHeaderComponent {
  readonly homeRoute = input.required<string>();
  readonly cartRoute = input.required<string>();
  readonly ordersRoute = input.required<string>();
  readonly showOrdersLink = input(false);
  readonly activeLink = input<CustomerMenuActiveLink>('cart');
  readonly totalItems = input(0);

  readonly menuSelected = output<Event>();
  readonly cartSelected = output<Event>();
  readonly ordersSelected = output<Event>();

  protected selectMenu(event: Event): void {
    this.menuSelected.emit(event);
  }

  protected selectCart(event: Event): void {
    this.cartSelected.emit(event);
  }

  protected selectOrders(event: Event): void {
    this.ordersSelected.emit(event);
  }

  protected isActive(link: CustomerMenuActiveLink): boolean {
    return this.activeLink() === link;
  }
}
