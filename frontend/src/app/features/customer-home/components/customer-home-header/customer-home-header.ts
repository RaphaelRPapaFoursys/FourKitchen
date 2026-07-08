import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

@Component({
  selector: 'app-customer-home-header',
  templateUrl: './customer-home-header.html',
  styleUrl: './customer-home-header.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerHomeHeaderComponent {
  readonly cartItemsCount = input(0);
  readonly cartRoute = input.required<string>();
  readonly ordersRoute = input.required<string>();
  readonly showOrdersLink = input(false);

  readonly sectionSelected = output<{ sectionId: string; event: Event }>();
  readonly cartSelected = output<Event>();
  readonly ordersSelected = output<Event>();

  protected selectSection(sectionId: string, event: Event): void {
    this.sectionSelected.emit({ sectionId, event });
  }

  protected selectCart(event: Event): void {
    this.cartSelected.emit(event);
  }

  protected selectOrders(event: Event): void {
    this.ordersSelected.emit(event);
  }
}
