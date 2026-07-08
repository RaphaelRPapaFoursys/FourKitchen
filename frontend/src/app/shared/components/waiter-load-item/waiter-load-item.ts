import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

import { NivelCarga } from '../../../core/constants/urgencia.constants';
import { Avatar } from '../avatar/avatar';
import { ProgressBar, ProgressVariant } from '../progress-bar/progress-bar';

/** Linha de carga de um garçom: avatar + nome + contagem de mesas + barra de carga.
 *  Reutilizada no card "Carga dos garçons" e nas opções do modal de reatribuição. */
@Component({
  selector: 'fk-waiter-load-item',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Avatar, ProgressBar],
  template: `
    <fk-avatar size="sm" [initial]="initial()" [color]="color()" />
    <div class="info">
      <div class="row">
        <span class="name">{{ name() }}</span>
        <span class="count">{{ count() }} {{ count() === 1 ? 'mesa' : 'mesas' }}</span>
      </div>
      <fk-progress-bar [value]="barWidth()" [variant]="barVariant()" />
    </div>
  `,
  styles: `
    :host {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .info {
      flex: 1;
      min-width: 0;
    }

    .row {
      display: flex;
      align-items: baseline;
      justify-content: space-between;
      margin-bottom: 0.25rem;
    }

    .name {
      font-size: 0.85rem;
      color: var(--fk-text);
    }

    .count {
      color: var(--fk-muted);
      font-size: 0.78rem;
    }
  `,
})
export class WaiterLoadItem {
  readonly name = input('');
  readonly initial = input('?');
  readonly color = input<string | null>(null);
  readonly count = input(0);
  readonly barWidth = input(0);
  readonly level = input<NivelCarga>('BAIXA');

  protected readonly barVariant = computed<ProgressVariant>(() => {
    if (this.level() === 'ALTA') return 'load-high';
    if (this.level() === 'MEDIA') return 'load-medium';
    return 'load-low';
  });
}
