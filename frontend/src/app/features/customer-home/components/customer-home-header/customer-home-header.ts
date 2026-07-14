import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';

import { Icon } from '../../../../shared/components/icon/icon';

@Component({
  selector: 'app-customer-home-header',
  imports: [Icon],
  templateUrl: './customer-home-header.html',
  styleUrl: './customer-home-header.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerHomeHeaderComponent {
  readonly cartItemsCount = input(0);
  readonly cartRoute = input.required<string>();
  readonly ordersRoute = input.required<string>();
  readonly showOrdersLink = input(false);
  readonly showMesaActions = input(false);
  readonly tableNumber = input<number | null>(null);
  readonly waiterCallLoading = input(false);
  readonly waiterCallSent = input(false);
  readonly waiterCallDisabled = input(false);
  readonly waiterCallUnavailable = input(false);

  readonly sectionSelected = output<{ sectionId: string; event: Event }>();
  readonly cartSelected = output<Event>();
  readonly ordersSelected = output<Event>();
  readonly waiterCallSelected = output<void>();

  protected readonly tableLabel = computed(() => {
    const number = this.tableNumber();
    return number === null ? 'Mesa —' : `Mesa ${String(number).padStart(2, '0')}`;
  });

  protected readonly waiterCallLabel = computed(() => {
    if (this.waiterCallLoading()) {
      return 'Chamando garçom';
    }

    if (this.waiterCallSent()) {
      return 'Garçom chamado';
    }

    return 'Chamar garçom';
  });

  protected readonly waiterCallTitle = computed(() =>
    this.waiterCallUnavailable()
      ? 'A mesa ainda não possui atendimento iniciado.'
      : this.waiterCallLabel(),
  );

  protected selectSection(sectionId: string, event: Event): void {
    this.sectionSelected.emit({ sectionId, event });
  }

  protected selectCart(event: Event): void {
    this.cartSelected.emit(event);
  }

  protected selectOrders(event: Event): void {
    this.ordersSelected.emit(event);
  }

  protected selectWaiterCall(): void {
    this.waiterCallSelected.emit();
  }
}
