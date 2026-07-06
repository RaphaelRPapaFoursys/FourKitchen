import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export type ProgressVariant = 'orange' | 'load-low' | 'load-medium' | 'load-high' | 'blue';
export type ProgressThickness = 'sm' | 'md' | 'lg';

/** Barra de progresso apresentacional (trilho + fill). Reutilizada no progresso do pedido,
 *  na carga dos garçons e no gráfico de mesas mais ocupadas. */
@Component({
  selector: 'fk-progress-bar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="track" [class]="'track--' + thickness()">
      <div class="fill" [class]="'fill--' + variant()" [style.width.%]="largura()"></div>
    </div>
  `,
  styles: `
    :host {
      display: block;
    }

    .track {
      border-radius: var(--fk-radius-pill);
      background: var(--fk-surface-muted);
      overflow: hidden;
    }

    .track--sm {
      height: 5px;
    }

    .track--md {
      height: 6px;
    }

    .track--lg {
      height: 8px;
    }

    .fill {
      height: 100%;
      border-radius: var(--fk-radius-pill);
      transition: width 220ms ease;
    }

    .fill--orange {
      background: var(--fk-orange-progress);
    }

    .fill--load-low {
      background: var(--fk-green);
    }

    .fill--load-medium {
      background: var(--fk-amber);
    }

    .fill--load-high {
      background: var(--fk-red);
    }

    .fill--blue {
      background: var(--fk-blue);
    }
  `,
})
export class ProgressBar {
  readonly value = input(0);
  readonly variant = input<ProgressVariant>('orange');
  readonly thickness = input<ProgressThickness>('md');

  protected readonly largura = computed(() => Math.min(100, Math.max(0, this.value())));
}
